package main;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.util.*;

public class Generator implements Listener {
	private final JavaPlugin plugin;
	private final Random random;
	private final Map<String, List<Material>> worldMaterials;
	private final Map<String, Set<String>> generatedChunksByWorld;
	private final Map<String, Map<Material, Integer>> biomeMaterials; // Biome-specific block data
	private final Spawner spawner; // Create a field to hold the Spawner instance
	private final Chest chest; // Create a field to hold the Spawner instance

	// Variable to set the number of chunks in which the blocks are distributed
	private final double chunksForDistribution = 32;

	// Delay in ticks before processing the chunk after it is loaded
	private final int PROCESS_DELAY = 5;

	// Variable to track the number of chunks being processed for each dimension
	private final Map<World.Environment, Integer> chunksProcessedByDimension;

	public Generator(JavaPlugin plugin, boolean clearChunksBeforeGeneration) {
		this.plugin = plugin;
		this.random = new Random();
		this.worldMaterials = new HashMap<>();
		this.generatedChunksByWorld = new HashMap<>();
		this.biomeMaterials = new HashMap<>();
		this.chunksProcessedByDimension = new HashMap<>();
		// Initialize the Spawner instance
		this.spawner = Spawner.getInstance(plugin);
		// Initialize the Spawner instance
		this.chest = Chest.getInstance(plugin);
	}

	// Method to initialize the generator and load data from files
	public void initialize() {
		loadWorldMaterials();
		loadGeneratedChunks();
		processLoadedChunks();
		registerEvents();
		callOreGen();
	}

	private void callOreGen() {
		OreGen oreGen = new OreGen(plugin);
		plugin.getServer().getPluginManager().registerEvents(oreGen, plugin);
	}

	// Method to load world materials from text files
	private void loadWorldMaterials() {
		loadMaterialsForWorld("world.txt");
		loadMaterialsForWorld("world_nether.txt");
		loadMaterialsForWorld("world_the_end.txt");
	}

	private void loadMaterialsForWorld(String fileName) {
		plugin.getLogger().info("Loading materials for world from file: " + fileName);
		File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);
		if (file.exists()) {
			List<Material> materials = new ArrayList<>();
			List<Material> materialsWithoutPercentage = new ArrayList<>(); // New list for items without percentages
			double remainingPercentage = 100.0; // Initialize remaining percentage

			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;
				String currentBiome = null;

				while ((line = reader.readLine()) != null) {
					if (line.trim().startsWith("#")) {
						continue; // Skip comments
					}

					if (line.trim().startsWith("-")) {
						currentBiome = line.trim().replace("-", "");
						continue; // Start of a new biome section
					}

					String[] parts = line.split(":");
					Material material = Material.getMaterial(parts[0]);

					if (material != null) {
						int count = (int) Math.ceil(parsePercentage(parts[1]) * chunksForDistribution);

						if (currentBiome != null) {
							// Add biome-specific block data
							Map<Material, Integer> biomeMaterialsMap = biomeMaterials.computeIfAbsent(currentBiome, k -> new HashMap<>());
							biomeMaterialsMap.put(material, count);
						} else {
							// If no biome-specific data, add to global materials
							for (int i = 0; i < count; i++) {
								materials.add(material);
							}
							remainingPercentage -= parsePercentage(parts[1]);
						}
					}
				}

				// Calculate the number of items to distribute the remaining percentage
				int remainingItemCount = materialsWithoutPercentage.size();
				if (remainingItemCount > 0 && remainingPercentage > 0) {
					double percentagePerItem = remainingPercentage / remainingItemCount;
					for (Material material : materialsWithoutPercentage) {
						int count = (int) Math.ceil(percentagePerItem * chunksForDistribution);
						for (int i = 0; i < count; i++) {
							materials.add(material);
						}
					}
				}

				if (!materials.isEmpty()) {
					String worldName = getWorldNameFromFileName(fileName);
					plugin.getLogger().info("Materials loaded for world: " + worldName);
					worldMaterials.put(worldName, materials);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			plugin.getLogger().warning("Missing file: " + fileName);
		}
	}

	private double parsePercentage(String percentageString) {
		try {
			return Double.parseDouble(percentageString);
		} catch (NumberFormatException e) {
			plugin.getLogger().warning("Invalid percentage format: " + percentageString);
			return 1.0; // Default to 1% if invalid format
		}
	}

	private String getWorldNameFromFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	private void loadGeneratedChunks() {
		// Load generated chunks for each world type
		for (String worldName : worldMaterials.keySet()) {
			File file = new File(plugin.getDataFolder(), worldName + "_generated_chunks.txt");
			Set<String> chunks = new HashSet<>();
			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = reader.readLine()) != null) {
						chunks.add(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			generatedChunksByWorld.put(worldName, chunks);
		}
	}

	private void saveGeneratedChunks() {
		// Save generated chunks for each world type
		for (String worldName : generatedChunksByWorld.keySet()) {
			File file = new File(plugin.getDataFolder(), worldName + "_generated_chunks.txt");
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (String chunkCoords : generatedChunksByWorld.get(worldName)) {
					writer.write(chunkCoords);
					writer.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		String chunkCoords = chunk.getX() + "," + chunk.getZ();
		String worldName = chunk.getWorld().getName();
		Set<String> generatedChunks = generatedChunksByWorld.getOrDefault(worldName, new HashSet<>());

		if (!generatedChunks.contains(chunkCoords)) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> processChunk(chunk), PROCESS_DELAY);
		}
	}

	public void processLoadedChunks() {
		List<World.Environment> dimensionsToProcess = Arrays.asList(World.Environment.NORMAL, World.Environment.NETHER, World.Environment.THE_END);
		Queue<World.Environment> dimensionQueue = new LinkedList<>(dimensionsToProcess);
		processNextDimension(dimensionQueue);
	}

	private void processNextDimension(Queue<World.Environment> dimensionQueue) {
		World.Environment dimension = dimensionQueue.poll();
		if (dimension == null) {
			return;
		}

		List<Chunk> chunksToProcess = new ArrayList<>();
		for (World world : Bukkit.getWorlds()) {
			if (world.getEnvironment() == dimension) {
				for (Chunk loadedChunk : world.getLoadedChunks()) {
					String chunkCoords = loadedChunk.getX() + "," + loadedChunk.getZ();
					String worldName = loadedChunk.getWorld().getName();
					Set<String> generatedChunks = generatedChunksByWorld.getOrDefault(worldName, new HashSet<>());

					if (!generatedChunks.contains(chunkCoords)) {
						chunksToProcess.add(loadedChunk);
					}
				}
			}
		}

		int totalChunks = chunksToProcess.size();
		int batchSize = Math.max(1, (totalChunks + 49) / 50);

		Bukkit.getScheduler().runTaskLater(plugin, () -> processChunksBatch(dimension, chunksToProcess, batchSize, 0, dimensionQueue), PROCESS_DELAY);
	}

	private void processChunksBatch(World.Environment dimension, List<Chunk> chunksToProcess, int batchSize, int batchIndex, Queue<World.Environment> dimensionQueue) {
		int totalChunks = chunksToProcess.size();
		int startIdx = batchIndex * batchSize;
		int endIdx = Math.min((batchIndex + 1) * batchSize, totalChunks);

		for (int i = startIdx; i < endIdx; i++) {
			Chunk chunk = chunksToProcess.get(i);
			processChunk(chunk);
		}

		if (endIdx < totalChunks) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> processChunksBatch(dimension, chunksToProcess, batchSize, batchIndex + 1, dimensionQueue), PROCESS_DELAY);
		} else {
			processNextDimension(dimensionQueue);
			plugin.getLogger().info("Chunk generation completed for " + dimension.toString().toLowerCase().replace("_", " ") + "!");
		}
	}

	private void processChunk(Chunk chunk) {
		int xStart = chunk.getX() << 4;
		int zStart = chunk.getZ() << 4;
		int minY;
		int maxY;

		String worldName = chunk.getWorld().getName();
		World.Environment dimension = chunk.getWorld().getEnvironment();
		String biomeName = getBiomeName(chunk);

		switch (dimension) {
		case NETHER:
			minY = 0;
			maxY = 128;
			break;
		case THE_END:
			minY = 0;
			maxY = 128;
			break;
		default: // Overworld
			minY = -64;
			maxY = 256;
			break;
		}

		for (int x = xStart + 1; x <= xStart + 13; x += 4) {
			for (int z = zStart + 2; z <= zStart + 14; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Material material = getRandomMaterialForWorld(worldName, biomeName);
					if (material != null) {
						int chunkX = x & 0xF;
						int chunkZ = z & 0xF;
						int chunkY = Math.max(minY, Math.min(maxY, y));

						Block block = chunk.getBlock(chunkX, chunkY, chunkZ);
						block.setType(material, false);

						BlockData blockData = block.getBlockData();

						if (material == Material.CHORUS_FLOWER) {
							if (blockData instanceof Ageable) {
								Ageable ageable = (Ageable) blockData;
								int maxAge = ageable.getMaximumAge();
								ageable.setAge(Math.min(5, maxAge));
								block.setBlockData(ageable, false);
							}
						} else if (material.toString().endsWith("_LEAVES")) {
							Leaves leaves = (Leaves) blockData;
							leaves.setPersistent(true);
							block.setBlockData(leaves, false);
						} else if (blockData instanceof Bamboo) {
							Bamboo bamboo = (Bamboo) blockData;
							bamboo.setStage(1);
							block.setBlockData(bamboo, false);
						} else if (material == Material.CACTUS || material == Material.SUGAR_CANE || material == Material.KELP || material == Material.WHEAT || material == Material.BEETROOTS || material == Material.CARROTS || material == Material.POTATOES || material == Material.TORCHFLOWER_CROP || material == Material.PITCHER_CROP || material == Material.NETHER_WART || material == Material.CAVE_VINES) {
							if (blockData instanceof Ageable) {
								Ageable ageable = (Ageable) blockData;
								int maxAge = ageable.getMaximumAge();
								ageable.setAge(maxAge);
								block.setBlockData(ageable, false);
							}
							if (material == Material.CAVE_VINES || material == Material.CAVE_VINES_PLANT) {
								CaveVines caveVines = (CaveVines) block.getBlockData();
								caveVines.setBerries(true);
								block.setBlockData(caveVines, true);
							}
						} else if (material == Material.SPAWNER) {
							spawner.BlockInfo(block);
						}else if (material == Material.CHEST) {
							chest.loadChest(block);
						}
					}
				}
			}
		}

		int remainingChunks = chunksProcessedByDimension.getOrDefault(dimension, 0) - 1;
		chunksProcessedByDimension.put(dimension, remainingChunks);

		if (remainingChunks == 0) {
			plugin.getLogger().info("Chunk generation completed for " + dimension.toString().toLowerCase().replace("_", " ") + "!");
		}

		String chunkCoords = chunk.getX() + "," + chunk.getZ();
		generatedChunksByWorld.get(worldName).add(chunkCoords);
		saveGeneratedChunks();
	}

	// Method to get a random material for a specific world and biome, respecting percentages
	private Material getRandomMaterialForWorld(String worldName, String biomeName) {
		List<Material> materials = worldMaterials.get(worldName);
		Map<Material, Integer> biomeMaterialsMap = biomeMaterials.get(biomeName);

		if (materials != null && !materials.isEmpty()) {
			if (biomeMaterialsMap != null && !biomeMaterialsMap.isEmpty()) {
				List<Material> possibleMaterials = new ArrayList<>();

				// Add biome-specific materials
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

			// If there are no biome-specific materials, use the global materials list
			int randomIndex = random.nextInt(materials.size());
			return materials.get(randomIndex);
		}

		return null;
	}

	private String getBiomeName(Chunk chunk) {
		return chunk.getBlock(7, 64, 7).getBiome().name();
	}
}