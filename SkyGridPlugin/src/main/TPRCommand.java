package main;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TPRCommand implements CommandExecutor {
	private final Map<UUID, Long> lastTeleportTimes = new HashMap<>();
	private final Set<Material> dangerousBlocks = new HashSet<>(Arrays.asList(
			Material.LAVA, Material.CACTUS, Material.FIRE, Material.WATER, Material.LAVA_CAULDRON,
			Material.MAGMA_BLOCK, Material.SWEET_BERRY_BUSH, Material.CAMPFIRE, Material.WITHER_ROSE,
			Material.SUGAR_CANE, Material.VINE, Material.WEEPING_VINES, Material.TWISTING_VINES,
			Material.POINTED_DRIPSTONE, Material.AIR, Material.SNOW, Material.GRASS, Material.CORNFLOWER, 
			Material.SUNFLOWER, Material.TORCHFLOWER, Material.FERN, Material.DANDELION, Material.POPPY,
			Material.SPORE_BLOSSOM, Material.HANGING_ROOTS, Material.BIG_DRIPLEAF, Material.SMALL_DRIPLEAF,
			Material.CAVE_VINES_PLANT, Material.TALL_GRASS, Material.SEAGRASS, Material.BROWN_MUSHROOM,
			Material.RED_MUSHROOM, Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.BEETROOTS,
			Material.KELP, Material.MELON_STEM, Material.NETHER_WART, Material.PUMPKIN_STEM, Material.TORCHFLOWER_CROP,
			Material.TWISTING_VINES, Material.WEEPING_VINES, Material.ROSE_BUSH, Material.PITCHER_PLANT, Material.PEONY,
			Material.LILAC, Material.LARGE_FERN, Material.TALL_SEAGRASS,Material.ALLIUM, Material.AZURE_BLUET, 
			Material.BLUE_ORCHID, Material.LILY_OF_THE_VALLEY,Material.OXEYE_DAISY, Material.RED_TULIP, Material.ORANGE_TULIP, 
			Material.WHITE_TULIP, Material.PINK_TULIP, Material.WITHER_ROSE, Material.DEAD_BUSH
			));

	private final PluginSettings settings;

	public TPRCommand(PluginSettings settings) {
		this.settings = settings;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
			return true;
		}

		Player player = (Player) sender;
		if (canTeleport(player)) {
			World world = Bukkit.getWorld("world");
			if (args.length > 0) {
				world = getWorldByName(args[0]);
			}
			teleportPlayer(player, world);
		} else {
			long secondsLeft = (lastTeleportTimes.get(player.getUniqueId()) + settings.getTprDelay() * 1000 - System.currentTimeMillis()) / 1000;
			player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before using this command again.");
		}
		return true;
	}

	private World getWorldByName(String worldName) {
		World world;
		switch (worldName.toLowerCase()) {
		case "nether":
			world = Bukkit.getWorld("world_nether");
			break;
		case "end":
			world = Bukkit.getWorld("world_the_end");
			break;
		default:
			world = Bukkit.getWorld("world");
			break;
		}
		if (world == null) {
			Bukkit.getLogger().warning("World '" + worldName + "' does not exist.");
			return Bukkit.getWorld("world");
		}
		return world;
	}

	private boolean canTeleport(Player player) {
		UUID playerId = player.getUniqueId();
		if (!lastTeleportTimes.containsKey(playerId)) {
			return true;
		}
		long lastTeleportTime = lastTeleportTimes.get(playerId);
		return (System.currentTimeMillis() - lastTeleportTime) >= settings.getTprDelay() * 1000;
	}

	private void teleportPlayer(Player player, World world) {
		Random random = new Random();
		int randomX = random.nextInt(settings.getMaxX() - settings.getMinX()) + settings.getMinX();
		int randomZ = random.nextInt(settings.getMaxZ() - settings.getMinZ()) + settings.getMinZ();
		findNonAirBlock(player, world, randomX, settings.getDestinationY(), randomZ);
	}

	private void findNonAirBlock(Player player, World world, int destinationX, int destinationY, int destinationZ) {
		int chunkX = destinationX >> 4;
		int chunkZ = destinationZ >> 4;
		world.loadChunk(chunkX, chunkZ, true);

		new BukkitRunnable() {
			private int tickCount = 0;

			@Override
			public void run() {
				if (tickCount >= 10) {
					for (int dx = -2; dx <= 2; dx++) {
						for (int dz = -2; dz <= 2; dz++) {
							for (int dy = 0; dy <= 10; dy++) {
								int x = destinationX + dx;
								int y = destinationY + dy;
								int z = destinationZ + dz;

								player.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));

								Block block = world.getBlockAt(x, y, z);
								if (!block.getType().isAir() && !isDangerousBlock(block.getType())) {
									lastTeleportTimes.put(player.getUniqueId(), System.currentTimeMillis());
									cancel();
									return;
								}
							}
						}
					}
				}
				tickCount++;
			}
		}.runTaskTimer(SkyGridPlugin.getPlugin(SkyGridPlugin.class), 0L, 0L);
	}

	private boolean isDangerousBlock(Material blockType) {
		return dangerousBlocks.contains(blockType);
	}
}