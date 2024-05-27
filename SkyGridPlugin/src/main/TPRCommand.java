package main;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class TPRCommand implements CommandExecutor {
	private final Map<UUID, Map<World.Environment, Long>> lastTeleportTimes = new HashMap<>();
	private final Map<UUID, Long> lastCommandTimes = new HashMap<>();
	private static final String ERROR_MESSAGE = ChatColor.RED + ("Only players can use this command.");
	private final EnumSet<Material> DANGEROUSBLOCKS;


	private final PluginSettings settings;
	private final JavaPlugin plugin;

	public TPRCommand(PluginSettings settings, JavaPlugin plugin) {
		this.settings = settings;
		this.plugin = plugin;
		this.DANGEROUSBLOCKS = settings.getDangerousBlocks();
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
		return switch (label.toLowerCase()) {
		case "tprn" -> Bukkit.getWorld("world_nether");
		case "tpre" -> Bukkit.getWorld("world_the_end");
		case "tpro" -> Bukkit.getWorld("world");
		default -> Bukkit.getWorld("world");
		};
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
		case "overworld":
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

	private boolean canTeleport(Player player, World world) {
		UUID playerId = player.getUniqueId();
		long currentTime = System.currentTimeMillis();

		if (!lastTeleportTimes.containsKey(playerId) && !lastCommandTimes.containsKey(playerId)) {
			return true;
		}

		long lastCommandTime = lastCommandTimes.getOrDefault(playerId, 0L);
		long b2bDelay = settings.getb2bDelay() * 1000;

		if ((currentTime - lastCommandTime) < b2bDelay) {
			return false;
		}

		Map<World.Environment, Long> environmentTimes = lastTeleportTimes.getOrDefault(playerId, new HashMap<>());
		long lastTeleportTime = environmentTimes.getOrDefault(world.getEnvironment(), 0L);
		int delay = getTeleportDelay(world);

		return (currentTime - lastTeleportTime) >= delay * 1000;
	}

	private int getTeleportDelay(World world) {
		switch (world.getEnvironment()) {
		case NETHER:
			return settings.getTprNetherDelay();
		case THE_END:
			return settings.getTprEndDelay();
		default:
			return settings.getTprDelay();
		}
	}

	private void teleportPlayer(Player player, World world) {
		Random random = new Random();
		int randomX = random.nextInt(settings.getMaxX() - settings.getMinX()) + settings.getMinX();
		int randomZ = random.nextInt(settings.getMaxZ() - settings.getMinZ()) + settings.getMinZ();
		findNonAirBlock(player, world, randomX, settings.getDestinationY(), randomZ, true);
	}

	private long getRemainingCooldown(Player player, World world) {
		UUID playerId = player.getUniqueId();
		if (!lastTeleportTimes.containsKey(playerId)) {
			return 0;
		}
		Map<World.Environment, Long> environmentTimes = lastTeleportTimes.get(playerId);
		long lastTeleportTime = environmentTimes.getOrDefault(world.getEnvironment(), 0L);
		int delay = getTeleportDelay(world);
		return (lastTeleportTime + (delay * 1000) - System.currentTimeMillis()) / 1000;
	}

	private long getRemainingB2BCooldown(Player player) {
		UUID playerId = player.getUniqueId();
		long lastCommandTime = lastCommandTimes.getOrDefault(playerId, 0L);
		long b2bDelay = settings.getb2bDelay() * 1000;
		return (lastCommandTime + b2bDelay - System.currentTimeMillis()) / 1000;
	}

	private void findNonAirBlock(Player player, World world, int destinationX, int destinationY, int destinationZ, boolean isRegularTeleport) {
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
									if (isRegularTeleport) {
										UUID playerId = player.getUniqueId();
										Map<World.Environment, Long> environmentTimes = lastTeleportTimes.getOrDefault(playerId, new HashMap<>());
										environmentTimes.put(world.getEnvironment(), System.currentTimeMillis());
										lastTeleportTimes.put(playerId, environmentTimes);
										lastCommandTimes.put(playerId, System.currentTimeMillis());
									}
									cancel();
									return;
								}
							}
						}
					}
				}
				tickCount++;
			}
		}.runTaskTimer(plugin, 0L, 0L);
	}

	public void teleportPlayerForFirstJoin(Player player, World world) {
		Random random = new Random();
		int randomX = random.nextInt(settings.getMaxX() - settings.getMinX()) + settings.getMinX();
		int randomZ = random.nextInt(settings.getMaxZ() - settings.getMinZ()) + settings.getMinZ();
		findNonAirBlock(player, world, randomX, settings.getDestinationY(), randomZ, false);
	}

	private boolean isDangerousBlock(Material blockType) {
		return DANGEROUSBLOCKS.contains(blockType);
	}
}
