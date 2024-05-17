package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.file.Files;


import org.bukkit.Material;

public class MaterialManager {
	private final SkyGridPlugin plugin;
	private final ThreadLocalRandom random;
	private final Map<String, List<Material>> worldMaterials;
	private final Map<String, Map<Material, Double>> biomeMaterials;
	private static final double CHUNKS_FOR_DISTRIBUTION = 64.0;

	public MaterialManager(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.random = ThreadLocalRandom.current();
		this.worldMaterials = new HashMap<>();
		this.biomeMaterials = new HashMap<>();
	}

	public void loadMaterialsForWorld(String fileName) {
		Path filePath = Paths.get(plugin.getDataFolder().toString(), "SkygridBlocks", fileName);
		try (BufferedReader reader = Files.newBufferedReader(filePath)) {
			List<Material> materials = new ArrayList<>();
			Map<String, List<Material>> biomeMaterialsMap = new HashMap<>();
			double remainingPercentage = 100.0;

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("#")) {
					continue;
				}
				String[] parts = line.split(":");
				Material material = Material.getMaterial(parts[0]);

				if (material != null) {
					double percentage = parts.length > 1 ? parsePercentage(parts[1]) : 1.0;
					int chunkCount = (int) Math.ceil(percentage * CHUNKS_FOR_DISTRIBUTION);

					if (biomeMaterialsMap.isEmpty()) {
						for (int i = 0; i < chunkCount; i++) {
							materials.add(material);
						}
						remainingPercentage -= percentage;
					} else {
						for (List<Material> biomeMaterials : biomeMaterialsMap.values()) {
							for (int i = 0; i < chunkCount; i++) {
								biomeMaterials.add(material);
							}
						}
					}
				}
			}

			if (!materials.isEmpty()) {
				redistributeRemainingPercentage(materials, remainingPercentage);
				String worldName = getWorldNameFromFileName(fileName);
				worldMaterials.put(worldName, materials);
			}

			for (List<Material> biomeMaterials : biomeMaterialsMap.values()) {
				redistributeRemainingPercentage(biomeMaterials, 100.0);
			}

			biomeMaterialsMap.forEach((biome, materialsList) ->
			biomeMaterials.put(biome, calculateMaterialDistribution(materialsList))
					);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void redistributeRemainingPercentage(List<Material> materials, double remainingPercentage) {
		if (remainingPercentage > 0.0) {
			int itemCount = materials.size();
			double percentagePerItem = remainingPercentage / itemCount;
			int chunkCount = (int) Math.ceil(percentagePerItem * CHUNKS_FOR_DISTRIBUTION);

			for (int i = 0; i < itemCount; i++) {
				for (int j = 0; j < chunkCount; j++) {
					materials.add(materials.get(i));
				}
			}
		}
	}

	private Map<Material, Double> calculateMaterialDistribution(List<Material> materials) {
		Map<Material, Double> materialDistribution = new HashMap<>();
		for (Material material : materials) {
			materialDistribution.put(material, materialDistribution.getOrDefault(material, 0.0) + 1.0);
		}
		return materialDistribution;
	}

	private double parsePercentage(String percentageString) {
		try {
			return Double.parseDouble(percentageString);
		} catch (NumberFormatException e) {
			plugin.getLogger().warning("Invalid percentage format: " + percentageString);
			return 1.0;
		}
	}

	private String getWorldNameFromFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	public Material getRandomMaterialForWorld(String worldName, String biomeName) {
		List<Material> materials = worldMaterials.get(worldName);
		Map<Material, Double> biomeMaterialsMap = biomeMaterials.get(biomeName);

		if (biomeMaterialsMap != null && !biomeMaterialsMap.isEmpty()) {
			List<Material> possibleMaterials = new ArrayList<>();
			biomeMaterialsMap.forEach((material, count) -> {
				for (int i = 0; i < count; i++) {
					possibleMaterials.add(material);
				}
			});

			if (!possibleMaterials.isEmpty()) {
				return possibleMaterials.get(random.nextInt(possibleMaterials.size()));
			}
		}

		return materials != null && !materials.isEmpty() ? materials.get(random.nextInt(materials.size())) : null;
	}

	public void loadMaterialsForWorldMultiBiome(String fileName) {
		Path filePath = Paths.get(plugin.getDataFolder().toString(), "SkygridBlocks", fileName);
		try (BufferedReader reader = Files.newBufferedReader(filePath)) {
			String currentLine;
			Set<String> currentBiomes = new HashSet<>();
			Map<Material, Double> materialsMap = new HashMap<>();

			while ((currentLine = reader.readLine()) != null) {
				final String line = currentLine;

				if (line.trim().startsWith("#")) {
					continue;
				}
				if (line.trim().startsWith("-")) {
					if (!currentBiomes.isEmpty() && !materialsMap.isEmpty()) {
						currentBiomes.forEach(biome -> biomeMaterials.put(biome, new HashMap<>(materialsMap)));
						currentBiomes.clear();
						materialsMap.clear();
					}
					String[] biomes = line.trim().replace("-", "").split(",");
					currentBiomes.addAll(Arrays.asList(biomes));
					continue;
				}
				currentBiomes.stream()
				.filter(Objects::nonNull)
				.map(biome -> line.split(":"))
				.forEach(parts -> {
					Material material = Material.getMaterial(parts[0]);
					if (material != null) {
						double percentage = parts.length > 1 ? parsePercentage(parts[1]) : 1.0;
						materialsMap.put(material, percentage);
					}
				});
			}
			currentBiomes.forEach(biome -> biomeMaterials.put(biome, new HashMap<>(materialsMap)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Material getRandomMaterialForWorldMultiBiome(String worldName, String biomeName) {
		Map<Material, Double> biomeMaterialsMap = biomeMaterials.get(biomeName);
		if (biomeMaterialsMap != null && !biomeMaterialsMap.isEmpty()) {
			return selectMaterialWithWeight(biomeMaterialsMap);
		}
		List<Material> materials = worldMaterials.get(worldName);
		if (materials != null && !materials.isEmpty()) {
			return materials.get(random.nextInt(materials.size()));
		}
		return null;
	}

	private Material selectMaterialWithWeight(Map<Material, Double> materialsMap) {
		double totalWeight = 0;
		for (Double weight : materialsMap.values()) {
			totalWeight += weight;
		}
		double randomWeight = random.nextDouble() * totalWeight;

		for (Map.Entry<Material, Double> entry : materialsMap.entrySet()) {
			randomWeight -= entry.getValue();
			if (randomWeight <= 0) {
				return entry.getKey();
			}
		}
		throw new IllegalStateException("Failed to select material based on weight");
	}
}