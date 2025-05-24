package main;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MaterialManager {
	private final SkyGridPlugin plugin;
	private final Object2ObjectMap<String, MaterialDistribution> materialDistributions;
	private final Object2ObjectMap<String, ObjectSet<String>> worldBiomeMappings;

	public MaterialManager(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.materialDistributions = new Object2ObjectOpenHashMap<>();
		this.worldBiomeMappings = new Object2ObjectOpenHashMap<>();
		precomputeBiomeMappings();
		precomputeAllMaterialDistributions();
	}

	private void precomputeBiomeMappings() {
		worldBiomeMappings.put("world.yml", new ObjectOpenHashSet<>(Arrays.asList(Arrays.stream(WorldBiomes.values()).map(Enum::name).toArray(String[]::new))));
		worldBiomeMappings.put("world_nether.yml", new ObjectOpenHashSet<>(Arrays.asList(Arrays.stream(NetherBiomes.values()).map(Enum::name).toArray(String[]::new))));
		worldBiomeMappings.put("world_the_end.yml", new ObjectOpenHashSet<>(Arrays.asList(Arrays.stream(EndBiomes.values()).map(Enum::name).toArray(String[]::new))));
	}

	private void precomputeAllMaterialDistributions() {
		for (String fileName : worldBiomeMappings.keySet()) {
			loadMaterialsForWorld(fileName);
		}
	}

	private enum WorldBiomes {
		FROZEN_RIVER, FROZEN_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, LUKEWARM_OCEAN, COLD_OCEAN, DEEP_COLD_OCEAN,
		OCEAN, DEEP_OCEAN, RIVER, WARM_OCEAN, SWAMP, MANGROVE_SWAMP, DESERT, DARK_FOREST, OLD_GROWTH_PINE_TAIGA,
		OLD_GROWTH_SPRUCE_TAIGA, BEACH, SNOWY_BEACH, STONY_SHORE, JUNGLE, SPARSE_JUNGLE, BAMBOO_JUNGLE, JAGGED_PEAKS,
		FROZEN_PEAKS, ICE_SPIKES, STONY_PEAKS, FOREST, FLOWER_FOREST, BIRCH_FOREST, OLD_GROWTH_BIRCH_FOREST, TAIGA,
		SNOWY_TAIGA, SNOWY_PLAINS, GROVE, SNOWY_SLOPES, PLAINS, SUNFLOWER_PLAINS, MEADOW, MUSHROOM_FIELDS, CHERRY_GROVE,
		SAVANNA, SAVANNA_PLATEAU, WINDSWEPT_SAVANNA, WINDSWEPT_FOREST, WINDSWEPT_HILLS, WINDSWEPT_GRAVELLY_HILLS,
		DEEP_DARK, LUSH_CAVES, DRIPSTONE_CAVES, BADLANDS, ERODED_BADLANDS, WOODED_BADLANDS, PALE_GARDEN
	}

	private enum NetherBiomes {
		BASALT_DELTAS, CRIMSON_FOREST, NETHER_WASTES, SOUL_SAND_VALLEY, WARPED_FOREST
	}

	private enum EndBiomes {
		THE_END, END_BARRENS, END_HIGHLANDS, END_MIDLANDS, SMALL_END_ISLANDS
	}

	public void loadMaterialsForWorld(String fileName) {
		File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);
		if (!file.exists()) {
			plugin.getLogger().warning("File not found: " + file.getPath());
			return;
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		String worldName = getWorldNameFromFileName(fileName);
		boolean hasDefaultDistribution = config.contains("default_distribution");
		ConfigurationSection distributionsSection = config.getConfigurationSection("distributions");
		Object2ObjectOpenHashMap<String, MaterialDistribution> distributionCache = new Object2ObjectOpenHashMap<>();
		if (distributionsSection == null) {
			if (!hasDefaultDistribution) {
				plugin.getLogger().warning("'distributions' section not found in " + fileName);
			}
		} else {
			for (String key : distributionsSection.getKeys(false)) {
				ConfigurationSection distSection = distributionsSection.getConfigurationSection(key);
				if (distSection == null) {
					plugin.getLogger().warning("No section found for distribution key: " + key + " in file " + fileName);
					continue;
				}
				Object2DoubleMap<Material> distributionMap = new Object2DoubleOpenHashMap<>();
				for (String materialKey : distSection.getKeys(false)) {
					double percentage = distSection.getDouble(materialKey);
					Material material = Material.getMaterial(materialKey.trim().toUpperCase());
					if (material != null) {
						distributionMap.put(material, percentage);
					} else {
						plugin.getLogger().warning("Invalid material '" + materialKey
								+ "' in distribution '" + key + "' of file " + fileName);
					}
				}
				if (distributionMap.isEmpty()) {
					plugin.getLogger().warning("Empty distribution '" + key + "' in file " + fileName);
					continue;
				}
				distributionCache.put(key, new MaterialDistribution(distributionMap));
			}
		}
		ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
		if (biomesSection == null) {
			if (!hasDefaultDistribution) {
				plugin.getLogger().warning("'biomes' section not found in " + fileName);
			}
		} else {
			for (String biomeKey : biomesSection.getKeys(false)) {
				String distributionKey = biomesSection.getString(biomeKey + ".distribution");
				if (distributionKey == null) {
					plugin.getLogger().warning("No distribution defined for biome '" + biomeKey + "' in file " + fileName);
					continue;
				}
				MaterialDistribution distribution = distributionCache.get(distributionKey);
				if (distribution == null) {
					plugin.getLogger().warning("Distribution '" + distributionKey
							+ "' not found for biome '" + biomeKey + "' in file " + fileName);
					continue;
				}
				String[] biomes = biomeKey.split(",");
				for (String biome : biomes) {
					String trimmedBiome = biome.trim();
					if (trimmedBiome.isEmpty()) {
						plugin.getLogger().warning("Empty biome name in key: " + biomeKey + " in file " + fileName);
						continue;
					}
					String alias = worldName + "-" + trimmedBiome;
					setMaterialDistribution(alias, distribution);
				}
			}
		}
		if (config.contains("default_distribution")) {
			ConfigurationSection defaultDistSection = config.getConfigurationSection("default_distribution");
			if (defaultDistSection != null) {
				Object2DoubleMap<Material> defaultMap = new Object2DoubleOpenHashMap<>();
				for (String materialKey : defaultDistSection.getKeys(false)) {
					double percentage = defaultDistSection.getDouble(materialKey);
					Material material = Material.getMaterial(materialKey.trim().toUpperCase());
					if (material != null) {
						defaultMap.put(material, percentage);
					} else {
						plugin.getLogger().warning("Invalid material '" + materialKey
								+ "' in default_distribution of file " + fileName);
					}
				}
				if (!defaultMap.isEmpty()) {
					MaterialDistribution defaultDistribution = new MaterialDistribution(defaultMap);
					setMaterialDistribution(worldName + "-DEFAULT", defaultDistribution);
				} else {
					plugin.getLogger().warning("Default distribution is empty in file " + fileName);
				}
			}
		}
	}

	private String getWorldNameFromFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private void setMaterialDistribution(String alias, MaterialDistribution materialDistribution) {
		materialDistributions.put(alias, materialDistribution);
	}

	public Material getRandomMaterialForWorld(String worldName, String biomeName) {
		// 1) exact match: skygridx_world_THE_BIOME
		String key = worldName + "-" + biomeName;
		MaterialDistribution dist = materialDistributions.get(key);
		if (dist != null) {
			return dist.next();
		}

		// 2) if prefixed, try without prefix: world_THE_BIOME
		String base = worldName.startsWith("skygridx_")
				? worldName.substring("skygridx_".length())
						: worldName;
		String baseKey = base + "-" + biomeName;
		dist = materialDistributions.get(baseKey);
		if (dist != null) {
			return dist.next();
		}

		// 3) exact default for prefixed
		String defaultKey = worldName + "-DEFAULT";
		dist = materialDistributions.get(defaultKey);
		if (dist != null) {
			return dist.next();
		}

		// 4) base default
		String baseDefaultKey = base + "-DEFAULT";
		dist = materialDistributions.get(baseDefaultKey);
		if (dist != null) {
			return dist.next();
		}

		plugin.getLogger().warning(
				"No material distribution found for alias: " + key +
				" and no default distribution available."
				);
		return null;
	}

	static class MaterialDistribution {
		private final Material[] materials;
		private final double[] probabilities;
		private final int[] alias;
		private final int size;

		public MaterialDistribution(Object2DoubleMap<Material> distribution) {
			this.size = distribution.size();
			this.materials = new Material[size];
			this.probabilities = new double[size];
			this.alias = new int[size];
			double total = 0.0;
			int index = 0;
			Material[] tempMaterials = new Material[size];
			double[] normalized = new double[size];

			for (Object2DoubleMap.Entry<Material> entry :
				((Object2DoubleMap.FastEntrySet<Material>)distribution.object2DoubleEntrySet())) {
				total += entry.getDoubleValue();
			}
			for (Object2DoubleMap.Entry<Material> entry :
				((Object2DoubleMap.FastEntrySet<Material>)distribution.object2DoubleEntrySet())) {
				tempMaterials[index] = entry.getKey();
				normalized[index] = (entry.getDoubleValue() / total) * size;
				index++;
			}

			IntArrayList small = new IntArrayList();
			IntArrayList large = new IntArrayList();
			for (int i = 0; i < size; i++) {
				if (normalized[i] < 1.0) small.add(i);
				else large.add(i);
			}

			while (!small.isEmpty() && !large.isEmpty()) {
				int smallIndex = small.removeInt(small.size() - 1);
				int largeIndex = large.removeInt(large.size() - 1);
				probabilities[smallIndex] = normalized[smallIndex];
				alias[smallIndex] = largeIndex;
				normalized[largeIndex] = (normalized[largeIndex] + normalized[smallIndex]) - 1.0;
				if (normalized[largeIndex] < 1.0) small.add(largeIndex);
				else large.add(largeIndex);
			}
			while (!small.isEmpty()) {
				probabilities[small.removeInt(small.size() - 1)] = 1.0;
			}
			while (!large.isEmpty()) {
				probabilities[large.removeInt(large.size() - 1)] = 1.0;
			}

			System.arraycopy(tempMaterials, 0, materials, 0, size);
		}

		public Material next() {
			int column = ThreadLocalRandom.current().nextInt(size);
			return ThreadLocalRandom.current().nextDouble() < probabilities[column]
					? materials[column]
							: materials[alias[column]];
		}
	}
}