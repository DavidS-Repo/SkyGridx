package main;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores chest data for all worlds in a SQLite database.
 */
public class ChestRegionData {
	private static ChestRegionData instance;
	private final JavaPlugin plugin;
	private final String jdbcUrl;
	private Connection connection;

	/**
	 * Chest type enum.
	 */
	public enum ChestType { CUSTOM, VANILLA }

	/**
	 * Holds info about a chest.
	 */
	public static class ChestInfo {
		public final ChestType type;
		public final String lootTableKey;

		public ChestInfo(ChestType type, String lootTableKey) {
			this.type = type;
			this.lootTableKey = lootTableKey;
		}
	}

	/**
	 * Represents a chest record for batch inserts.
	 */
	public static class ChestRecord {
		public final Location location;
		public final ChestType type;
		public final String lootTableKey;

		public ChestRecord(Location location, ChestType type, String lootTableKey) {
			this.location = location;
			this.type = type;
			this.lootTableKey = lootTableKey;
		}
	}

	private ChestRegionData(JavaPlugin plugin) {
		this.plugin = plugin;
		this.jdbcUrl = initDatabase(plugin);
	}

	/**
	 * Returns the singleton instance.
	 */
	public static ChestRegionData getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new ChestRegionData(plugin);
		}
		return instance;
	}

	/**
	 * Sets up the SQLite database and schema.
	 */
	private String initDatabase(JavaPlugin plugin) {
		File dataFolder = plugin.getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		File dbFile = new File(dataFolder, "chests.db");
		String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe("SQLite JDBC driver not found: " + e.getMessage());
		}

		try (Connection conn = DriverManager.getConnection(url)) {
			try (PreparedStatement p1 = conn.prepareStatement("PRAGMA journal_mode = WAL")) {
				p1.execute();
			}
			try (PreparedStatement p2 = conn.prepareStatement("PRAGMA synchronous = NORMAL")) {
				p2.execute();
			}
			try (PreparedStatement p3 = conn.prepareStatement("PRAGMA busy_timeout = 5000")) {
				p3.execute();
			}

			String createTable =
					"CREATE TABLE IF NOT EXISTS chests (" +
							"world TEXT NOT NULL," +
							"x INTEGER NOT NULL," +
							"y INTEGER NOT NULL," +
							"z INTEGER NOT NULL," +
							"region_key INTEGER NOT NULL," +
							"chest_key INTEGER NOT NULL," +
							"type INTEGER NOT NULL," +
							"loot_table TEXT," +
							"looted INTEGER NOT NULL DEFAULT 0," +
							"PRIMARY KEY (world, chest_key)" +
							")";
			try (PreparedStatement stmt = conn.prepareStatement(createTable)) {
				stmt.execute();
			}

			String createIndex =
					"CREATE INDEX IF NOT EXISTS idx_chests_world_region " +
							"ON chests(world, region_key)";
			try (PreparedStatement stmt = conn.prepareStatement(createIndex)) {
				stmt.execute();
			}
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to init chest database: " + e.getMessage());
		}

		return url;
	}

	/**
	 * Returns the shared database connection.
	 */
	private synchronized Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(jdbcUrl);
			try (Statement st = connection.createStatement()) {
				st.execute("PRAGMA journal_mode = WAL");
				st.execute("PRAGMA synchronous = NORMAL");
				st.execute("PRAGMA busy_timeout = 5000");
			}
		}
		return connection;
	}

	/**
	 * Registers or updates a single chest entry.
	 */
	public void registerChest(Location loc, ChestType type, String lootTableKey) {
		List<ChestRecord> list = new ArrayList<>(1);
		list.add(new ChestRecord(loc, type, lootTableKey));
		registerChests(list);
	}

	/**
	 * Registers or updates multiple chest entries in one transaction.
	 */
	public void registerChests(Iterable<ChestRecord> records) {
		String sql =
				"INSERT OR REPLACE INTO chests " +
						"(world, x, y, z, region_key, chest_key, type, loot_table, looted) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

		boolean hasAny = false;

		try {
			Connection conn = getConnection();
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				for (ChestRecord rec : records) {
					if (rec == null || rec.location == null || rec.location.getWorld() == null) {
						continue;
					}
					hasAny = true;

					Location loc = rec.location;
					long regionKey = getRegionKey(loc);
					long chestKey = getChestKey(loc);
					String worldName = loc.getWorld().getName();

					ps.setString(1, worldName);
					ps.setInt(2, loc.getBlockX());
					ps.setInt(3, loc.getBlockY());
					ps.setInt(4, loc.getBlockZ());
					ps.setLong(5, regionKey);
					ps.setLong(6, chestKey);
					ps.setInt(7, rec.type == ChestType.CUSTOM ? 0 : 1);
					ps.setString(8, rec.lootTableKey);
					ps.addBatch();
				}

				if (hasAny) {
					ps.executeBatch();
				}
			}

			if (hasAny) {
				conn.commit();
			} else {
				conn.rollback();
			}
			conn.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to bulk register chests: " + e.getMessage());
		}
	}

	/**
	 * Returns info for a chest that is not looted yet.
	 */
	public ChestInfo getChestInfo(Location loc) {
		long chestKey = getChestKey(loc);
		String worldName = loc.getWorld().getName();

		String sql =
				"SELECT type, loot_table " +
						"FROM chests " +
						"WHERE world = ? AND chest_key = ? AND looted = 0";

		try {
			Connection conn = getConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, worldName);
				ps.setLong(2, chestKey);

				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						int typeId = rs.getInt("type");
						String lootTable = rs.getString("loot_table");
						ChestType type = (typeId == 0) ? ChestType.CUSTOM : ChestType.VANILLA;
						return new ChestInfo(type, lootTable);
					}
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to get chest info: " + e.getMessage());
		}

		return null;
	}

	/**
	 * Marks a chest as looted.
	 */
	public void markChestLooted(Location loc) {
		long chestKey = getChestKey(loc);
		String worldName = loc.getWorld().getName();

		String sql =
				"UPDATE chests " +
						"SET looted = 1 " +
						"WHERE world = ? AND chest_key = ? AND looted = 0";

		try {
			Connection conn = getConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, worldName);
				ps.setLong(2, chestKey);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			plugin.getLogger().warning("Failed to mark chest looted: " + e.getMessage());
		}
	}

	/**
	 * Hook for plugin shutdown.
	 */
	public void shutdown() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ignored) {
			}
		}
	}

	/**
	 * Computes region key from block location.
	 */
	private long getRegionKey(Location loc) {
		int regionX = loc.getBlockX() >> 9;
		int regionZ = loc.getBlockZ() >> 9;
		return packRegionCoords(regionX, regionZ);
	}

	/**
	 * Packs a block position into a chest key.
	 */
	private long getChestKey(Location loc) {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		return ((long) x & 0x3FFFFFFL) << 38
				| ((long) z & 0x3FFFFFFL) << 12
				| ((long) y & 0xFFFL);
	}

	/**
	 * Packs region coordinates into a single long.
	 */
	private long packRegionCoords(int x, int z) {
		return ((long) x << 32) | (z & 0xFFFFFFFFL);
	}
}
