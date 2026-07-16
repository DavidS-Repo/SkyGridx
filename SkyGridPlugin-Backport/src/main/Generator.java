package main;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates and processes Skygrid chunks and blocks.
 */
public class Generator implements Listener {
	private final SkyGridPlugin plugin;
	private final MaterialManager materialManager;
	private final Spawner spawner;
	private final CustomChest chest;
	private final Map<World.Environment, MinMaxSettings> environmentSettings;

	private static final EnumSet<World.Environment> DIMENSIONS_TO_PROCESS;
	private static final Set<Material> CROP_MATERIALS;

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
	}

	public Generator(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.materialManager = new MaterialManager(plugin);
		this.spawner = Spawner.getInstance(plugin);
		this.chest = CustomChest.getInstance(plugin);
		this.environmentSettings = new EnumMap<>(World.Environment.class);
		initEnvSettings();
	}

	/**
	 * Sets min and max Y per environment.
	 */
	private void initEnvSettings() {
		environmentSettings.put(World.Environment.NORMAL,
				new MinMaxSettings(PluginSettings.normalMinY(), PluginSettings.normalMaxY()));
		environmentSettings.put(World.Environment.NETHER,
				new MinMaxSettings(PluginSettings.netherMinY(), PluginSettings.netherMaxY()));
		environmentSettings.put(World.Environment.THE_END,
				new MinMaxSettings(PluginSettings.endMinY(), PluginSettings.endMaxY()));
		environmentSettings.put(World.Environment.CUSTOM,
				new MinMaxSettings(PluginSettings.defaultMinY(), PluginSettings.defaultMaxY()));
	}

	/**
	 * Sets up worlds, listeners, and material data.
	 */
	public void initialize() {
		materialManager.reloadAll();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		WorldManager.setupWorlds(plugin);
		registerOreGen();
	}

	/**
	 * Registers ore generation listener.
	 */
	private void registerOreGen() {
		OreGen oreGen = new OreGen(plugin);
		plugin.getServer().getPluginManager().registerEvents(oreGen, plugin);
	}

	/**
	 * Checks if a world is a Skygrid world.
	 */
	/**
	 * Handles chunk load event for Skygrid worlds.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		World world = event.getWorld();
		String worldName = world.getName();
		if (!WorldManager.isCustomWorld(world)) {
			return;
		}

		Chunk chunk = event.getChunk();

		if (event.isNewChunk()) {
			int processDelay = PluginSettings.getProcessDelay();
			if (processDelay <= 0) {
				SkyGridScheduler.runRegion(plugin, world, chunk.getX(), chunk.getZ(),
						() -> processChunk(chunk, world, worldName));
			} else {
				SkyGridScheduler.runRegionLater(plugin, world, chunk.getX(), chunk.getZ(),
						() -> processChunk(chunk, world, worldName),
						processDelay);
			}
		}
	}

	/**
	 * Processes all loaded chunks in Skygrid worlds.
	 */
	public void processLoadedChunks() {
		for (World.Environment dim : DIMENSIONS_TO_PROCESS) {
			Set<Chunk> toProcess = new HashSet<>();
			for (World world : plugin.getServer().getWorlds()) {
				if (world.getEnvironment() == dim && WorldManager.isCustomWorld(world)) {
					for (Chunk chunk : world.getLoadedChunks()) {
						toProcess.add(chunk);
					}
				}
			}
			for (Chunk chunk : toProcess) {
				World world = chunk.getWorld();
				SkyGridScheduler.runRegion(plugin, world, chunk.getX(), chunk.getZ(),
						() -> processChunk(chunk, world, world.getName()));
			}
		}
	}

	/**
	 * Regenerates all loaded chunks in all Skygrid worlds.
	 */
	public void regenerateAllLoadedChunks() {
		for (World world : plugin.getServer().getWorlds()) {
			if (!WorldManager.isCustomWorld(world)) continue;
			for (Chunk chunk : world.getLoadedChunks()) {
				SkyGridScheduler.runRegion(plugin, world, chunk.getX(), chunk.getZ(),
						() -> processChunk(chunk, world, world.getName()));
			}
		}
	}

	/**
	 * Regenerates a mini grid inside a chunk with given material distribution.
	 */
	public void regenerateMiniChunk(Chunk chunk, MaterialManager.MaterialDistribution dist) {
		World world = chunk.getWorld();
		MinMaxSettings mm = environmentSettings.get(world.getEnvironment());
		int minY = mm.minY;
		int maxY = mm.maxY;

		List<ChestRegionData.ChestRecord> chestRecords = new ArrayList<>();

		for (int x = 1; x <= 15; x += 4) {
			for (int z = 1; z <= 15; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(x, y, z);
					setGridBlockAndHandle(block, dist.next(), chestRecords);
				}
			}
		}

		if (!chestRecords.isEmpty()) {
			ChestRegionData.getInstance(plugin).registerChests(chestRecords);
		}
	}

	/**
	 * Fills a chunk with grid blocks for Skygrid.
	 */
	private void processChunk(Chunk chunk, World world, String worldName) {
		World.Environment env = world.getEnvironment();
		MinMaxSettings mm = environmentSettings.get(env);
		int minY = mm.minY;
		int maxY = mm.maxY;

		List<ChestRegionData.ChestRecord> chestRecords = new ArrayList<>();

		for (int x = 1; x <= 15; x += 4) {
			for (int z = 1; z <= 15; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(x, y, z);
					String biome = BiomeKeyUtil.fromBiome(block.getBiome());
					MaterialManager.GridBlock gridBlock = materialManager.getRandomGridBlockForWorld(worldName, biome);
					if (gridBlock != null) {
						setGridBlockAndHandle(block, gridBlock, chestRecords);
					}
				}
			}
		}

		if (!chestRecords.isEmpty()) {
			ChestRegionData.getInstance(plugin).registerChests(chestRecords);
		}
	}

	/**
	 * Sets a block type and applies special handling for some types.
	 */
	private void setBlockTypeAndHandle(Block block, Material mat, List<ChestRegionData.ChestRecord> chestRecords) {
		setBlockTypeAndHandle(block, mat, chestRecords, null);
	}

	/**
	 * Sets a parent grid block, then places any configured face attachments.
	 */
	private void setGridBlockAndHandle(Block block, MaterialManager.GridBlock gridBlock, List<ChestRegionData.ChestRecord> chestRecords) {
		setBlockTypeAndHandle(block, gridBlock.material(), chestRecords);

		MaterialManager.FaceAttachment[] attachments = gridBlock.attachments();
		for (MaterialManager.FaceAttachment attachment : attachments) {
			if (attachment.face() != BlockFace.UP) {
				setAttachmentBlock(block, attachment, chestRecords);
			}
		}
		for (MaterialManager.FaceAttachment attachment : attachments) {
			if (attachment.face() == BlockFace.UP) {
				setAttachmentBlock(block, attachment, chestRecords);
			}
		}
	}

	private void setAttachmentBlock(Block parentBlock, MaterialManager.FaceAttachment attachment, List<ChestRegionData.ChestRecord> chestRecords) {
		Block attachmentBlock = parentBlock.getRelative(attachment.face());
		if (!isWithinBuildHeight(attachmentBlock)) {
			return;
		}
		setBlockTypeAndHandle(attachmentBlock, attachment.material(), chestRecords, attachment.face());
	}

	private boolean isWithinBuildHeight(Block block) {
		int y = block.getY();
		World world = block.getWorld();
		return y >= world.getMinHeight() && y < world.getMaxHeight();
	}

	private void setBlockTypeAndHandle(Block block, Material mat, List<ChestRegionData.ChestRecord> chestRecords, BlockFace attachmentFace) {
		block.setType(mat, false);
		if (attachmentFace != null) {
			orientAttachment(block, attachmentFace);
		}

		if (block.getBlockData() instanceof Leaves) {
			handleLeaves(block);
		} else if (CROP_MATERIALS.contains(mat)) {
			handleCrop(block);
		} else if (mat == Material.CHORUS_FLOWER) {
			handleChorusFlower(block);
		} else if (mat == Material.BAMBOO) {
			handleBamboo(block);
		} else if (mat == Material.SPAWNER) {
			spawner.BlockInfo(block);
		} else if (isChestMaterial(mat)) {
			chest.loadChest(block, chestRecords);
		} else if (mat == Material.BEE_NEST || mat == Material.BEEHIVE) {
			handleBeehive(block);
		} else if (mat == Material.TRIAL_SPAWNER || mat == Material.VAULT) {
			handleTrial(block);
		}

		if (mat == Material.END_PORTAL_FRAME || mat == Material.END_PORTAL) {
			plugin.getPortalManager().addPortal(block.getLocation());
		}
	}

	private boolean isChestMaterial(Material material) {
		return material == Material.CHEST || material.name().endsWith("COPPER_CHEST");
	}

	private void orientAttachment(Block block, BlockFace attachmentFace) {
		BlockData data = block.getBlockData();
		boolean changed = false;

		if (data instanceof FaceAttachable attachable) {
			if (attachmentFace == BlockFace.UP) {
				attachable.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
			} else if (attachmentFace == BlockFace.DOWN) {
				attachable.setAttachedFace(FaceAttachable.AttachedFace.CEILING);
			} else {
				attachable.setAttachedFace(FaceAttachable.AttachedFace.WALL);
			}
			changed = true;
		}

		if (data instanceof Directional directional && directional.getFaces().contains(attachmentFace)) {
			directional.setFacing(attachmentFace);
			changed = true;
		}

		if (changed) {
			block.setBlockData(data, false);
		}
	}

	/**
	 * Sets leaves to persistent to avoid decay.
	 */
	private void handleLeaves(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Leaves leaves) {
			leaves.setPersistent(true);
			block.setBlockData(leaves, false);
		}
	}

	/**
	 * Sets crop blocks to a grown state.
	 */
	private void handleCrop(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof CaveVines vines) {
			vines.setBerries(true);
			block.setBlockData(vines, true);
		} else if (data instanceof Ageable age) {
			age.setAge(age.getMaximumAge());
			block.setBlockData(age, false);
		}
	}

	/**
	 * Sets chorus flowers to a mid growth state.
	 */
	private void handleChorusFlower(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Ageable age) {
			age.setAge(Math.min(5, age.getMaximumAge()));
			block.setBlockData(age, false);
		}
	}

	/**
	 * Adjusts bamboo growth state and leaves.
	 */
	private void handleBamboo(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Bamboo bamboo) {
			bamboo.setStage(1);
			bamboo.setLeaves(getRandomLeafSize());
			block.setBlockData(bamboo, false);
		}
	}

	/**
	 * Picks a random bamboo leaf size.
	 */
	private Bamboo.Leaves getRandomLeafSize() {
		Bamboo.Leaves[] vals = Bamboo.Leaves.values();
		return vals[ThreadLocalRandom.current().nextInt(vals.length)];
	}

	/**
	 * Fills beehives with bees.
	 */
	private void handleBeehive(Block block) {
		Beehive hive = (Beehive) block.getState();
		if (!hive.isFull()) {
			for (int i = 0; i < 3; i++) {
				Bee bee = hive.getWorld().createEntity(block.getLocation(), Bee.class);
				hive.addEntity(bee);
			}
		}
		hive.update();
	}

	/**
	 * Sets trial spawners and vaults to ominous or not.
	 */
	private void handleTrial(Block block) {
		BlockData data = block.getBlockData();
		boolean ominous = ThreadLocalRandom.current().nextBoolean();
		if (data instanceof TrialSpawner ts) {
			ts.setOminous(ominous);
			block.setBlockData(ts, false);
		} else if (data instanceof Vault v) {
			v.setOminous(ominous);
			block.setBlockData(v, false);
		}
	}

	private record MinMaxSettings(int minY, int maxY) {}
}
