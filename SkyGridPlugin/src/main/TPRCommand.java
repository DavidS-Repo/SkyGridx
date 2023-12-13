package main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TPRCommand implements CommandExecutor {
	private final Map<Player, Long> lastTeleportTimes = new HashMap<>();
	private final Set<Material> dangerousBlocks = new HashSet<>(Arrays.asList(
			Material.LAVA, Material.CACTUS, Material.FIRE, Material.WATER, Material.LAVA_CAULDRON,
			Material.MAGMA_BLOCK, Material.SWEET_BERRY_BUSH, Material.CAMPFIRE, Material.WITHER_ROSE,
			Material.SUGAR_CANE, Material.VINE, Material.WEEPING_VINES, Material.TWISTING_VINES,
			Material.POINTED_DRIPSTONE
			));

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (canTeleport(player)) {
				World world = getWorldByName("world");
				if (args.length > 0) {
					world = getWorldByName(args[0]);
				}
				teleportPlayer(player, world);
			} else {
				long secondsLeft = (lastTeleportTimes.get(player) + 30 * 1000 - System.currentTimeMillis()) / 1000;
				player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before using this command again.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
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
			// Handle the case when the world does not exist
			Bukkit.getLogger().warning("World '" + worldName + "' does not exist.");
			return Bukkit.getWorld("world"); // Default to the Overworld
		}
		return world;
	}

	private boolean canTeleport(Player player) {
		if (!lastTeleportTimes.containsKey(player)) {
			return true;
		}
		long lastTeleportTime = lastTeleportTimes.get(player);
		return (System.currentTimeMillis() - lastTeleportTime) >= 30 * 1000;
	}

	private void teleportPlayer(Player player, World world) {
		Random random = new Random();
		int maxX = 29999884;
		int maxZ = 29999884;
		int minX = -29999884;
		int minZ = -29999884;
		int randomX = random.nextInt(maxX - minX) + minX;
		int randomZ = random.nextInt(maxZ - minZ) + minZ;
		int destinationX = randomX;
		int destinationZ = randomZ;
		int destinationY = 64;
		findNonAirBlock(player, world, destinationX, destinationY, destinationZ);
	}

	private void findNonAirBlock(Player player, World world, int destinationX, int destinationY, int destinationZ) {
		int chunkX = destinationX >> 4; // Calculate chunk X coordinate
		int chunkZ = destinationZ >> 4; // Calculate chunk Z coordinate

		// Preload the chunk
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

								// Teleport the player to the location
								player.teleport(new org.bukkit.Location(world, x + 0.5, y + 1, z + 0.5));

								// Check if the block is safe
								org.bukkit.block.Block block = world.getBlockAt(x, y, z);
								if (!block.getType().isAir() && !isDangerousBlock(block.getType())) {
									// Update the last teleport time for the player
									lastTeleportTimes.put(player, System.currentTimeMillis());
									cancel();
									return;
								}
							}
						}
					}
				}
				tickCount++;
			}
		}.runTaskTimer(SkyGridPlugin.getPlugin(SkyGridPlugin.class), 0L, 1L); // Check every tick
	}

	private boolean isDangerousBlock(Material blockType) {
		return dangerousBlocks.contains(blockType);
	}
}