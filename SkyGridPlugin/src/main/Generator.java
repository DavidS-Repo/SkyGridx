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
import org.bukkit.event.world.ChunkLoadEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Generator implements Listener {
	private final SkyGridPlugin plugin;
	private final PluginSettings settings;
	private final ThreadLocalRandom random;
	private final Map<String, Set<Pair<Integer, Integer>>> generatedChunksByWorld;
	private final MaterialManager materialManager;
	private final Spawner spawner;
	private final Chest chest;


	private static final List<Material> CROP_MATERIALS = Arrays.asList(
			Material.CACTUS, Material.SUGAR_CANE, Material.KELP, Material.WHEAT,
			Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.TORCHFLOWER_CROP,
			Material.PITCHER_CROP, Material.NETHER_WART, Material.CAVE_VINES
			);

	private static final List<Material> LEAVES = Arrays.asList(
			Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.CHERRY_LEAVES,
			Material.DARK_OAK_LEAVES, Material.FLOWERING_AZALEA_LEAVES, Material.JUNGLE_LEAVES,
			Material.MANGROVE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES
			);

	public Generator(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.settings = new PluginSettings(plugin);
		this.random = ThreadLocalRandom.current();
		this.materialManager = new MaterialManager(plugin);
		this.generatedChunksByWorld = new HashMap<>();
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
		if (hasBiomeHeaders()) {
			plugin.getLogger().info("Enabling Advanced material loader.");
			materialManager.loadMaterialsForWorldMultiBiome("world.txt");
			materialManager.loadMaterialsForWorldMultiBiome("world_nether.txt");
			materialManager.loadMaterialsForWorldMultiBiome("world_the_end.txt");
		} else {
			plugin.getLogger().info("Enabling Basic material loader.");
			materialManager.loadMaterialsForWorld("world.txt");
			materialManager.loadMaterialsForWorld("world_nether.txt");
			materialManager.loadMaterialsForWorld("world_the_end.txt");
		}
	}

	private boolean hasBiomeHeaders() {
		List<String> fileNames = Arrays.asList("world.txt", "world_nether.txt", "world_the_end.txt");

		for (String fileName : fileNames) {
			File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);

			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (!line.trim().startsWith("#") && line.trim().startsWith("-")) {
							return true;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				plugin.getLogger().warning("Missing file: " + fileName);
			}
		}
		return false;
	}

	private void callOreGen() {
		OreGen oreGen = new OreGen(plugin);
		plugin.getServer().getPluginManager().registerEvents(oreGen, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		Pair<Integer, Integer> chunkCoords = new Pair<>(chunk.getX(), chunk.getZ());
		String worldName = chunk.getWorld().getName();
		Set<Pair<Integer, Integer>> generatedChunks = generatedChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>());

		if (event.isNewChunk() && !generatedChunks.contains(chunkCoords)) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> processChunk(chunk), settings.getProcessDelay());
		} else {
			generatedChunks.add(chunkCoords);
		}
	}

	public void processAllLoadedChunks() {
		for (World world : Bukkit.getWorlds()) {
			for (Chunk loadedChunk : world.getLoadedChunks()) {
				Pair<Integer, Integer> chunkCoords = new Pair<>(loadedChunk.getX(), loadedChunk.getZ());
				String worldName = loadedChunk.getWorld().getName();
				Set<Pair<Integer, Integer>> generatedChunks = generatedChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>());

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
					Pair<Integer, Integer> chunkCoords = new Pair<>(loadedChunk.getX(), loadedChunk.getZ());
					String worldName = loadedChunk.getWorld().getName();
					Set<Pair<Integer, Integer>> generatedChunks = generatedChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>());

					if (!generatedChunks.contains(chunkCoords)) {
						chunksToProcess.add(loadedChunk);
					}
				}
			}
		}
		int totalChunks = chunksToProcess.size();
		int batchSize = Math.max(1, (totalChunks + 1) / 2);
		Bukkit.getScheduler().runTaskLater(plugin, () -> processChunksBatch(dimension, chunksToProcess, batchSize, 0, dimensionQueue), settings.getProcessDelay());
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
			Bukkit.getScheduler().runTaskLater(plugin, () -> processChunksBatch(dimension, chunksToProcess, batchSize, batchIndex + 1, dimensionQueue), settings.getProcessDelay());
		} else {
			processNextDimension(dimensionQueue);
		}
	}

	@FunctionalInterface
	interface MaterialSelector {
		Material selectMaterial(String worldName, String biomeName);
	}

	private Material selectMaterialWithoutBiomes(String worldName, String biomeName) {
		return materialManager.getRandomMaterialForWorld(worldName, biomeName);
	}

	private Material selectMaterialWithBiomes(String worldName, String biomeName) {
		return materialManager.getRandomMaterialForWorldMultiBiome(worldName, biomeName);
	}

	private void processChunk(Chunk chunk) {
		World world = chunk.getWorld();
		String worldName = world.getName();
		World.Environment dimension = world.getEnvironment();
		int xStart = chunk.getX() << 4, zStart = chunk.getZ() << 4;
		int minY, maxY;

		switch (dimension) {
		case NETHER:
			minY = settings.netherMinY();
			maxY = settings.netherMaxY();
			break;
		case THE_END:
			minY = settings.endMinY();
			maxY = settings.endMaxY();
			break;
		case NORMAL:
			minY = settings.normalMinY();
			maxY = settings.normalMaxY();
			break;
		default:
			minY = settings.defaultMinY();
			maxY = settings.defaultMaxY();
			break;
		}

		MaterialSelector materialSelector = hasBiomeHeaders() ? this::selectMaterialWithBiomes : this::selectMaterialWithoutBiomes;

		for (int x = xStart + 1; x <= xStart + 13; x += 4) {
			for (int z = zStart + 2; z <= zStart + 14; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					int chunkX = x & 0xF;
					int chunkZ = z & 0xF;
					int chunkY = Math.max(minY, Math.min(maxY, y));

					Block block = chunk.getBlock(chunkX, chunkY, chunkZ);
					String biomeName = block.getBiome().name();

					Material material = materialSelector.selectMaterial(worldName, biomeName);

					if (material != null) {
						block.setType(material, false);
						if (LEAVES.contains(material)) {
							handleLeaves(block);
						} else if (CROP_MATERIALS.contains(material)) {
							handleCrop(block);
						} else if (material == Material.CHORUS_FLOWER) {
							handleChorusFlower(block);
						} else if (material == Material.BAMBOO) {
							handleBamboo(block);
						} else if (material == Material.SPAWNER) {
							spawner.BlockInfo(block);
						} else if (material == Material.CHEST) {
							chest.loadChest(block);
						}
					}
				}
			}
		}
		generatedChunksByWorld.putIfAbsent(worldName, Collections.newSetFromMap(new HashMap<>()));
		generatedChunksByWorld.get(worldName).add(new Pair<>(chunk.getX(), chunk.getZ()));
	}

	public void handleLeaves(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Leaves) {
			Leaves leaves = (Leaves) blockData;
			leaves.setPersistent(true);
			block.setBlockData(leaves, false);
		}
	}

	public void handleCrop(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Ageable) {
			Ageable ageable = (Ageable) blockData;
			int maxAge = ageable.getMaximumAge();
			ageable.setAge(maxAge);
			block.setBlockData(ageable, false);
		} else if (block.getType() == Material.CAVE_VINES || block.getType() == Material.CAVE_VINES_PLANT) {
			CaveVines caveVines = (CaveVines) block.getBlockData();
			caveVines.setBerries(true);
			block.setBlockData(caveVines, true);
		}
	}

	public void handleChorusFlower(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Ageable) {
			Ageable ageable = (Ageable) blockData;
			int maxAge = ageable.getMaximumAge();
			ageable.setAge(Math.min(5, maxAge));
			block.setBlockData(ageable, false);
		}
	}

	public void handleBamboo(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Bamboo) {
			Bamboo bamboo = (Bamboo) blockData;
			bamboo.setStage(1);
			bamboo.setLeaves(getRandomLeafSize());
			block.setBlockData(bamboo, false);
		}
	}

	private Bamboo.Leaves getRandomLeafSize() {
		Bamboo.Leaves[] values = Bamboo.Leaves.values();
		int randomIndex = random.nextInt(values.length);
		return values[randomIndex];
	}

	public void regenerateAllLoadedChunks() {
		for (World world : Bukkit.getWorlds()) {
			for (Chunk loadedChunk : world.getLoadedChunks()) {
				processChunk(loadedChunk);
			}
		}
	}
}