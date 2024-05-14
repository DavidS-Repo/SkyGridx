package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;

public class MaterialManager {
	private final SkyGridPlugin plugin;
	private final ThreadLocalRandom random;
	private final Map<String, List<Material>> worldMaterials;
	private final Map<String, Map<Material, Integer>> biomeMaterials;
	private final double chunksForDistribution = 64;

	public MaterialManager(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.random = ThreadLocalRandom.current();
		this.worldMaterials = new HashMap<>();
		this.biomeMaterials = new HashMap<>();
	}

	public void loadMaterialsForWorld(String fileName) {
		plugin.getLogger().info("Enabling simple material loader.");
		plugin.getLogger().info("Loading materials for world from file: " + fileName);
		File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);

		if (file.exists()) {
			List<Material> materials = new ArrayList<>();
			Map<String, List<Material>> biomeMaterialsMap = new HashMap<>();
			double remainingPercentage = 100.0;

			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;
				Set<String> currentBiomes = new HashSet<>();

				while ((line = reader.readLine()) != null) {
					if (line.trim().startsWith("#")) {
						continue;
					}
					String[] parts = line.split(":");
					Material material = Material.getMaterial(parts[0]);

					if (material != null) {
						double percentage = parts.length > 1 ? parsePercentage(parts[1]) : 1.0;

						if (!currentBiomes.isEmpty()) {
							for (String biome : currentBiomes) {
								List<Material> biomeMaterials = biomeMaterialsMap.computeIfAbsent(biome, k -> new ArrayList<>());
								for (int i = 0; i < Math.ceil(percentage * chunksForDistribution); i++) {
									biomeMaterials.add(material);
								}
							}
						} else {
							for (int i = 0; i < Math.ceil(percentage * chunksForDistribution); i++) {
								materials.add(material);
							}
							remainingPercentage -= percentage;
						}
					}
				}
				if (currentBiomes.isEmpty() && !materials.isEmpty()) {
					redistributeRemainingPercentage(materials, remainingPercentage);
				}
				if (!materials.isEmpty()) {
					String worldName = getWorldNameFromFileName(fileName);
					plugin.getLogger().info("Materials loaded for world: " + worldName);
					worldMaterials.put(worldName, materials);
				}
				for (String biome : currentBiomes) {
					redistributeRemainingPercentage(biomeMaterialsMap.get(biome), 100.0);
					biomeMaterials.put(biome, calculateMaterialDistribution(biomeMaterialsMap.get(biome)));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			plugin.getLogger().warning("Missing file: " + fileName);
		}
	}

	private List<Material> redistributeRemainingPercentage(List<Material> materials, double remainingPercentage) {
		List<Material> redistributedMaterials = new ArrayList<>();
		int remainingItemCount = materials.size();
		if (remainingItemCount > 0 && remainingPercentage > 0) {
			double percentagePerItem = remainingPercentage / remainingItemCount;
			for (Material material : materials) {
				int repetitionCount = (int) Math.ceil(percentagePerItem * chunksForDistribution);
				for (int i = 0; i < repetitionCount; i++) {
					redistributedMaterials.add(material);
				}
			}
		}
		return redistributedMaterials;
	}

	private Map<Material, Integer> calculateMaterialDistribution(List<Material> materials) {
		Map<Material, Integer> materialDistribution = new HashMap<>();
		for (Material material : materials) {
			materialDistribution.put(material, materialDistribution.getOrDefault(material, 0) + 1);
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
		Map<Material, Integer> biomeMaterialsMap = biomeMaterials.get(biomeName);

		if (biomeMaterialsMap != null && !biomeMaterialsMap.isEmpty()) {
			List<Material> possibleMaterials = new ArrayList<>();

			for (Map.Entry<Material, Integer> entry : biomeMaterialsMap.entrySet()) {
				Material material = entry.getKey();
				int count = entry.getValue();
				for (int i = 0; i < count; i++) {
					possibleMaterials.add(material);
				}
			}
			if (!possibleMaterials.isEmpty()) {
				int randomIndex = random.nextInt(possibleMaterials.size());
				return possibleMaterials.get(randomIndex);
			}
		}
		if (materials != null && !materials.isEmpty()) {
			int randomIndex = random.nextInt(materials.size());
			return materials.get(randomIndex);
		}
		return null;
	}

	public void loadMaterialsForWorldMultiBiome(String fileName) {
		plugin.getLogger().info("Enabling Advanced material loader.");
		plugin.getLogger().info("Loading materials for world from file: " + fileName);
		File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);

		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;
				Set<String> currentBiomes = new HashSet<>();
				Map<Material, Integer> materialsMap = new HashMap<>();

				while ((line = reader.readLine()) != null) {
					if (line.trim().startsWith("#")) {
						continue;
					}
					if (line.trim().startsWith("-")) {
						if (!currentBiomes.isEmpty() && !materialsMap.isEmpty()) {
							for (String biome : currentBiomes) {
								biomeMaterials.put(biome, new HashMap<>(materialsMap));
							}
							currentBiomes.clear();
							materialsMap = new HashMap<>();
						}
						String[] biomes = line.trim().replace("-", "").split(",");
						currentBiomes.addAll(Arrays.asList(biomes));
						continue;
					}
					for (String biome : currentBiomes) {
						if (biome != null) {
							String[] parts = line.split(":");
							Material material = Material.getMaterial(parts[0]);

							if (material != null) {
								double percentage = parts.length > 1 ? parsePercentage(parts[1]) : 1.0;
								int itemCount = (int) Math.ceil(percentage * chunksForDistribution);
								materialsMap.put(material, itemCount);
							}
						}
					}
				}
				if (!materialsMap.isEmpty() && !currentBiomes.isEmpty()) {
					for (String biome : currentBiomes) {
						biomeMaterials.put(biome, new HashMap<>(materialsMap));
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Material getRandomMaterialForWorldMultiBiome(String worldName, String biomeName) {
		List<Material> materials = worldMaterials.get(worldName);
		Map<Material, Integer> biomeMaterialsMap = biomeMaterials.get(biomeName);

		if (biomeMaterialsMap != null && !biomeMaterialsMap.isEmpty()) {
			List<Material> possibleMaterials = new ArrayList<>();

			for (Map.Entry<Material, Integer> entry : biomeMaterialsMap.entrySet()) {
				Material material = entry.getKey();
				int count = entry.getValue();
				for (int i = 0; i < count; i++) {
					possibleMaterials.add(material);
				}
			}
			if (!possibleMaterials.isEmpty()) {
				int randomIndex = random.nextInt(possibleMaterials.size());
				return possibleMaterials.get(randomIndex);
			}
		}
		if (materials != null && !materials.isEmpty()) {
			int randomIndex = random.nextInt(materials.size());
			return materials.get(randomIndex);
		}
		return null;
	}
}