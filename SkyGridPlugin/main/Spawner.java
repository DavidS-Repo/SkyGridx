package main;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Spawner {
	private static Spawner instance;
	private FileConfiguration spawnerSettings;
	private static JavaPlugin plugin;

	private static final int DEFAULT_DELAY = -1;
	private static final int DEFAULT_MAX_NEARBY_ENTITIES = 16;
	private static final int DEFAULT_MAX_SPAWN_DELAY = 800;
	private static final int DEFAULT_MIN_SPAWN_DELAY = 200;
	private static final int DEFAULT_PLAYER_RANGE = 16;
	private static final int DEFAULT_SPAWN_COUNT = 4;
	private static final int DEFAULT_SPAWN_RANGE = 4;

	private static final Set<EntityType> DEFAULT_WORLD_ENTITIES;
	private static final Set<EntityType> DEFAULT_NETHER_ENTITIES;
	private static final Set<EntityType> DEFAULT_END_ENTITIES;

	private final Object2ObjectOpenHashMap<String, ObjectOpenHashSet<String>> biomeCache = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<String, ObjectArrayList<EntityType>> defaultEntitiesCache = new Object2ObjectOpenHashMap<>();

	static {
		DEFAULT_WORLD_ENTITIES = EnumSet.of(EntityType.SKELETON, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER);
		DEFAULT_NETHER_ENTITIES = EnumSet.of(EntityType.BLAZE, EntityType.WITHER_SKELETON);
		DEFAULT_END_ENTITIES = EnumSet.of(EntityType.ENDERMAN, EntityType.SHULKER);
	}

	private Spawner(JavaPlugin plugin) {
		Spawner.plugin = plugin;
		loadSpawnerSettings();
		cacheDefaultEntities();
	}

	public static Spawner getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new Spawner(plugin);
		}
		return instance;
	}

	public void BlockInfo(Block block) {
	    if (block == null || block.getType() != Material.SPAWNER) {
	        return;
	    }

	    String biomeName = block.getBiome().getKey().toString().intern();
	    if (spawnerSettings != null && spawnerSettings.isConfigurationSection("SpawnerSettings")) {
	        ConfigurationSection spawnerSettingsSection = spawnerSettings.getConfigurationSection("SpawnerSettings");

	        for (String spawnerKey : spawnerSettingsSection.getKeys(false)) {
	            ConfigurationSection spawnerSection = spawnerSettingsSection.getConfigurationSection(spawnerKey);
	            ObjectOpenHashSet<String> biomes = biomeCache.computeIfAbsent(spawnerKey, key -> new ObjectOpenHashSet<>(spawnerSection.getStringList("Biomes")));
	            if (biomes.contains(biomeName)) {
	                String entityName = getEntityForBiome(spawnerSection);
	                if (entityName != null) {
	                    setSpawnerSettings((CreatureSpawner) block.getState(), spawnerSection, entityName);
	                    return;
	                }
	            }
	        }
	    }
	    setDefaultSpawnerSettings((CreatureSpawner) block.getState(), block.getWorld());
	}

	private void setSpawnerSettings(CreatureSpawner spawner, ConfigurationSection spawnerSection, String entityName) {
		spawner.setDelay(spawnerSection.getInt("Delay", DEFAULT_DELAY));
		spawner.setMaxNearbyEntities(spawnerSection.getInt("MaxNearbyEntities", DEFAULT_MAX_NEARBY_ENTITIES));
		spawner.setMaxSpawnDelay(spawnerSection.getInt("MaxSpawnDelay", DEFAULT_MAX_SPAWN_DELAY));
		spawner.setMinSpawnDelay(spawnerSection.getInt("MinSpawnDelay", DEFAULT_MIN_SPAWN_DELAY));
		spawner.setRequiredPlayerRange(spawnerSection.getInt("PlayerRange", DEFAULT_PLAYER_RANGE));
		spawner.setSpawnCount(spawnerSection.getInt("SpawnCount", DEFAULT_SPAWN_COUNT));
		spawner.setSpawnRange(spawnerSection.getInt("SpawnRange", DEFAULT_SPAWN_RANGE));
		spawner.setSpawnedType(EntityType.valueOf(entityName));
		spawner.update();
	}

	private void setDefaultSpawnerSettings(CreatureSpawner spawner, World world) {
		spawner.setDelay(DEFAULT_DELAY);
		spawner.setMaxNearbyEntities(DEFAULT_MAX_NEARBY_ENTITIES);
		spawner.setMaxSpawnDelay(DEFAULT_MAX_SPAWN_DELAY);
		spawner.setMinSpawnDelay(DEFAULT_MIN_SPAWN_DELAY);
		spawner.setRequiredPlayerRange(DEFAULT_PLAYER_RANGE);
		spawner.setSpawnCount(DEFAULT_SPAWN_COUNT);
		spawner.setSpawnRange(DEFAULT_SPAWN_RANGE);
		spawner.setSpawnedType(getDefaultEntityTypeForWorld(world));
		spawner.update();
	}

	private EntityType getDefaultEntityTypeForWorld(World world) {
		ObjectArrayList<EntityType> entities = defaultEntitiesCache.get(world.getName());
		if (entities == null || entities.isEmpty()) {
			return EntityType.SKELETON;
		}
		return entities.get(ThreadLocalRandom.current().nextInt(entities.size()));
	}

	private String getEntityForBiome(ConfigurationSection spawnerSection) {
		if (spawnerSection.isList("Entities")) {
			List<String> entityList = spawnerSection.getStringList("Entities");
			return chooseRandomEntityWithWeights(entityList);
		}
		return null;
	}

	private String chooseRandomEntityWithWeights(List<String> entityList) {
		if (entityList.isEmpty()) {
			return null;
		}

		String[] entityNames = new String[entityList.size()];
		double[] cumulativeWeights = new double[entityList.size()];
		double totalWeight = 0.0;

		for (int i = 0; i < entityList.size(); i++) {
			String[] parts = entityList.get(i).split(":");
			if (parts.length == 2) {
				entityNames[i] = parts[0];
				double weight = Double.parseDouble(parts[1]);
				totalWeight += weight;
				cumulativeWeights[i] = totalWeight;
			}
		}

		double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
		for (int i = 0; i < cumulativeWeights.length; i++) {
			if (randomValue <= cumulativeWeights[i]) {
				return entityNames[i];
			}
		}
		return null;
	}

	private void loadSpawnerSettings() {
		File spawnerSettingsFile = new File(plugin.getDataFolder(), "SkygridBlocks/SpawnerSettings.yml");
		spawnerSettings = YamlConfiguration.loadConfiguration(spawnerSettingsFile);
		plugin.getLogger().info("Custom Skygrid generation started.");
		plugin.getLogger().info("SpawnerSettings Loaded");
	}

	private void cacheDefaultEntities() {
		defaultEntitiesCache.put("world", new ObjectArrayList<>(DEFAULT_WORLD_ENTITIES));
		defaultEntitiesCache.put("world_nether", new ObjectArrayList<>(DEFAULT_NETHER_ENTITIES));
		defaultEntitiesCache.put("world_the_end", new ObjectArrayList<>(DEFAULT_END_ENTITIES));
	}

	static class EntityWeight {
		String entityName;
		double weight;

		EntityWeight(String entityName, double weight) {
			this.entityName = entityName;
			this.weight = weight;
		}
	}
}