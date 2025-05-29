package main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomBedManager {
	private static CustomBedManager instance;

	private final Plugin plugin;
	private final File bedFile;
	private final Map<UUID, Location> bedSpawns = new HashMap<>();

	public CustomBedManager(Plugin plugin) {
		instance = this;
		this.plugin = plugin;
		this.bedFile = new File(plugin.getDataFolder(), "custom_beds.yml");
		if (!bedFile.exists()) {
			bedFile.getParentFile().mkdirs();
			try {
				bedFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void reloadBeds() {
		CustomBedManager inst = getInstance();
		if (inst != null) {
			inst.loadBeds();
		}
	}

	public void loadBeds() {
		synchronized (bedSpawns) {
			bedSpawns.clear();
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(bedFile);
		for (String key : config.getKeys(false)) {
			UUID uuid;
			try {
				uuid = UUID.fromString(key);
			} catch (IllegalArgumentException ex) {
				continue;
			}
			String worldName = config.getString(key + ".world");
			World world = Bukkit.getWorld(worldName);
			int x = config.getInt(key + ".x");
			int y = config.getInt(key + ".y");
			int z = config.getInt(key + ".z");
			Location loc = new Location(world, x, y, z);
			synchronized (bedSpawns) {
				bedSpawns.put(uuid, loc);
			}
		}
	}

	private synchronized void saveSnapshot() {
		Map<UUID, Location> snapshot = new HashMap<>(bedSpawns);
		var config = new YamlConfiguration();
		for (var entry : snapshot.entrySet()) {
			UUID uuid = entry.getKey();
			Location loc = entry.getValue();
			World world = loc.getWorld();
			if (!world.getName().startsWith(WorldManager.PREFIX)
					|| world.getEnvironment() == World.Environment.NETHER
					|| world.getEnvironment() == World.Environment.THE_END) {
				continue;
			}
			String key = uuid.toString();
			config.set(key + ".world", world.getName());
			config.set(key + ".x", loc.getBlockX());
			config.set(key + ".y", loc.getBlockY());
			config.set(key + ".z", loc.getBlockZ());
		}
		try {
			config.save(bedFile);
		} catch (IOException e) {
			plugin.getLogger().severe("Could not save custom_beds.yml");
			e.printStackTrace();
		}
	}


	public void setCustomBed(Player player, Location loc) {
		World world = loc.getWorld();
		if (world.getName().startsWith(WorldManager.PREFIX)
				&& world.getEnvironment() != World.Environment.NETHER
				&& world.getEnvironment() != World.Environment.THE_END) {
			synchronized (bedSpawns) {
				bedSpawns.put(player.getUniqueId(), loc);
			}
			saveSnapshot();
		}
	}

	public void removeCustomBed(Player player) {
		removeCustomBed(player.getUniqueId());
	}

	public void removeCustomBed(UUID uuid) {
		synchronized (bedSpawns) {
			bedSpawns.remove(uuid);
		}
		saveSnapshot();
	}

	public Location getCustomBed(UUID uuid) {
		synchronized (bedSpawns) {
			return bedSpawns.get(uuid);
		}
	}

	public boolean hasCustomBed(UUID uuid) {
		synchronized (bedSpawns) {
			return bedSpawns.containsKey(uuid);
		}
	}

	public Map<UUID, Location> getAllBeds() {
		synchronized (bedSpawns) {
			return new HashMap<>(bedSpawns);
		}
	}

	public static CustomBedManager getInstance() {
		return instance;
	}
}
