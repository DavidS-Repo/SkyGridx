package main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomBedManager {
	private final File bedFile;
	private static CustomBedManager instance;
	private final FileConfiguration bedData;
	private final Map<UUID, Location> bedSpawns = new HashMap<>();

	public CustomBedManager(Plugin plugin) {
		instance = this;
		this.bedFile = new File(plugin.getDataFolder(), "custom_beds.yml");
		if (!bedFile.exists()) {
			try {
				bedFile.getParentFile().mkdirs();
				bedFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.bedData = YamlConfiguration.loadConfiguration(bedFile);
		loadBeds();
	}

	private void loadBeds() {
		for (String key : bedData.getKeys(false)) {
			UUID uuid = UUID.fromString(key);
			World world = Bukkit.getWorld(bedData.getString(key + ".world"));
			double x = bedData.getDouble(key + ".x");
			double y = bedData.getDouble(key + ".y");
			double z = bedData.getDouble(key + ".z");

			// Only load beds if world is a custom world
			if (world != null && world.getName().startsWith(WorldManager.PREFIX)) {
				bedSpawns.put(uuid, new Location(world, x, y, z));
			}
		}
	}

	private void saveBeds() {
		// Clear old data
		for (String key : bedData.getKeys(false)) {
			bedData.set(key, null);
		}

		// Save only beds in custom worlds
		for (Map.Entry<UUID, Location> entry : bedSpawns.entrySet()) {
			Location loc = entry.getValue();
			if (loc.getWorld().getName().startsWith(WorldManager.PREFIX)) {
				String uuidStr = entry.getKey().toString();
				bedData.set(uuidStr + ".world", loc.getWorld().getName());
				bedData.set(uuidStr + ".x", loc.getX());
				bedData.set(uuidStr + ".y", loc.getY());
				bedData.set(uuidStr + ".z", loc.getZ());
			}
		}

		try {
			bedData.save(bedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCustomBed(Player player, Location loc) {
		if (loc.getWorld().getName().startsWith(WorldManager.PREFIX)) {
			bedSpawns.put(player.getUniqueId(), loc);
			saveBeds();
		}
	}

	public void removeCustomBed(Player player) {
		bedSpawns.remove(player.getUniqueId());
		bedData.set(player.getUniqueId().toString(), null);
		saveBeds();
	}

	public void removeCustomBed(UUID uuid) {
		bedSpawns.remove(uuid);
		bedData.set(uuid.toString(), null);
		saveBeds();
	}

	public Location getCustomBed(UUID uuid) {
		return bedSpawns.get(uuid);
	}

	public boolean hasCustomBed(UUID uuid) {
		return bedSpawns.containsKey(uuid);
	}

	public Map<UUID, Location> getAllBeds() {
		return new HashMap<>(bedSpawns);
	}

	public static CustomBedManager getInstance() {
		return instance;
	}

	public Location getCustomBedLocation(Player player) {
		return bedSpawns.get(player.getUniqueId());
	}
}
