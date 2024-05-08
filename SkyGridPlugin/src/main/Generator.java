package main;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class Generator implements Listener {
	private final JavaPlugin plugin;
	private final Random random;
	private final Map<String, List<Material>> worldMaterials;
	private final Map<String, Set<String>> generatedChunksByWorld;
	private final Map<String, Map<Material, Integer>> biomeMaterials;
	private final Spawner spawner;
	private final Chest chest;
	private final int PROCESS_DELAY = 10;
	private final double chunksForDistribution = 64;
	private boolean firstBootComplete = false;
	private boolean serverLoaded = false;

	public Generator(JavaPlugin plugin) {
		this.plugin = plugin;
		this.random = new Random();
		this.worldMaterials = new HashMap<>();
		this.generatedChunksByWorld = new HashMap<>();
		this.biomeMaterials = new HashMap<>();
		this.spawner = Spawner.getInstance(plugin);
		this.chest = Chest.getInstance(plugin);
	}

	public void initialize() {
		registerEvents();
		loadWorldMaterials();
		callOreGen();
	}

	private void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	private void loadWorldMaterials() {
		loadMaterialsForWorld("world.txt");
		loadMaterialsForWorld("world_nether.txt");
		loadMaterialsForWorld("world_the_end.txt");
	}

	private void callOreGen() {
		OreGen oreGen = new OreGen(plugin);
		plugin.getServer().getPluginManager().registerEvents(oreGen, plugin);
	}

	private void loadMaterialsForWorld(String fileName) {
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
					if (line.trim().startsWith("-")) {
						String[] biomes = line.trim().replace("-", "").split(",");
						currentBiomes.addAll(Arrays.asList(biomes));
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
					// Redistribute remaining percentage among materials without specified biome
					redistributeRemainingPercentage(materials, remainingPercentage);
				}

				if (!materials.isEmpty()) {
					String worldName = getWorldNameFromFileName(fileName);
					plugin.getLogger().info("Materials loaded for world: " + worldName);
					worldMaterials.put(worldName, materials);
				}

				for (String biome : currentBiomes) {
					redistributeRemainingPercentage(biomeMaterialsMap.get(biome), 100.0); // Redistribute percentages for each biome list
					// Store biome-specific materials
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
			System.out.println("Invalid percentage format: " + percentageString);
			return 1.0;
		}
	}

	private String getWorldNameFromFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerLoad(ServerLoadEvent event) {
		serverLoaded = true;
		if (!firstBootComplete) {
			processAllLoadedChunks();
			firstBootComplete = true;
			plugin.getServer().getConsoleSender().sendMessage("\u001B[32mFirst boot generation complete.\u001B[0m");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		if (serverLoaded) {
			Chunk chunk = event.getChunk();
			String chunkCoords = chunk.getX() + "," + chunk.getZ();
			String worldName = chunk.getWorld().getName();
			Set<String> generatedChunks = generatedChunksByWorld.getOrDefault(worldName, new HashSet<>());

			if (event.isNewChunk() && !generatedChunks.contains(chunkCoords)) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> processChunk(chunk), PROCESS_DELAY);
			} else {
				generatedChunks.add(chunkCoords);
			}
		}
	}

	private void processAllLoadedChunks() {
		for (World world : Bukkit.getWorlds()) {
			for (Chunk loadedChunk : world.getLoadedChunks()) {
				String chunkCoords = loadedChunk.getX() + "," + loadedChunk.getZ();
				String worldName = loadedChunk.getWorld().getName();
				Set<String> generatedChunks = generatedChunksByWorld.getOrDefault(worldName, new HashSet<>());

				if (!generatedChunks.contains(chunkCoords)) {
					processChunk(loadedChunk);
				}
			}
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
		int batchSize = Math.max(1, (totalChunks + 1) / 2);
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
		}
	}

	private void processChunk(Chunk chunk) {
		World world = chunk.getWorld();
		String worldName = world.getName();
		World.Environment dimension = world.getEnvironment();
		int xStart = chunk.getX() << 4, zStart = chunk.getZ() << 4;
		int minY, maxY;

		switch (dimension) {
		case NETHER: minY = 0; maxY = 128; break;
		case THE_END: minY = 0; maxY = 128; break;
		default: minY = -64; maxY = 64; break;
		}

		for (int x = xStart + 1; x <= xStart + 13; x += 4) {
			for (int z = zStart + 2; z <= zStart + 14; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					int chunkX = x & 0xF;
					int chunkZ = z & 0xF;
					int chunkY = Math.max(minY, Math.min(maxY, y));

					Block block = chunk.getBlock(chunkX, chunkY, chunkZ);
					String biomeName = block.getBiome().name();

					Material material = getRandomMaterialForWorld(worldName, biomeName);
					if (material != null) {
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
						} else if (material == Material.CACTUS || material == Material.SUGAR_CANE
								|| material == Material.KELP || material == Material.WHEAT
								|| material == Material.BEETROOTS || material == Material.CARROTS
								|| material == Material.POTATOES || material == Material.TORCHFLOWER_CROP
								|| material == Material.PITCHER_CROP || material == Material.NETHER_WART
								|| material == Material.CAVE_VINES) {
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
						} else if (material == Material.CHEST) {
							chest.loadChest(block);
						}
					}
				}
			}
		}
	}

	private Material getRandomMaterialForWorld(String worldName, String biomeName) {
		List<Material> materials = worldMaterials.get(worldName);
		Map<Material, Integer> biomeMaterialsMap = new HashMap<>();

		if (biomeMaterials.containsKey(biomeName)) {
			biomeMaterialsMap.putAll(biomeMaterials.get(biomeName));
		}
		if (!biomeMaterialsMap.isEmpty()) {
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
