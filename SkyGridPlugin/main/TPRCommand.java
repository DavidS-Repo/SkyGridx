package main;

import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class TPRCommand implements CommandExecutor {
	private final JavaPlugin plugin;
	private final Object2ObjectOpenHashMap<UUID, Object2LongOpenHashMap<World.Environment>> lastTeleportTimes = new Object2ObjectOpenHashMap<>();
	private final Object2LongOpenHashMap<UUID> lastCommandTimes = new Object2LongOpenHashMap<>();
	private static final String ERROR_MESSAGE = ChatColor.RED + "Only players can use this command.";
	private static final World netherWorld = Bukkit.getWorld("world_nether");
	private static final World endWorld = Bukkit.getWorld("world_the_end");
	private static final World overworld = Bukkit.getWorld("world");
	private static final EnumSet<Material> DANGEROUSBLOCKS;
	private static final int Lx, Sx, Lz, Sz, Y, b2b, tprN, tprE, tprO;
	
	public TPRCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	static {
		DANGEROUSBLOCKS = PluginSettings.getDangerousBlocks();
		Lx = PluginSettings.getMaxX(); Sx = PluginSettings.getMinX(); Lz = PluginSettings.getMaxZ(); Sz = PluginSettings.getMinZ(); Y = PluginSettings.getDestinationY();
		b2b = PluginSettings.getb2bDelay(); tprN = PluginSettings.getTprNetherDelay(); tprE = PluginSettings.getTprEndDelay(); tprO = PluginSettings.getTprDelay();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ERROR_MESSAGE);
			return true;
		}
		Player player = (Player) sender;
		World world = getWorldByLabel(label, args);
		if (canTeleport(player, world)) {
			teleportPlayer(player, world);
		} else {
			long regularCooldown = getRemainingCooldown(player, world);
			long b2bCooldown = getRemainingB2BCooldown(player);
			if (regularCooldown > 0 && b2bCooldown > 0) {
				player.sendMessage(ChatColor.RED + "Wait " + regularCooldown + "s before this command.");
				player.sendMessage(ChatColor.RED + "Or, " + b2bCooldown + "s before any other tpr commands.");
			} else if (regularCooldown > 0) {
				player.sendMessage(ChatColor.RED + "Wait " + regularCooldown + "s before this command.");
			} else if (b2bCooldown > 0) {
				player.sendMessage(ChatColor.RED + "Wait " + b2bCooldown + "s before any other tpr commands.");
			}
		}
		return true;
	}

	private World getWorldByLabel(String label, String[] args) {
		if (args.length > 0) {
			return getWorldByName(args[0]);
		}
		switch (label.toLowerCase()) {
		case "tprn":
			return netherWorld;
		case "tpre":
			return endWorld;
		case "tpro":
			return overworld;
		default:
			return overworld;
		}
	}

	private World getWorldByName(String worldName) {
		switch (worldName.toLowerCase()) {
		case "nether":
			return netherWorld;
		case "end":
			return endWorld;
		case "overworld":
		default:
			return overworld;
		}
	}

	private boolean canTeleport(Player player, World world) {
		UUID playerId = player.getUniqueId();
		long currentTime = System.currentTimeMillis();
		if (!lastTeleportTimes.containsKey(playerId) && !lastCommandTimes.containsKey(playerId)) {
			return true;
		}
		long lastCommandTime = lastCommandTimes.getOrDefault(playerId, 0L);
		long b2bDelay = b2b * 1000L;

		if ((currentTime - lastCommandTime) < b2bDelay) {
			return false;
		}
		Object2LongOpenHashMap<World.Environment> environmentTimes = lastTeleportTimes.getOrDefault(playerId, new Object2LongOpenHashMap<>());
		long lastTeleportTime = environmentTimes.getOrDefault(world.getEnvironment(), 0L);
		int delay = getTeleportDelay(world);
		return (currentTime - lastTeleportTime) >= delay * 1000L;
	}

	private int getTeleportDelay(World world) {
		return switch (world.getEnvironment()) {
		case NETHER -> tprN;
		case THE_END -> tprE;
		default -> tprO;
		};
	}

	private void teleportPlayer(Player player, World world) {
		int randomX = ThreadLocalRandom.current().nextInt(Lx - Sx) + Sx;
		int randomZ = ThreadLocalRandom.current().nextInt(Lz - Sz) + Sz;
		findNonAirBlock(player, world, randomX, Y, randomZ, true);
	}

	private long getRemainingCooldown(Player player, World world) {
		UUID playerId = player.getUniqueId();
		if (!lastTeleportTimes.containsKey(playerId)) {
			return 0;
		}
		Object2LongOpenHashMap<World.Environment> environmentTimes = lastTeleportTimes.get(playerId);
		long lastTeleportTime = environmentTimes.getOrDefault(world.getEnvironment(), 0L);
		int delay = getTeleportDelay(world);
		return (lastTeleportTime + (delay * 1000L) - System.currentTimeMillis()) / 1000L;
	}

	private long getRemainingB2BCooldown(Player player) {
		UUID playerId = player.getUniqueId();
		long lastCommandTime = lastCommandTimes.getOrDefault(playerId, 0L);
		long b2bDelay = b2b * 1000L;
		return (lastCommandTime + b2bDelay - System.currentTimeMillis()) / 1000L;
	}

	private void findNonAirBlock(Player player, World world, int destinationX, int destinationY, int destinationZ, boolean isRegularTeleport) {
		int chunkX = destinationX >> 4;
		int chunkZ = destinationZ >> 4;
		world.loadChunk(chunkX, chunkZ, true);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!world.isChunkLoaded(chunkX, chunkZ)) {
					return;
				}
				for (int dx = -2; dx <= 2; dx++) {
					for (int dz = -2; dz <= 2; dz++) {
						for (int dy = 0; dy <= 10; dy++) {
							int x = destinationX + dx;
							int y = destinationY + dy;
							int z = destinationZ + dz;
							Block block = world.getBlockAt(x, y, z);
							if (!block.getType().isAir() && !isDangerousBlock(block.getType())) {
								if (isRegularTeleport) {
									UUID playerId = player.getUniqueId();
									Object2LongOpenHashMap<World.Environment> environmentTimes = lastTeleportTimes.getOrDefault(playerId, new Object2LongOpenHashMap<>());
									environmentTimes.put(world.getEnvironment(), System.currentTimeMillis());
									lastTeleportTimes.put(playerId, environmentTimes);
									lastCommandTimes.put(playerId, System.currentTimeMillis());
								}
								player.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));
								cancel();
								return;
							}
						}
					}
				}
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}

	public void teleportPlayerForFirstJoin(Player player, World world) {
		int randomX = ThreadLocalRandom.current().nextInt(Lx - Sx) + Sx;
		int randomZ = ThreadLocalRandom.current().nextInt(Lz - Sz) + Sz;
		findNonAirBlock(player, world, randomX, Y, randomZ, false);
	}

	private boolean isDangerousBlock(Material blockType) {
		return DANGEROUSBLOCKS.contains(blockType);
	}
}