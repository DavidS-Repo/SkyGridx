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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores chest data for all worlds in a SQLite database.
 */
public class ChestRegionData {
	private static final int MAX_PENDING_RECORDS = 65_536;
	private static final int QUEUE_WARNING_THRESHOLD = MAX_PENDING_RECORDS * 3 / 4;
	private static final int MAX_BATCH_SIZE = 4_096;
	private static final int CHECKPOINT_EVERY_BATCHES = 64;
	private static final long FLUSH_INTERVAL_MILLIS = 25L;
	private static final long QUEUE_OFFER_TIMEOUT_MILLIS = 250L;
	private static final long QUEUE_WARNING_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(5);

	private static ChestRegionData instance;
	private final JavaPlugin plugin;
	private final String jdbcUrl;
	private final Object dbLock = new Object();
	private final BlockingQueue<ChestRecord> writeQueue = new ArrayBlockingQueue<>(MAX_PENDING_RECORDS);
	private final AtomicBoolean writerRunning = new AtomicBoolean(true);
	private final AtomicLong lastQueueWarning = new AtomicLong();
	private final Thread writerThread;
	private Connection connection;
	private long batchesSinceCheckpoint;

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
		public final String worldName;
		public final int x;
		public final int y;
		public final int z;
		public final long regionKey;
		public final long chestKey;
		public final ChestType type;
		public final String lootTableKey;

		public ChestRecord(Location location, ChestType type, String lootTableKey) {
			if (location == null || location.getWorld() == null) {
				this.worldName = null;
				this.x = 0;
				this.y = 0;
				this.z = 0;
				this.regionKey = 0L;
				this.chestKey = 0L;
			} else {
				this.worldName = location.getWorld().getName();
				this.x = location.getBlockX();
				this.y = location.getBlockY();
				this.z = location.getBlockZ();
				this.regionKey = getRegionKey(x, z);
				this.chestKey = getChestKey(x, y, z);
			}
			this.type = type;
			this.lootTableKey = lootTableKey;
		}

		private boolean isValid() {
			return worldName != null && type != null;
		}
	}

	private ChestRegionData(JavaPlugin plugin) {
		this.plugin = plugin;
		this.jdbcUrl = initDatabase(plugin);
		this.writerThread = new Thread(this::runWriter, "SkyGrid-ChestDB-Writer");
		this.writerThread.setDaemon(true);
		this.writerThread.start();
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
			try (PreparedStatement p4 = conn.prepareStatement("PRAGMA wal_autocheckpoint = 1000")) {
				p4.execute();
			}
			try (PreparedStatement p5 = conn.prepareStatement("PRAGMA cache_size = -8192")) {
				p5.execute();
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
			migrateWorldNames(conn);
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to init chest database: " + e.getMessage());
		}

		return url;
	}

	private void migrateWorldNames(Connection conn) throws SQLException {
		List<String> activeNames = WorldManager.getConfiguredWorldNames();
		List<String> inactiveNames = WorldManager.getInactiveWorldNames();
		if (activeNames.size() != inactiveNames.size()) {
			return;
		}

		String copySql =
				"INSERT INTO chests " +
						"(world, x, y, z, region_key, chest_key, type, loot_table, looted) " +
						"SELECT ?, x, y, z, region_key, chest_key, type, loot_table, looted " +
						"FROM chests WHERE world = ? " +
						"ON CONFLICT(world, chest_key) DO UPDATE SET " +
						"x = excluded.x, " +
						"y = excluded.y, " +
						"z = excluded.z, " +
						"region_key = excluded.region_key, " +
						"type = excluded.type, " +
						"loot_table = excluded.loot_table, " +
						"looted = MAX(chests.looted, excluded.looted)";
		String deleteSql = "DELETE FROM chests WHERE world = ?";

		boolean autoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);
		try (PreparedStatement copy = conn.prepareStatement(copySql);
				PreparedStatement delete = conn.prepareStatement(deleteSql)) {
			for (int i = 0; i < activeNames.size(); i++) {
				String active = activeNames.get(i);
				String inactive = inactiveNames.get(i);
				copy.setString(1, active);
				copy.setString(2, inactive);
				copy.executeUpdate();

				delete.setString(1, inactive);
				delete.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(autoCommit);
		}
	}

	/**
	 * Returns the shared database connection.
	 */
	private Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(jdbcUrl);
			try (Statement st = connection.createStatement()) {
				st.execute("PRAGMA journal_mode = WAL");
				st.execute("PRAGMA synchronous = NORMAL");
				st.execute("PRAGMA busy_timeout = 5000");
				st.execute("PRAGMA wal_autocheckpoint = 1000");
				st.execute("PRAGMA cache_size = -8192");
			}
		}
		return connection;
	}

	/**
	 * Registers or updates a single chest entry.
	 */
	public void registerChest(Location loc, ChestType type, String lootTableKey) {
		enqueueRecord(new ChestRecord(loc, type, lootTableKey));
	}

	/**
	 * Registers or updates multiple chest entries in one transaction.
	 */
	public void registerChests(Iterable<ChestRecord> records) {
		for (ChestRecord rec : records) {
			enqueueRecord(rec);
		}
	}

	private void enqueueRecord(ChestRecord rec) {
		if (rec == null || !rec.isValid()) {
			return;
		}
		while (writerRunning.get()) {
			try {
				if (writeQueue.offer(rec, QUEUE_OFFER_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
					warnIfQueueBackedUp();
					return;
				}
				warnQueueFull();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				plugin.getLogger().warning("Interrupted while queueing chest DB write");
				return;
			}
		}
	}

	private void warnIfQueueBackedUp() {
		if (writeQueue.size() >= QUEUE_WARNING_THRESHOLD) {
			warnQueueFull();
		}
	}

	private void warnQueueFull() {
		long now = System.nanoTime();
		long previous = lastQueueWarning.get();
		if (now - previous < QUEUE_WARNING_INTERVAL_NANOS || !lastQueueWarning.compareAndSet(previous, now)) {
			return;
		}
		plugin.getLogger().warning("Chest DB write queue is backed up (" + writeQueue.size()
				+ "/" + MAX_PENDING_RECORDS + "). Slowing chunk generation to protect memory.");
	}

	private void runWriter() {
		List<ChestRecord> batch = new ArrayList<>(MAX_BATCH_SIZE);
		while (writerRunning.get() || !writeQueue.isEmpty()) {
			try {
				ChestRecord first = writeQueue.poll(FLUSH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
				if (first == null) {
					continue;
				}
				batch.add(first);
				writeQueue.drainTo(batch, MAX_BATCH_SIZE - batch.size());
				writeBatch(batch);
			} catch (InterruptedException e) {
				if (writerRunning.get()) {
					Thread.currentThread().interrupt();
					break;
				}
			} finally {
				batch.clear();
			}
		}
		synchronized (dbLock) {
			try {
				checkpoint(getConnection(), true);
			} catch (SQLException e) {
				plugin.getLogger().warning("Failed to checkpoint chest database on writer shutdown: " + e.getMessage());
			}
		}
	}

	private void writeBatch(List<ChestRecord> batch) {
		if (batch.isEmpty()) {
			return;
		}

		String sql =
				"INSERT INTO chests " +
						"(world, x, y, z, region_key, chest_key, type, loot_table, looted) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0) " +
						"ON CONFLICT(world, chest_key) DO UPDATE SET " +
						"x = excluded.x, " +
						"y = excluded.y, " +
						"z = excluded.z, " +
						"region_key = excluded.region_key, " +
						"type = excluded.type, " +
						"loot_table = excluded.loot_table";

		synchronized (dbLock) {
			Connection conn = null;
			boolean autoCommit = true;
			try {
				conn = getConnection();
				autoCommit = conn.getAutoCommit();
				conn.setAutoCommit(false);
				try (PreparedStatement ps = conn.prepareStatement(sql)) {
					for (ChestRecord rec : batch) {
						if (rec == null || !rec.isValid()) {
							continue;
						}
						ps.setString(1, rec.worldName);
						ps.setInt(2, rec.x);
						ps.setInt(3, rec.y);
						ps.setInt(4, rec.z);
						ps.setLong(5, rec.regionKey);
						ps.setLong(6, rec.chestKey);
						ps.setInt(7, rec.type == ChestType.CUSTOM ? 0 : 1);
						ps.setString(8, rec.lootTableKey);
						ps.addBatch();
					}
					ps.executeBatch();
				}
				conn.commit();
				conn.setAutoCommit(autoCommit);
				try {
					checkpoint(conn, false);
				} catch (SQLException e) {
					plugin.getLogger().warning("Failed to checkpoint chest database: " + e.getMessage());
				}
			} catch (SQLException e) {
				plugin.getLogger().warning("Failed to write chest DB batch of " + batch.size() + ": " + e.getMessage());
				try {
					if (conn == null) {
						conn = getConnection();
					}
					if (!conn.getAutoCommit()) {
						conn.rollback();
						conn.setAutoCommit(autoCommit);
					}
				} catch (SQLException ignored) {
				}
			}
		}
	}

	private void checkpoint(Connection conn, boolean force) throws SQLException {
		if (!force && ++batchesSinceCheckpoint < CHECKPOINT_EVERY_BATCHES) {
			return;
		}
		batchesSinceCheckpoint = 0;
		try (Statement st = conn.createStatement()) {
			st.execute(force ? "PRAGMA wal_checkpoint(TRUNCATE)" : "PRAGMA wal_checkpoint(PASSIVE)");
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

		synchronized (dbLock) {
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

		synchronized (dbLock) {
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
	}

	/**
	 * Hook for plugin shutdown.
	 */
	public void shutdown() {
		writerRunning.set(false);
		writerThread.interrupt();
		try {
			writerThread.join(TimeUnit.SECONDS.toMillis(15));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			plugin.getLogger().warning("Interrupted while waiting for chest DB writer to stop");
		}

		synchronized (dbLock) {
			if (connection != null) {
				try {
					checkpoint(connection, true);
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	private static long getRegionKey(int blockX, int blockZ) {
		int regionX = blockX >> 9;
		int regionZ = blockZ >> 9;
		return packRegionCoords(regionX, regionZ);
	}

	/**
	 * Packs a block position into a chest key.
	 */
	private static long getChestKey(Location loc) {
		return getChestKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	private static long getChestKey(int x, int y, int z) {
		return ((long) x & 0x3FFFFFFL) << 38
				| ((long) z & 0x3FFFFFFL) << 12
				| ((long) y & 0xFFFL);
	}

	/**
	 * Packs region coordinates into a single long.
	 */
	private static long packRegionCoords(int x, int z) {
		return ((long) x << 32) | (z & 0xFFFFFFFFL);
	}
}
