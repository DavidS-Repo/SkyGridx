package main;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
	private static JavaPlugin plugin;
	private FileConfiguration spawnerSettings;

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

	private final Object2ObjectOpenHashMap<String, SpawnerData> biomeToSpawnerData = new Object2ObjectOpenHashMap<>();
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
		preprocessSpawnerData();
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
		String biomeName = block.getBiome().toString();
		SpawnerData data = biomeToSpawnerData.get(biomeName);
		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		if (data != null) {
			EntityType entity = data.getRandomEntity();
			spawner.setDelay(data.delay);
			spawner.setMaxNearbyEntities(data.maxNearbyEntities);
			spawner.setMaxSpawnDelay(data.maxSpawnDelay);
			spawner.setMinSpawnDelay(data.minSpawnDelay);
			spawner.setRequiredPlayerRange(data.playerRange);
			spawner.setSpawnCount(data.spawnCount);
			spawner.setSpawnRange(data.spawnRange);
			spawner.setSpawnedType(entity);
			spawner.update();
		} else {
			setDefaultSpawnerSettings(spawner, block.getWorld());
		}
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

	private void preprocessSpawnerData() {
		if (!spawnerSettings.isConfigurationSection("SpawnerSettings")) {
			return;
		}
		ConfigurationSection spawnerSettingsSection = spawnerSettings.getConfigurationSection("SpawnerSettings");
		if (spawnerSettingsSection == null) {
			return;
		}
		for (String spawnerKey : spawnerSettingsSection.getKeys(false)) {
			ConfigurationSection spawnerSection = spawnerSettingsSection.getConfigurationSection(spawnerKey);
			if (spawnerSection == null) continue;
			int delay = spawnerSection.getInt("Delay", DEFAULT_DELAY);
			int maxNearby = spawnerSection.getInt("MaxNearbyEntities", DEFAULT_MAX_NEARBY_ENTITIES);
			int maxSpawnDelay = spawnerSection.getInt("MaxSpawnDelay", DEFAULT_MAX_SPAWN_DELAY);
			int minSpawnDelay = spawnerSection.getInt("MinSpawnDelay", DEFAULT_MIN_SPAWN_DELAY);
			int playerRange = spawnerSection.getInt("PlayerRange", DEFAULT_PLAYER_RANGE);
			int spawnCount = spawnerSection.getInt("SpawnCount", DEFAULT_SPAWN_COUNT);
			int spawnRange = spawnerSection.getInt("SpawnRange", DEFAULT_SPAWN_RANGE);
			List<String> entityList = spawnerSection.getStringList("Entities");
			SpawnerData data = parseSpawnerData(entityList, delay, maxNearby, maxSpawnDelay, minSpawnDelay, playerRange, spawnCount, spawnRange);
			List<String> biomes = spawnerSection.getStringList("Biomes");
			for (String biome : biomes) {
				biomeToSpawnerData.put(biome, data);
			}
		}
	}

	private SpawnerData parseSpawnerData(List<String> entityList, int delay, int maxNearby, int maxSpawnDelay, int minSpawnDelay,
			int playerRange, int spawnCount, int spawnRange) {
		if (entityList == null || entityList.isEmpty()) {
			return new SpawnerData(delay, maxNearby, maxSpawnDelay, minSpawnDelay, playerRange, spawnCount, spawnRange,
					new EntityType[0], new double[0]);
		}
		EntityType[] entityTypes = new EntityType[entityList.size()];
		double[] cumulativeWeights = new double[entityList.size()];
		double totalWeight = 0.0;
		int idx = 0;
		for (String entry : entityList) {
			String[] parts = entry.split(":");
			if (parts.length == 2) {
				try {
					EntityType type = EntityType.valueOf(parts[0]);
					double weight = Double.parseDouble(parts[1]);
					totalWeight += weight;
					cumulativeWeights[idx] = totalWeight;
					entityTypes[idx] = type;
					idx++;
				} catch (IllegalArgumentException e) {
				}
			}
		}
		if (idx < entityTypes.length) {
			EntityType[] truncatedTypes = new EntityType[idx];
			double[] truncatedWeights = new double[idx];
			System.arraycopy(entityTypes, 0, truncatedTypes, 0, idx);
			System.arraycopy(cumulativeWeights, 0, truncatedWeights, 0, idx);
			entityTypes = truncatedTypes;
			cumulativeWeights = truncatedWeights;
		}
		return new SpawnerData(delay, maxNearby, maxSpawnDelay, minSpawnDelay, playerRange, spawnCount, spawnRange, entityTypes, cumulativeWeights);
	}

	private static class SpawnerData {
		final int delay;
		final int maxNearbyEntities;
		final int maxSpawnDelay;
		final int minSpawnDelay;
		final int playerRange;
		final int spawnCount;
		final int spawnRange;
		final EntityType[] entityTypes;
		final double[] cumulativeWeights;
		final double totalWeight;
		SpawnerData(int delay, int maxNearbyEntities, int maxSpawnDelay, int minSpawnDelay,
				int playerRange, int spawnCount, int spawnRange, EntityType[] entityTypes, double[] cumulativeWeights) {
			this.delay = delay;
			this.maxNearbyEntities = maxNearbyEntities;
			this.maxSpawnDelay = maxSpawnDelay;
			this.minSpawnDelay = minSpawnDelay;
			this.playerRange = playerRange;
			this.spawnCount = spawnCount;
			this.spawnRange = spawnRange;
			this.entityTypes = entityTypes;
			this.cumulativeWeights = cumulativeWeights;
			this.totalWeight = (cumulativeWeights.length > 0) ? cumulativeWeights[cumulativeWeights.length - 1] : 0.0;
		}

		EntityType getRandomEntity() {
			if (entityTypes.length == 0) {
				return EntityType.SKELETON;
			}
			double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
			int low = 0;
			int high = cumulativeWeights.length - 1;
			while (low < high) {
				int mid = (low + high) >>> 1;
				if (cumulativeWeights[mid] < randomValue) {
					low = mid + 1;
				} else {
					high = mid;
				}
			}
			return entityTypes[low];
		}
	}
}
