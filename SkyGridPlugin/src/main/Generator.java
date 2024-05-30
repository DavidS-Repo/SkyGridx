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
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
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
import java.util.stream.Collectors;

public class Generator implements Listener {
	private final SkyGridPlugin plugin;
	private final PluginSettings settings;
	private final ThreadLocalRandom random;
	private final MaterialManager materialManager;
	private final Spawner spawner;
	private final CustomChest chest;
	private final Map<World.Environment, MinMaxSettings> environmentSettings;
	private static final Set<Material> CROP_MATERIALS;
	private static final Set<Material> LEAVES;
	private static final Set<World.Environment> DIMENSIONS_TO_PROCESS;

	static {
		DIMENSIONS_TO_PROCESS = EnumSet.of(
				World.Environment.NORMAL, World.Environment.NETHER, World.Environment.THE_END);

		CROP_MATERIALS = EnumSet.of(
				Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.TORCHFLOWER_CROP,
				Material.PITCHER_CROP, Material.NETHER_WART, Material.CAVE_VINES, Material.CAVE_VINES_PLANT, Material.SWEET_BERRY_BUSH
				);

		LEAVES = EnumSet.of(
				Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.CHERRY_LEAVES,
				Material.DARK_OAK_LEAVES, Material.FLOWERING_AZALEA_LEAVES, Material.JUNGLE_LEAVES,
				Material.MANGROVE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES
				);
	}

	public Generator(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.settings = new PluginSettings(plugin);
		this.random = ThreadLocalRandom.current();
		this.materialManager = new MaterialManager(plugin);
		this.spawner = Spawner.getInstance(plugin);
		this.chest = CustomChest.getInstance(plugin);
		this.environmentSettings = new HashMap<>();
		initializeEnvironmentSettings();
	}

	private void initializeEnvironmentSettings() {
		environmentSettings.put(World.Environment.NORMAL, new MinMaxSettings(settings.normalMinY(), settings.normalMaxY()));
		environmentSettings.put(World.Environment.NETHER, new MinMaxSettings(settings.netherMinY(), settings.netherMaxY()));
		environmentSettings.put(World.Environment.THE_END, new MinMaxSettings(settings.endMinY(), settings.endMaxY()));
		environmentSettings.put(World.Environment.CUSTOM, new MinMaxSettings(settings.defaultMinY(), settings.defaultMaxY()));
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
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				materialManager.loadMaterialsForWorldMultiBiome("world.txt");
				materialManager.loadMaterialsForWorldMultiBiome("world_nether.txt");
				materialManager.loadMaterialsForWorldMultiBiome("world_the_end.txt");
			});
		} else {
			plugin.getLogger().info("Enabling Basic material loader.");
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				materialManager.loadMaterialsForWorld("world.txt");
				materialManager.loadMaterialsForWorld("world_nether.txt");
				materialManager.loadMaterialsForWorld("world_the_end.txt");
			});
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
		if (event.isNewChunk()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> processChunk(chunk), settings.getProcessDelay());
		}
	}

	public void processAllLoadedChunks() {
		Bukkit.getWorlds().forEach(world -> Arrays.stream(world.getLoadedChunks()).forEach(this::processChunk));
	}

	public void processLoadedChunks() {
		Queue<World.Environment> dimensionQueue = new LinkedList<>(DIMENSIONS_TO_PROCESS);
		processNextDimension(dimensionQueue);
	}

	private void processNextDimension(Queue<World.Environment> dimensionQueue) {
		World.Environment dimension = dimensionQueue.poll();
		if (dimension == null) {
			return;
		}
		List<Chunk> chunksToProcess = Bukkit.getWorlds().stream()
				.filter(world -> world.getEnvironment() == dimension)
				.flatMap(world -> Arrays.stream(world.getLoadedChunks()))
				.collect(Collectors.toList());

		int totalChunks = chunksToProcess.size();
		int batchSize = Math.max(1, (totalChunks + 1) / 2);
		Bukkit.getScheduler().runTaskLater(plugin, () -> processChunksBatch(dimension, chunksToProcess, batchSize, 0, dimensionQueue), settings.getProcessDelay());
	}

	private void processChunksBatch(World.Environment dimension, List<Chunk> chunksToProcess, int batchSize, int batchIndex, Queue<World.Environment> dimensionQueue) {
		int totalChunks = chunksToProcess.size();
		int startIdx = batchIndex * batchSize;
		int endIdx = Math.min((batchIndex + 1) * batchSize, totalChunks);

		for (int i = startIdx; i < endIdx; i++) {
			processChunk(chunksToProcess.get(i));
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

	public void processChunk(Chunk chunk) {
		World world = chunk.getWorld();
		String worldName = world.getName();
		MinMaxSettings minMaxSettings = environmentSettings.getOrDefault(world.getEnvironment(), new MinMaxSettings(settings.defaultMinY(), settings.defaultMaxY()));
		int minY = minMaxSettings.getMinY();
		int maxY = minMaxSettings.getMaxY();

		MaterialSelector materialSelector = hasBiomeHeaders() ? this::selectMaterialWithBiomes : this::selectMaterialWithoutBiomes;

		int xStart = chunk.getX() << 4, zStart = chunk.getZ() << 4;

		for (int x = xStart + 1; x <= xStart + 13; x += 4) {
			for (int z = zStart + 2; z <= zStart + 14; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(x & 0xF, y, z & 0xF);
					String biomeName = block.getBiome().name();
					Material material = materialSelector.selectMaterial(worldName, biomeName);
					if (material != null) {
						setBlockTypeAndHandle(block, material);
					}
				}
			}
		}
	}

	private void setBlockTypeAndHandle(Block block, Material material) {
		Bukkit.getScheduler().runTask(plugin, () -> {
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
			} else if (material == Material.BEE_NEST || material == Material.BEEHIVE) {
				handleBeehive(block);
			}
		});
	}

	private void handleLeaves(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Leaves) {
			Leaves leaves = (Leaves) blockData;
			leaves.setPersistent(true);
			block.setBlockData(leaves, false);
		}
	}

	private void handleCrop(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Ageable) {
			Ageable ageable = (Ageable) blockData;
			ageable.setAge(ageable.getMaximumAge());
			block.setBlockData(ageable, false);
		}
		if (block.getType() == Material.CAVE_VINES || block.getType() == Material.CAVE_VINES_PLANT) {
			CaveVines caveVines = (CaveVines) block.getBlockData();
			caveVines.setBerries(true);
			block.setBlockData(caveVines, true);
		}
	}

	private void handleChorusFlower(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Ageable) {
			Ageable ageable = (Ageable) blockData;
			ageable.setAge(Math.min(5, ageable.getMaximumAge()));
			block.setBlockData(ageable, false);
		}
	}

	private void handleBamboo(Block block) {
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

	private void handleBeehive(Block block) {
		World world = block.getWorld();
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			for (int i = 0; i < 2; i++) {
				Bee bee = (Bee) world.spawnEntity(block.getLocation(), EntityType.BEE);
				bee.setHive(block.getLocation());
			}
		}, 0);
	}

	public void regenerateAllLoadedChunks() {
		Bukkit.getWorlds().forEach(world -> Arrays.stream(world.getLoadedChunks()) .forEach(this::processChunk));
	}
}
