package main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public class TPRCommand implements CommandExecutor {
	private final JavaPlugin plugin;
	private final ConcurrentMap<UUID, ConcurrentMap<World.Environment, Long>> lastTeleportTimes = new ConcurrentHashMap<>();
	private final ConcurrentMap<UUID, Long> lastCommandTimes = new ConcurrentHashMap<>();
	private static final String ERROR_MESSAGE = "Only players can use this command.";
	private static final EnumSet<Material> DANGEROUSBLOCKS;
	private static final int Lx, Sx, Lz, Sz, Y, b2b, tprN, tprE, tprO;

	public TPRCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	static {
		DANGEROUSBLOCKS = PluginSettings.getDangerousBlocks();
		Lx = PluginSettings.getMaxX();
		Sx = PluginSettings.getMinX();
		Lz = PluginSettings.getMaxZ();
		Sz = PluginSettings.getMinZ();
		Y = PluginSettings.getDestinationY();
		b2b = PluginSettings.getb2bDelay();
		tprN = PluginSettings.getTprNetherDelay();
		tprE = PluginSettings.getTprEndDelay();
		tprO = PluginSettings.getTprDelay();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			Cc.sendS(sender, Cc.RED, ERROR_MESSAGE);
			return true;
		}
		World world = getWorldByLabel(label, args);
		if (world == null) {
			Cc.sendS(player, Cc.RED, "Target world not found!");
			return true;
		}
		if (canTeleport(player, world)) {
			teleportPlayer(player, world);
		} else {
			handleCooldownMessages(player, world);
		}
		return true;
	}

	private World getWorldByLabel(String label, String[] args) {
		if (args.length > 0) {
			return getWorldByName(args[0]);
		}
		return getWorldForEnvironment(getEnvironmentFromLabel(label));
	}

	private World.Environment getEnvironmentFromLabel(String label) {
		switch (label.toLowerCase()) {
		case "tprn":
			return World.Environment.NETHER;
		case "tpre":
			return World.Environment.THE_END;
		default:
			return World.Environment.NORMAL;
		}
	}

	private World getWorldForEnvironment(World.Environment env) {
		return WorldManager.getWorldForEnvironment(env);
	}

	private void handleCooldownMessages(Player player, World world) {
		long regularCooldown = getRemainingCooldown(player, world);
		long b2bCooldown = getRemainingB2BCooldown(player);

		if (regularCooldown > 0 && b2bCooldown > 0) {
			Cc.sendS(player, Cc.RED, "Wait " + regularCooldown + "s before this command.");
			Cc.sendS(player, Cc.RED, "Or, " + b2bCooldown + "s before any other tpr commands.");
		} else if (regularCooldown > 0) {
			Cc.sendS(player, Cc.RED, "Wait " + regularCooldown + "s before this command.");
		} else if (b2bCooldown > 0) {
			Cc.sendS(player, Cc.RED, "Wait " + b2bCooldown + "s before any other tpr commands.");
		}
	}

	private World getWorldByName(String worldNameArg) {
		World.Environment env = switch (worldNameArg.toLowerCase()) {
		case "nether" -> World.Environment.NETHER;
		case "end" -> World.Environment.THE_END;
		default -> World.Environment.NORMAL;
		};
		return getWorldForEnvironment(env);
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
		Map<World.Environment, Long> environmentTimes = lastTeleportTimes.get(playerId);
		if (environmentTimes == null) {
			return true;
		}
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
		Map<World.Environment, Long> environmentTimes = lastTeleportTimes.get(playerId);
		if (environmentTimes == null) {
			return 0;
		}
		long lastTeleportTime = environmentTimes.getOrDefault(world.getEnvironment(), 0L);
		int delay = getTeleportDelay(world);
		long remaining = lastTeleportTime + (delay * 1000L) - System.currentTimeMillis();
		if (remaining <= 0) {
			return 0;
		}
		return remaining / 1000L;
	}

	private long getRemainingB2BCooldown(Player player) {
		UUID playerId = player.getUniqueId();
		long lastCommandTime = lastCommandTimes.getOrDefault(playerId, 0L);
		long b2bDelay = b2b * 1000L;
		long remaining = lastCommandTime + b2bDelay - System.currentTimeMillis();
		if (remaining <= 0) {
			return 0;
		}
		return remaining / 1000L;
	}

	public boolean isRecentCommandTeleport(Player player) {
		UUID playerId = player.getUniqueId();
		long lastTime = lastCommandTimes.getOrDefault(playerId, 0L);
		if (lastTime == 0L) {
			return false;
		}
		long diff = System.currentTimeMillis() - lastTime;
		return diff >= 0 && diff <= 2000L;
	}

	public void findNonAirBlock(Player player, World world, int destinationX, int destinationY, int destinationZ, boolean isRegularTeleport) {
		int chunkX = destinationX >> 4;
		int chunkZ = destinationZ >> 4;
		Location chunkLocation = new Location(world, chunkX << 4, destinationY, chunkZ << 4);
		world.getChunkAtAsync(chunkLocation).thenRun(() ->
				SkyGridScheduler.runRegion(plugin, world, chunkX, chunkZ,
						() -> findSafeBlockAndTeleport(player, world, destinationX, destinationY, destinationZ, isRegularTeleport)))
		.exceptionally(error -> {
			plugin.getLogger().warning("Failed to load teleport chunk: " + error.getMessage());
			return null;
		});
	}

	private void findSafeBlockAndTeleport(Player player, World world, int destinationX, int destinationY, int destinationZ,
			boolean isRegularTeleport) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int dy = 0; dy <= 10; dy++) {
					int x = destinationX + dx;
					int y = destinationY + dy;
					int z = destinationZ + dz;
					Block block = world.getBlockAt(x, y, z);
					if (!block.getType().isAir() && !isDangerousBlock(block.getType())) {
						if (isRegularTeleport) {
							recordTeleport(player.getUniqueId(), world.getEnvironment());
						}
						player.teleportAsync(new Location(world, x + 0.5, y + 1, z + 0.5));
						return;
					}
				}
			}
		}
	}

	private void recordTeleport(UUID playerId, World.Environment environment) {
		long now = System.currentTimeMillis();
		ConcurrentMap<World.Environment, Long> environmentTimes = lastTeleportTimes.get(playerId);
		if (environmentTimes == null) {
			ConcurrentMap<World.Environment, Long> newTimes = new ConcurrentHashMap<>();
			environmentTimes = lastTeleportTimes.putIfAbsent(playerId, newTimes);
			if (environmentTimes == null) {
				environmentTimes = newTimes;
			}
		}
		environmentTimes.put(environment, now);
		lastCommandTimes.put(playerId, now);
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
