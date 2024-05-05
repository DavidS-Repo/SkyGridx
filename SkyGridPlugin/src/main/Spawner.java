package main;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
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
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Spawner {
	private static Spawner instance;
	private FileConfiguration spawnerSettings;
	private Random random;
	private static JavaPlugin plugin;
	private Spawner(JavaPlugin plugin) {
		Spawner.plugin = plugin;
		this.random = new Random();
		loadSpawnerSettings();
	}

	public static Spawner getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new Spawner(plugin);
		}
		return instance;
	}

	public void BlockInfo(Block block) {
		if (block != null && block.getType() == Material.SPAWNER) {
			String biomeName = getBiomeNameFromBlock(block);
			if (spawnerSettings != null && spawnerSettings.isConfigurationSection("SpawnerSettings")) {
				ConfigurationSection spawnerSettingsSection = spawnerSettings.getConfigurationSection("SpawnerSettings");

				for (String spawnerKey : spawnerSettingsSection.getKeys(false)) {
					ConfigurationSection spawnerSection = spawnerSettingsSection.getConfigurationSection(spawnerKey);
					List<String> biomes = spawnerSection.getStringList("Biomes");
					if (biomes.contains(biomeName)) {
						String entityName = getEntityForBiome(spawnerSection);
						if (entityName != null) {
							int delay = spawnerSection.getInt("Delay", -1);
							int maxNearbyEntities = spawnerSection.getInt("MaxNearbyEntities", 16);
							int maxSpawnDelay = spawnerSection.getInt("MaxSpawnDelay", 800);
							int minSpawnDelay = spawnerSection.getInt("MinSpawnDelay", 200);
							int playerRange = spawnerSection.getInt("PlayerRange", 16);
							int spawnCount = spawnerSection.getInt("SpawnCount", 4);
							int spawnRange = spawnerSection.getInt("SpawnRange", 4);

							CreatureSpawner spawner = (CreatureSpawner) block.getState();
							spawner.setDelay(delay);
							spawner.setMaxNearbyEntities(maxNearbyEntities);
							spawner.setMaxSpawnDelay(maxSpawnDelay);
							spawner.setMinSpawnDelay(minSpawnDelay);
							spawner.setRequiredPlayerRange(playerRange);
							spawner.setSpawnCount(spawnCount);
							spawner.setSpawnRange(spawnRange);
							spawner.setSpawnedType(EntityType.valueOf(entityName));
							spawner.update();
							return;
						}
					}
				}
			}
			setDefaultSpawnerSettings(block);
		}
	}

	private void setDefaultSpawnerSettings(Block block) {
		int defaultDelay = -1;
		int defaultMaxNearbyEntities = 16;
		int defaultMaxSpawnDelay = 800;
		int defaultMinSpawnDelay = 200;
		int defaultPlayerRange = 16;
		int defaultSpawnCount = 4;
		int defaultSpawnRange = 4;

		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		spawner.setDelay(defaultDelay);
		spawner.setMaxNearbyEntities(defaultMaxNearbyEntities);
		spawner.setMaxSpawnDelay(defaultMaxSpawnDelay);
		spawner.setMinSpawnDelay(defaultMinSpawnDelay);
		spawner.setRequiredPlayerRange(defaultPlayerRange);
		spawner.setSpawnCount(defaultSpawnCount);
		spawner.setSpawnRange(defaultSpawnRange);

		EntityType defaultEntityType = getDefaultEntityTypeForWorld(block.getWorld());
		spawner.setSpawnedType(defaultEntityType);

		spawner.update();
	}

	private EntityType getDefaultEntityTypeForWorld(World world) {
		String worldName = world.getName();
		List<EntityType> entities;

		switch (worldName) {
		case "world":
			entities = Arrays.asList(EntityType.SKELETON, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER);
			break;
		case "world_nether":
			entities = Arrays.asList(EntityType.BLAZE, EntityType.WITHER_SKELETON);
			break;
		case "world_the_end":
			entities = Arrays.asList(EntityType.ENDERMAN, EntityType.SHULKER);
			break;
		default:
			entities = Collections.emptyList();
		}

		if (!entities.isEmpty()) {
			int randomIndex = new Random().nextInt(entities.size());
			return entities.get(randomIndex);
		} else {
			return EntityType.SKELETON;
		}
	}

	private String getBiomeNameFromBlock(Block block) {
		if (block != null) {
			Biome biome = block.getBiome();
			String biomeName = biome.name();
			return biomeName;
		}
		return null;
	}

	private String getEntityForBiome(ConfigurationSection spawnerSection) {
		if (spawnerSection.isList("Entities")) {
			List<String> entityList = spawnerSection.getStringList("Entities");
			String chosenEntity = chooseRandomEntityWithWeights(entityList);
			return chosenEntity;
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
		double randomValue = random.nextDouble() * totalWeight;
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
			InputStream inputStream = plugin.getResource("SpawnerSettings.yml");
			if (inputStream != null) {
				try {
					InputStreamReader reader = new InputStreamReader(inputStream);
					char[] buffer = new char[1024];
					StringBuilder builder = new StringBuilder();
					int bytesRead;
					while ((bytesRead = reader.read(buffer)) != -1) {
						builder.append(buffer, 0, bytesRead);
					}
					reader.close();
					String yamlContent = builder.toString();
					Files.write(spawnerSettingsFile.toPath(), yamlContent.getBytes());

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				plugin.getLogger().warning("SpawnerSettings.yml not found in the JAR.");
			}
		}
		spawnerSettings = YamlConfiguration.loadConfiguration(spawnerSettingsFile);
		plugin.getLogger().info("Custom Skygrid generation started.");
		plugin.getLogger().info("SpawnerSettings Loaded");
	}
}