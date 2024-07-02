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
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public class Generator implements Listener {
	private final SkyGridPlugin plugin;
	private final ThreadLocalRandom random;
	private final MaterialManager materialManager;
	private final Spawner spawner;
	private final CustomChest chest;
	private final Int2ObjectMap<MinMaxSettings> environmentSettings;
	private static boolean hasBiomeHeaders;
	private static final ObjectOpenHashSet<Material> CROP_MATERIALS;
	private static final ObjectOpenHashSet<Material> LEAVES;
	private static final IntOpenHashSet DIMENSIONS_TO_PROCESS;

	static {
		DIMENSIONS_TO_PROCESS = new IntOpenHashSet(
				new int[]{World.Environment.NORMAL.ordinal(), World.Environment.NETHER.ordinal(), World.Environment.THE_END.ordinal()});

		CROP_MATERIALS = new ObjectOpenHashSet<>(
				EnumSet.of(
						Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.TORCHFLOWER_CROP,
						Material.PITCHER_CROP, Material.NETHER_WART, Material.CAVE_VINES, Material.CAVE_VINES_PLANT, Material.SWEET_BERRY_BUSH,
						Material.TWISTING_VINES, Material.TWISTING_VINES_PLANT
						));

		LEAVES = new ObjectOpenHashSet<>(
				EnumSet.of(
						Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.CHERRY_LEAVES,
						Material.DARK_OAK_LEAVES, Material.FLOWERING_AZALEA_LEAVES, Material.JUNGLE_LEAVES,
						Material.MANGROVE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES
						));
	}

	public Generator(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.random = ThreadLocalRandom.current();
		this.materialManager = new MaterialManager(plugin);
		this.spawner = Spawner.getInstance(plugin);
		this.chest = CustomChest.getInstance(plugin);
		this.environmentSettings = new Int2ObjectOpenHashMap<>();
		initializeEnvironmentSettings();
	}

	private void initializeEnvironmentSettings() {
		environmentSettings.put(World.Environment.NORMAL.ordinal(), new MinMaxSettings(PluginSettings.normalMinY(), PluginSettings.normalMaxY()));
		environmentSettings.put(World.Environment.NETHER.ordinal(), new MinMaxSettings(PluginSettings.netherMinY(), PluginSettings.netherMaxY()));
		environmentSettings.put(World.Environment.THE_END.ordinal(), new MinMaxSettings(PluginSettings.endMinY(), PluginSettings.endMaxY()));
		environmentSettings.put(World.Environment.CUSTOM.ordinal(), new MinMaxSettings(PluginSettings.defaultMinY(), PluginSettings.defaultMaxY()));
	}

	public void initialize() {
		registerEvents();
		checkBiomeHeaders();
		loadWorldMaterials();
		callOreGen();
	}

	private void registerEvents() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	private void loadWorldMaterials() {
		if (hasBiomeHeaders) {
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

	public void checkBiomeHeaders() {
		String[] fileNames = new String[]{"world.txt", "world_nether.txt", "world_the_end.txt"};
		for (String fileName : fileNames) {
			File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);
			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					if (reader.lines().anyMatch(line -> !line.trim().startsWith("#") && line.trim().startsWith("-"))) {
						hasBiomeHeaders = true;
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		hasBiomeHeaders = false;
	}

	private void callOreGen() {
		OreGen oreGen = new OreGen(plugin);
		plugin.getServer().getPluginManager().registerEvents(oreGen, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		if (event.isNewChunk()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> processChunk(event.getChunk()), PluginSettings.getProcessDelay());
		}
	}

	public void processLoadedChunks() {
		IntArrayFIFOQueue dimensionQueue = new IntArrayFIFOQueue();
		DIMENSIONS_TO_PROCESS.forEach(dimensionQueue::enqueue);
		processNextDimension(dimensionQueue);
	}

	private void processNextDimension(IntArrayFIFOQueue dimensionQueue) {
		if (dimensionQueue.isEmpty()) {
			return;
		}
		int dimensionOrdinal = dimensionQueue.dequeueInt();
		World.Environment dimension = World.Environment.values()[dimensionOrdinal];
		ObjectOpenHashSet<Chunk> chunksToProcess = new ObjectOpenHashSet<>();

		for (World world : Bukkit.getWorlds()) {
			if (world.getEnvironment() == dimension) {
				for (Chunk chunk : world.getLoadedChunks()) {
					chunksToProcess.add(chunk);
				}
			}
		}

		processChunksBatch(chunksToProcess);
		processNextDimension(dimensionQueue);
	}

	private void processChunksBatch(ObjectOpenHashSet<Chunk> chunksToProcess) {
		for (Chunk chunk : chunksToProcess) {
			processChunk(chunk);
		}
	}

	public void processChunk(Chunk chunk) {
		String worldName = chunk.getWorld().getName();
		int environmentOrdinal = chunk.getWorld().getEnvironment().ordinal();
		MinMaxSettings minMax = environmentSettings.get(environmentOrdinal);

		int minY = minMax.getMinY();
		int maxY = minMax.getMaxY();

		for (int xOffset = 1; xOffset <= 15; xOffset += 4) {
			for (int zOffset = 1; zOffset <= 15; zOffset += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(xOffset, y, zOffset);
					String biomeName = block.getBiome().name();
					Material material = materialManager.getRandomMaterialForWorld(worldName, biomeName);
					if (material != null) {
						setBlockTypeAndHandle(block, material);
					}
				}
			}
		}
	}

	private void setBlockTypeAndHandle(Block block, Material material) {
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
		} else if (material == Material.TRIAL_SPAWNER || material == Material.VAULT) {
			handleTrial(block);
		}
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
			if (blockData instanceof CaveVines) {
				CaveVines caveVines = (CaveVines) blockData;
				caveVines.setBerries(true);
				block.setBlockData(caveVines, true);
			} else {
				ageable.setAge(ageable.getMaximumAge());
				block.setBlockData(ageable, false);
			}
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
		return values[random.nextInt(values.length)];
	}

	private void handleBeehive(Block block) {
		World world = block.getWorld();
		for (int i = 0; i < 2; i++) {
			Bee bee = (Bee) world.spawnEntity(block.getLocation(), EntityType.BEE);
			bee.setHive(block.getLocation());
		}
	}

	private void handleTrial(Block block) {
		BlockData blockData = block.getBlockData();
		boolean setOminous = random.nextBoolean();
		if (blockData instanceof TrialSpawner) {
			TrialSpawner trialSpawner = (TrialSpawner) blockData;
			trialSpawner.setOminous(setOminous);
			block.setBlockData(trialSpawner, false);
		} else if (blockData instanceof Vault) {
			Vault vault = (Vault) blockData;
			vault.setOminous(setOminous);
			block.setBlockData(vault, false);
		}
	}

	public void regenerateAllLoadedChunks() {
		Bukkit.getWorlds().forEach(world -> {
			for (Chunk chunk : world.getLoadedChunks()) {
				processChunk(chunk);
			}
		});
	}

	private static class MinMaxSettings {
		private final int minY;
		private final int maxY;

		public MinMaxSettings(int minY, int maxY) {
			this.minY = minY;
			this.maxY = maxY;
		}

		public int getMinY() {
			return minY;
		}

		public int getMaxY() {
			return maxY;
		}
	}
}