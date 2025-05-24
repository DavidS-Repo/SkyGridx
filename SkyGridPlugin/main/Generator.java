package main;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Generator implements Listener {
	private static final String PREFIX = "skygridx_";

	private final SkyGridPlugin plugin;
	private final ThreadLocalRandom random;
	private final MaterialManager materialManager;
	private final Spawner spawner;
	private final CustomChest chest;
	private final Map<World.Environment, MinMaxSettings> environmentSettings;

	private static final EnumSet<World.Environment> DIMENSIONS_TO_PROCESS;
	private static final Set<Material> CROP_MATERIALS;
	private static final Set<Material> LEAVES;

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
				Material.MANGROVE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.PALE_OAK_LEAVES
				);
	}

	public Generator(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.random = ThreadLocalRandom.current();
		this.materialManager = new MaterialManager(plugin);
		this.spawner = Spawner.getInstance(plugin);
		this.chest = CustomChest.getInstance(plugin);
		this.environmentSettings = new EnumMap<>(World.Environment.class);
		initEnvSettings();
	}

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

	public void initialize() {
		WorldManager.setupWorlds(plugin);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		loadWorldMaterialsAsync();
		registerOreGen();
	}

	private void loadWorldMaterialsAsync() {
		CompletableFuture.runAsync(() -> {
			plugin.getLogger().info("Enabling Advanced material loader.");
			materialManager.loadMaterialsForWorld("world.yml");
			materialManager.loadMaterialsForWorld("world_nether.yml");
			materialManager.loadMaterialsForWorld("world_the_end.yml");
		}).exceptionally(th -> {
			plugin.getLogger().severe("Failed to load materials: " + th.getMessage());
			return null;
		});
	}

	private void registerOreGen() {
		OreGen oreGen = new OreGen(plugin);
		plugin.getServer().getPluginManager().registerEvents(oreGen, plugin);
	}

	private boolean isSkygridWorld(String name) {
		return name.startsWith(PREFIX);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		World world = event.getWorld();
		String worldName = world.getName();

		if (!isSkygridWorld(worldName)) {
			return;
		}

		if (event.isNewChunk()) {
			Bukkit.getScheduler().runTaskLater(
					plugin,
					() -> processChunk(event.getChunk(), world, worldName),
					PluginSettings.getProcessDelay()
					);
		}
	}

	public void processLoadedChunks() {
		for (World.Environment dim : DIMENSIONS_TO_PROCESS) {
			Set<Chunk> toProcess = new HashSet<>();
			for (World world : Bukkit.getWorlds()) {
				if (world.getEnvironment() == dim && isSkygridWorld(world.getName())) {
					for (Chunk chunk : world.getLoadedChunks()) {
						toProcess.add(chunk);
					}
				}
			}
			for (Chunk chunk : toProcess) {
				processChunk(chunk, chunk.getWorld(), chunk.getWorld().getName());
			}
		}
	}

	public void regenerateAllLoadedChunks() {
		for (World world : Bukkit.getWorlds()) {
			if (!isSkygridWorld(world.getName())) continue;
			for (Chunk chunk : world.getLoadedChunks()) {
				processChunk(chunk, world, world.getName());
			}
		}
	}

	public void regenerateMiniChunk(Chunk chunk, MaterialManager.MaterialDistribution dist) {
		World world = chunk.getWorld();
		MinMaxSettings mm = environmentSettings.get(world.getEnvironment());
		int minY = mm.minY;
		int maxY = mm.maxY;

		for (int x = 1; x <= 15; x += 4) {
			for (int z = 1; z <= 15; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(x, y, z);
					setBlockTypeAndHandle(block, dist.next());
				}
			}
		}
	}

	private void processChunk(Chunk chunk, World world, String worldName) {
		World.Environment env = world.getEnvironment();
		MinMaxSettings mm = environmentSettings.get(env);
		int minY = mm.minY;
		int maxY = mm.maxY;

		for (int x = 1; x <= 15; x += 4) {
			for (int z = 1; z <= 15; z += 4) {
				for (int y = minY; y <= maxY; y += 4) {
					Block block = chunk.getBlock(x, y, z);
					String biome = block.getBiome().toString();
					Material mat = materialManager.getRandomMaterialForWorld(worldName, biome);
					if (mat != null) {
						setBlockTypeAndHandle(block, mat);
					}
				}
			}
		}
	}

	private void setBlockTypeAndHandle(Block block, Material mat) {
		BlockState state = block.getState();
		if (state instanceof TileState) {
			block.setType(Material.AIR, false);
		}
		block.setType(mat, false);

		if (LEAVES.contains(mat)) {
			handleLeaves(block);
		} else if (CROP_MATERIALS.contains(mat)) {
			handleCrop(block);
		} else if (mat == Material.CHORUS_FLOWER) {
			handleChorusFlower(block);
		} else if (mat == Material.BAMBOO) {
			handleBamboo(block);
		} else if (mat == Material.SPAWNER) {
			spawner.BlockInfo(block);
		} else if (mat == Material.CHEST) {
			chest.loadChest(block);
		} else if (mat == Material.BEE_NEST || mat == Material.BEEHIVE) {
			handleBeehive(block);
		} else if (mat == Material.TRIAL_SPAWNER || mat == Material.VAULT) {
			handleTrial(block);
		}
		if (mat == Material.END_PORTAL_FRAME || mat == Material.END_PORTAL) {
			plugin.getPortalManager().addPortal(block.getLocation());
		}
	}

	private void handleLeaves(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Leaves leaves) {
			leaves.setPersistent(true);
			block.setBlockData(leaves, false);
		}
	}

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

	private void handleChorusFlower(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Ageable age) {
			age.setAge(Math.min(5, age.getMaximumAge()));
			block.setBlockData(age, false);
		}
	}

	private void handleBamboo(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Bamboo bamboo) {
			bamboo.setStage(1);
			bamboo.setLeaves(getRandomLeafSize());
			block.setBlockData(bamboo, false);
		}
	}

	private Bamboo.Leaves getRandomLeafSize() {
		Bamboo.Leaves[] vals = Bamboo.Leaves.values();
		return vals[random.nextInt(vals.length)];
	}

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

	private void handleTrial(Block block) {
		BlockData data = block.getBlockData();
		boolean ominous = random.nextBoolean();
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