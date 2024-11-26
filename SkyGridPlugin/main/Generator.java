package main;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Bee;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Generator implements Listener {
	private final SkyGridPlugin plugin;
	private final ThreadLocalRandom random;
	private final MaterialManager materialManager;
	private final Spawner spawner;
	private final CustomChest chest;
	private final Map<World.Environment, MinMaxSettings> environmentSettings;
	private static boolean hasBiomeHeaders;
	private static final Set<Material> CROP_MATERIALS;
	private static final Set<Material> LEAVES;
	private static final EnumSet<World.Environment> DIMENSIONS_TO_PROCESS;

	static {
		DIMENSIONS_TO_PROCESS = EnumSet.of(
				World.Environment.NORMAL,
				World.Environment.NETHER,
				World.Environment.THE_END
				);

		CROP_MATERIALS = EnumSet.of(
				Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.TORCHFLOWER_CROP,
				Material.PITCHER_CROP, Material.NETHER_WART, Material.CAVE_VINES, Material.CAVE_VINES_PLANT,
				Material.SWEET_BERRY_BUSH, Material.TWISTING_VINES, Material.TWISTING_VINES_PLANT
				);

		LEAVES = EnumSet.of(
				Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.CHERRY_LEAVES,
				Material.DARK_OAK_LEAVES, Material.FLOWERING_AZALEA_LEAVES, Material.JUNGLE_LEAVES,
				Material.MANGROVE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES
				);
	}

	public Generator(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.random = ThreadLocalRandom.current();
		this.materialManager = new MaterialManager(plugin);
		this.spawner = Spawner.getInstance(plugin);
		this.chest = CustomChest.getInstance(plugin);
		this.environmentSettings = new EnumMap<>(World.Environment.class);
		initializeEnvironmentSettings();
	}

	private void initializeEnvironmentSettings() {
		environmentSettings.put(World.Environment.NORMAL,
				new MinMaxSettings(PluginSettings.normalMinY(), PluginSettings.normalMaxY()));
		environmentSettings.put(World.Environment.NETHER,
				new MinMaxSettings(PluginSettings.netherMinY(), PluginSettings.netherMaxY()));
		environmentSettings.put(World.Environment.THE_END,
				new MinMaxSettings(PluginSettings.endMinY(), PluginSettings.endMaxY()));
		environmentSettings.put(World.Environment.CUSTOM,
				new MinMaxSettings(PluginSettings.defaultMinY(), PluginSettings.defaultMaxY()));
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
		String[] fileNames = {"world.txt", "world_nether.txt", "world_the_end.txt"};
		for (String fileName : fileNames) {
			File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);
			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					if (reader.lines().anyMatch(line -> !line.trim().startsWith("#") && line.trim().startsWith("-"))) {
						hasBiomeHeaders = true;
						return;
					}
				} catch (IOException e) {
					plugin.getLogger().severe("Error reading file " + fileName + ": " + e.getMessage());
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
			Bukkit.getScheduler().runTaskLater(plugin,
					() -> processChunk(event.getChunk()),
					PluginSettings.getProcessDelay());
		}
	}

	public void processLoadedChunks() {
		for (World.Environment dimension : DIMENSIONS_TO_PROCESS) {
			processDimensionChunks(dimension);
		}
	}

	private void processDimensionChunks(World.Environment dimension) {
		Set<Chunk> chunksToProcess = new HashSet<>();
		for (World world : Bukkit.getWorlds()) {
			if (world.getEnvironment() == dimension) {
				for (Chunk chunk : world.getLoadedChunks()) {
					chunksToProcess.add(chunk);
				}
			}
		}
		processChunksBatch(chunksToProcess);
	}

	private void processChunksBatch(Set<Chunk> chunksToProcess) {
		for (Chunk chunk : chunksToProcess) {
			processChunk(chunk);
		}
	}

	public void processChunk(Chunk chunk) {
		String worldName = chunk.getWorld().getName();
		World.Environment environment = chunk.getWorld().getEnvironment();
		MinMaxSettings minMax = environmentSettings.get(environment);

		int minY = minMax.minY();
		int maxY = minMax.maxY();

		for (int xOffset = 1; xOffset <= 15; xOffset += 4) {
			for (int zOffset = 1; zOffset <= 15; zOffset += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(xOffset, y, zOffset);
					String biomeName = block.getBiome().toString();
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
		if (blockData instanceof Leaves leaves) {
			leaves.setPersistent(true);
			block.setBlockData(leaves, false);
		}
	}

	private void handleCrop(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof CaveVines caveVines) {
			caveVines.setBerries(true);
			block.setBlockData(caveVines, true);
		} else if (blockData instanceof Ageable ageable) {
			ageable.setAge(ageable.getMaximumAge());
			block.setBlockData(ageable, false);
		}
	}

	private void handleChorusFlower(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Ageable ageable) {
			ageable.setAge(Math.min(5, ageable.getMaximumAge()));
			block.setBlockData(ageable, false);
		}
	}

	private void handleBamboo(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Bamboo bamboo) {
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
		Beehive beehive = (Beehive) block.getState();
		if (!beehive.isFull()) {
			for (int i = 0; i < 3; i++) {
				Bee newBee = beehive.getWorld().createEntity(block.getLocation(), Bee.class);
				beehive.addEntity(newBee);
			}
		}
		beehive.update();
	}

	private void handleTrial(Block block) {
		BlockData blockData = block.getBlockData();
		boolean setOminous = random.nextBoolean();
		if (blockData instanceof TrialSpawner trialSpawner) {
			trialSpawner.setOminous(setOminous);
			block.setBlockData(trialSpawner, false);
		} else if (blockData instanceof Vault vault) {
			vault.setOminous(setOminous);
			block.setBlockData(vault, false);
		}
	}

	public void regenerateAllLoadedChunks() {
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				processChunk(chunk);
			}
		}
	}

	private record MinMaxSettings(int minY, int maxY) {
	}
}