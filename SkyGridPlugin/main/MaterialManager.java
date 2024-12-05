package main;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Material;

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
		worldBiomeMappings.put("world.txt", new ObjectOpenHashSet<>(Arrays.stream(WorldBiomes.values()).map(Enum::name).collect(Collectors.toSet())));
		worldBiomeMappings.put("world_nether.txt", new ObjectOpenHashSet<>(Arrays.stream(NetherBiomes.values()).map(Enum::name).collect(Collectors.toSet())));
		worldBiomeMappings.put("world_the_end.txt", new ObjectOpenHashSet<>(Arrays.stream(EndBiomes.values()).map(Enum::name).collect(Collectors.toSet())));
	}

	private void precomputeAllMaterialDistributions() {
		for (String fileName : worldBiomeMappings.keySet()) {
			loadMaterialsForWorld(fileName);
		}
	}

	private static enum WorldBiomes {
		FROZEN_RIVER, FROZEN_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, LUKEWARM_OCEAN, COLD_OCEAN, DEEP_COLD_OCEAN,
		OCEAN, DEEP_OCEAN, RIVER, WARM_OCEAN, SWAMP, MANGROVE_SWAMP, DESERT, DARK_FOREST, OLD_GROWTH_PINE_TAIGA,
		OLD_GROWTH_SPRUCE_TAIGA, BEACH, SNOWY_BEACH, STONY_SHORE, JUNGLE, SPARSE_JUNGLE, BAMBOO_JUNGLE, JAGGED_PEAKS,
		FROZEN_PEAKS, ICE_SPIKES, STONY_PEAKS, FOREST, FLOWER_FOREST, BIRCH_FOREST, OLD_GROWTH_BIRCH_FOREST, TAIGA,
		SNOWY_TAIGA, SNOWY_PLAINS, GROVE, SNOWY_SLOPES, PLAINS, SUNFLOWER_PLAINS, MEADOW, MUSHROOM_FIELDS, CHERRY_GROVE,
		SAVANNA, SAVANNA_PLATEAU, WINDSWEPT_SAVANNA, WINDSWEPT_FOREST, WINDSWEPT_HILLS, WINDSWEPT_GRAVELLY_HILLS,
		DEEP_DARK, LUSH_CAVES, DRIPSTONE_CAVES, BADLANDS, ERODED_BADLANDS, WOODED_BADLANDS, PALE_GARDEN
	}

	private static enum NetherBiomes {
		BASALT_DELTAS, CRIMSON_FOREST, NETHER_WASTES, SOUL_SAND_VALLEY, WARPED_FOREST
	}

	private static enum EndBiomes {
		THE_END, END_BARRENS, END_HIGHLANDS, END_MIDLANDS, SMALL_END_ISLANDS
	}

	public void loadMaterialsForWorld(String fileName) {
		Path filePath = Paths.get(plugin.getDataFolder().toString(), "SkygridBlocks", fileName);
		try {
			String fileContent = Files.readString(filePath, StandardCharsets.UTF_8);
			Object2DoubleMap<Material> distribution = new Object2DoubleOpenHashMap<>();
			String worldName = getWorldNameFromFileName(fileName);
			for (String line : fileContent.split("\n")) {
				processLine(line.trim(), distribution);
			}
			ObjectSet<String> biomes = worldBiomeMappings.get(fileName);
			for (String biome : biomes) {
				String alias = worldName + "-" + biome;
				MaterialDistribution materialDistribution = new MaterialDistribution(distribution);
				setMaterialDistribution(alias, materialDistribution);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processLine(String line, Object2DoubleMap<Material> distribution) {
		if (!line.startsWith("#") && !line.isEmpty()) {
			String[] parts = line.split(":");
			Material material = Material.getMaterial(parts[0]);
			if (material != null) {
				double percentage = parts.length > 1 ? parsePercentage(parts[1]) : 1.0;
				distribution.put(material, distribution.getOrDefault(material, 0.0) + percentage);
			}
		}
	}

	public void loadMaterialsForWorldMultiBiome(String fileName) {
		Path filePath = Paths.get(plugin.getDataFolder().toString(), "SkygridBlocks", fileName);
		try {
			String fileContent = Files.readString(filePath, StandardCharsets.UTF_8);
			ObjectSet<String> currentBiomes = new ObjectOpenHashSet<>();
			Object2DoubleMap<Material> materialsMap = new Object2DoubleOpenHashMap<>();
			String worldName = getWorldNameFromFileName(fileName);
			for (String line : fileContent.split("\n")) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				if (line.startsWith("-")) {
					if (!currentBiomes.isEmpty() && !materialsMap.isEmpty()) {
						for (String biome : currentBiomes) {
							String alias = worldName + "-" + biome;
							MaterialDistribution materialDistribution = new MaterialDistribution(materialsMap);
							setMaterialDistribution(alias, materialDistribution);
						}
						currentBiomes.clear();
						materialsMap.clear();
					}
					String[] biomes = line.replace("-", "").split(",");
					Collections.addAll(currentBiomes, biomes);
					continue;
				}
				if (!line.isEmpty()) {
					String[] parts = line.split(":");
					Material material = Material.getMaterial(parts[0]);
					if (material != null) {
						double percentage = parts.length > 1 ? parsePercentage(parts[1]) : 1.0;
						materialsMap.put(material, materialsMap.getOrDefault(material, 0.0) + percentage);
					}
				}
			}
			if (!currentBiomes.isEmpty() && !materialsMap.isEmpty()) {
				for (String biome : currentBiomes) {
					String alias = worldName + "-" + biome;
					MaterialDistribution materialDistribution = new MaterialDistribution(materialsMap);
					setMaterialDistribution(alias, materialDistribution);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double parsePercentage(String percentageString) {
		try {
			return Double.parseDouble(percentageString);
		} catch (NumberFormatException e) {
			return 1.0;
		}
	}

	private String getWorldNameFromFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private void setMaterialDistribution(String alias, MaterialDistribution materialDistribution) {
		materialDistributions.put(alias, materialDistribution);
	}

	public Material getRandomMaterialForWorld(String worldName, String biomeName) {
		String alias = worldName + "-" + biomeName;
		MaterialDistribution materialDistribution = materialDistributions.get(alias);
		if (materialDistribution != null) {
			return materialDistribution.next();
		}
		return null;
	}

	static class MaterialDistribution {
		private final Material[] materials;
		private final double[] probabilities;
		private final int[] alias;
		private final int size;

		public MaterialDistribution(Object2DoubleMap<Material> distribution) {
			size = distribution.size();
			materials = new Material[size];
			probabilities = new double[size];
			alias = new int[size];

			IntArrayList small = new IntArrayList();
			IntArrayList large = new IntArrayList();

			double total = calculateTotal(distribution);
			double[] normalized = new double[size];
			int index = 0;

			for (Object2DoubleMap.Entry<Material> entry : distribution.object2DoubleEntrySet()) {
				materials[index] = entry.getKey();
				normalized[index] = (entry.getDoubleValue() / total) * size;
				if (normalized[index] < 1.0) {
					small.add(index);
				} else {
					large.add(index);
				}
				index++;
			}

			while (!small.isEmpty() && !large.isEmpty()) {
				int smallIndex = small.removeInt(small.size() - 1);
				int largeIndex = large.removeInt(large.size() - 1);

				probabilities[smallIndex] = normalized[smallIndex];
				alias[smallIndex] = largeIndex;

				normalized[largeIndex] = (normalized[largeIndex] + normalized[smallIndex]) - 1.0;

				if (normalized[largeIndex] < 1.0) {
					small.add(largeIndex);
				} else {
					large.add(largeIndex);
				}
			}

			while (!small.isEmpty()) {
				probabilities[small.removeInt(small.size() - 1)] = 1.0;
			}

			while (!large.isEmpty()) {
				probabilities[large.removeInt(large.size() - 1)] = 1.0;
			}
		}

		private double calculateTotal(Object2DoubleMap<Material> distribution) {
			double total = 0.0;
			for (double value : distribution.values()) {
				total += value;
			}
			return total;
		}

		public Material next() {
			int column = ThreadLocalRandom.current().nextInt(size);
			return ThreadLocalRandom.current().nextDouble() < probabilities[column] ? materials[column] : materials[alias[column]];
		}
	}
}