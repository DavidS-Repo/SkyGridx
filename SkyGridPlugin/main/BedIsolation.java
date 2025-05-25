package main;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BedIsolation implements Listener {
	private final JavaPlugin plugin;
	private final CustomBedManager bedManager;
	private final TPRCommand tpr;

	public BedIsolation(CustomBedManager manager, TPRCommand tpr, JavaPlugin plugin) {
		this.bedManager = manager;
		this.tpr = tpr;
		this.plugin = plugin;
	}

	@EventHandler
	public void onSetCustomBed(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (event.getClickedBlock() == null) return;

		Material mat = event.getClickedBlock().getType();
		if (!mat.name().endsWith("_BED")) return;

		World world = event.getClickedBlock().getWorld();
		// Only allow beds in custom worlds
		if (!world.getName().startsWith(WorldManager.PREFIX)) return;

		bedManager.setCustomBed(event.getPlayer(), event.getClickedBlock().getLocation());
	}

	@EventHandler
	public void onBedBreak(BlockBreakEvent event) {
		if (!event.getBlock().getType().name().endsWith("_BED")) return;

		Location loc = event.getBlock().getLocation();
		World world = loc.getWorld();

		// Only track bed breaks in custom worlds
		if (!world.getName().startsWith(WorldManager.PREFIX)) return;

		UUID toRemove = null;
		for (Map.Entry<UUID, Location> entry : bedManager.getAllBeds().entrySet()) {
			Location bed = entry.getValue();
			if (bed.getWorld().equals(world)
					&& bed.getBlockX() == loc.getBlockX()
					&& bed.getBlockY() == loc.getBlockY()
					&& bed.getBlockZ() == loc.getBlockZ()) {
				toRemove = entry.getKey();
				break;
			}
		}
		if (toRemove != null) {
			bedManager.removeCustomBed(toRemove);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCustomRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		World deathWorld = player.getWorld();
		if (!deathWorld.getName().startsWith(WorldManager.PREFIX)) return;

		UUID id = player.getUniqueId();
		World targetWorld;
		Location targetLoc;

		if (bedManager.hasCustomBed(id)) {
			targetLoc = bedManager.getCustomBed(id);
			if (targetLoc == null) return;
			targetWorld = targetLoc.getWorld();
		} else {
			String customName = WorldManager.PREFIX + "world";
			targetWorld = Bukkit.getWorld(customName);
			if (targetWorld == null) return;
			targetLoc = targetWorld.getSpawnLocation();
		}

		// tell Bukkit where to respawn immediately
		event.setRespawnLocation(new Location(
				targetWorld,
				targetLoc.getBlockX() + 0.5,
				targetLoc.getBlockY(),
				targetLoc.getBlockZ() + 0.5
				));

		// run your safe‐teleport a tick later
		new BukkitRunnable() {
			@Override
			public void run() {
				tpr.findNonAirBlock(
						player,
						targetWorld,
						targetLoc.getBlockX(),
						targetLoc.getBlockY(),
						targetLoc.getBlockZ(),
						true
						);
			}
		}.runTaskLater(plugin, 1L);
	}

}
