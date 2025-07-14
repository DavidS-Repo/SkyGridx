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

/**
 * Manages storage of player respawn anchor locations
 */
public class CustomAnchorManager {
    private static CustomAnchorManager instance;
    private final Plugin plugin;
    private final File anchorFile;
    private final Map<UUID, Location> anchorSpawns = new HashMap<>();

    public CustomAnchorManager(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.anchorFile = new File(plugin.getDataFolder(), "custom_anchors.yml");
        if (!anchorFile.exists()) {
            anchorFile.getParentFile().mkdirs();
            try {
                anchorFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadAnchors();
    }

    public static CustomAnchorManager getInstance() {
        return instance;
    }

    /**
     * Load stored anchor locations from disk
     */
    public void loadAnchors() {
        synchronized (anchorSpawns) {
            anchorSpawns.clear();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(anchorFile);
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
            synchronized (anchorSpawns) {
                anchorSpawns.put(uuid, loc);
            }
        }
    }

    /**
     * Save current anchor map to disk
     */
    private void saveSnapshot() {
        Map<UUID, Location> snapshot = new HashMap<>(anchorSpawns);
        YamlConfiguration config = new YamlConfiguration();
        for (var entry : snapshot.entrySet()) {
            UUID uuid = entry.getKey();
            Location loc = entry.getValue();
            World world = loc.getWorld();
            if (!world.getName().startsWith(WorldManager.PREFIX)) continue;
            String key = uuid.toString();
            config.set(key + ".world", world.getName());
            config.set(key + ".x", loc.getBlockX());
            config.set(key + ".y", loc.getBlockY());
            config.set(key + ".z", loc.getBlockZ());
        }
        try {
            config.save(anchorFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save custom_anchors.yml");
            e.printStackTrace();
        }
    }

    /**
     * Record a new respawn anchor for player
     */
    public void setCustomAnchor(Player player, Location loc) {
        World world = loc.getWorld();
        if (world.getName().startsWith(WorldManager.PREFIX)) {
            synchronized (anchorSpawns) {
                anchorSpawns.put(player.getUniqueId(), loc);
            }
            saveSnapshot();
        }
    }

    /**
     * Remove stored anchor for player
     */
    public void removeCustomAnchor(UUID uuid) {
        synchronized (anchorSpawns) {
            anchorSpawns.remove(uuid);
        }
        saveSnapshot();
    }

    /**
     * Check if player has a stored anchor
     */
    public boolean hasCustomAnchor(UUID uuid) {
        synchronized (anchorSpawns) {
            return anchorSpawns.containsKey(uuid);
        }
    }

    /**
     * Get stored anchor location for player
     */
    public Location getCustomAnchor(UUID uuid) {
        synchronized (anchorSpawns) {
            return anchorSpawns.get(uuid);
        }
    }

    /**
     * Get all stored anchors
     */
    public Map<UUID, Location> getAllAnchors() {
        synchronized (anchorSpawns) {
            return new HashMap<>(anchorSpawns);
        }
    }
}
