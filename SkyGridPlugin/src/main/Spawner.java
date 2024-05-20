package main;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
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

	private final Map<String, Set<String>> biomeCache = new HashMap<>();
	private final Map<String, List<EntityType>> defaultEntitiesCache = new HashMap<>();

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

		String biomeName = getBiomeNameFromBlock(block);
		if (spawnerSettings != null && spawnerSettings.isConfigurationSection("SpawnerSettings")) {
			ConfigurationSection spawnerSettingsSection = spawnerSettings.getConfigurationSection("SpawnerSettings");

			for (String spawnerKey : spawnerSettingsSection.getKeys(false)) {
				ConfigurationSection spawnerSection = spawnerSettingsSection.getConfigurationSection(spawnerKey);
				Set<String> biomes = biomeCache.computeIfAbsent(spawnerKey, key -> new HashSet<>(spawnerSection.getStringList("Biomes")));
				if (biomes.contains(biomeName)) {
					String entityName = getEntityForBiome(spawnerSection);
					if (entityName != null) {
						setSpawnerSettings(block, spawnerSection, entityName);
						return;
					}
				}
			}
		}
		setDefaultSpawnerSettings(block);
	}

	private void setSpawnerSettings(Block block, ConfigurationSection spawnerSection, String entityName) {
		CreatureSpawner spawner = (CreatureSpawner) block.getState();
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

	private void setDefaultSpawnerSettings(Block block) {
		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		applyDefaultSpawnerSettings(spawner);
		spawner.setSpawnedType(getDefaultEntityTypeForWorld(block.getWorld()));
		spawner.update();
	}

	private void applyDefaultSpawnerSettings(CreatureSpawner spawner) {
		spawner.setDelay(DEFAULT_DELAY);
		spawner.setMaxNearbyEntities(DEFAULT_MAX_NEARBY_ENTITIES);
		spawner.setMaxSpawnDelay(DEFAULT_MAX_SPAWN_DELAY);
		spawner.setMinSpawnDelay(DEFAULT_MIN_SPAWN_DELAY);
		spawner.setRequiredPlayerRange(DEFAULT_PLAYER_RANGE);
		spawner.setSpawnCount(DEFAULT_SPAWN_COUNT);
		spawner.setSpawnRange(DEFAULT_SPAWN_RANGE);
	}

	private EntityType getDefaultEntityTypeForWorld(World world) {
		List<EntityType> entities = defaultEntitiesCache.get(world.getName());
		if (entities == null || entities.isEmpty()) {
			return EntityType.SKELETON;
		}
		int randomIndex = ThreadLocalRandom.current().nextInt(entities.size());
		return entities.get(randomIndex);
	}

	private String getBiomeNameFromBlock(Block block) {
		return block != null ? block.getBiome().name() : null;
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

		List<EntityWeight> weightedEntities = new ArrayList<>();
		double totalWeight = 0.0;
		for (String entity : entityList) {
			String[] parts = entity.split(":");
			if (parts.length == 2) {
				String entityName = parts[0];
				double weight = Double.parseDouble(parts[1]);
				totalWeight += weight;
				weightedEntities.add(new EntityWeight(entityName, totalWeight));
			}
		}

		double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
		for (EntityWeight entityWeight : weightedEntities) {
			if (randomValue <= entityWeight.weight) {
				return entityWeight.entityName;
			}
		}
		return null;
	}

	private static class EntityWeight {
		String entityName;
		double weight;

		EntityWeight(String entityName, double weight) {
			this.entityName = entityName;
			this.weight = weight;
		}
	}

	private void loadSpawnerSettings() {
		File spawnerSettingsFile = new File(plugin.getDataFolder(), "SkygridBlocks/SpawnerSettings.yml");
		if (!spawnerSettingsFile.exists()) {
			try (InputStream inputStream = plugin.getResource("SpawnerSettings.yml")) {
				if (inputStream != null) {
					Files.copy(inputStream, spawnerSettingsFile.toPath());
				} else {
					plugin.getLogger().warning("SpawnerSettings.yml not found in the JAR.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		spawnerSettings = YamlConfiguration.loadConfiguration(spawnerSettingsFile);
		plugin.getLogger().info("Custom Skygrid generation started.");
		plugin.getLogger().info("SpawnerSettings Loaded");
	}

	private void cacheDefaultEntities() {
		defaultEntitiesCache.put("world", Arrays.asList(EntityType.SKELETON, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER));
		defaultEntitiesCache.put("world_nether", Arrays.asList(EntityType.BLAZE, EntityType.WITHER_SKELETON));
		defaultEntitiesCache.put("world_the_end", Arrays.asList(EntityType.ENDERMAN, EntityType.SHULKER));
	}
}
