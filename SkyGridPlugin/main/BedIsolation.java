package main;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
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
		if (!world.getName().startsWith(WorldManager.PREFIX)) return;

		// find the foot half no matter which half was clicked
		BlockData data = event.getClickedBlock().getBlockData();
		Location footLoc;
		if (data instanceof Bed) {
			Bed bed = (Bed) data;
			if (bed.getPart() == Bed.Part.HEAD) {
				footLoc = event.getClickedBlock()
						.getRelative(bed.getFacing().getOppositeFace())
						.getLocation();
			} else {
				footLoc = event.getClickedBlock().getLocation();
			}
			bedManager.setCustomBed(event.getPlayer(), footLoc);
		}
	}

	@EventHandler
	public void onBedBreak(BlockBreakEvent event) {
		if (!event.getBlock().getType().name().endsWith("_BED")) return;

		Location loc = event.getBlock().getLocation();
		World world = loc.getWorld();
		if (!world.getName().startsWith(WorldManager.PREFIX)) return;

		// convert broken block to its foot half
		BlockData data = event.getBlock().getBlockData();
		Location footLoc;
		if (data instanceof Bed) {
			Bed bed = (Bed) data;
			if (bed.getPart() == Bed.Part.HEAD) {
				footLoc = event.getBlock()
						.getRelative(bed.getFacing().getOppositeFace())
						.getLocation();
			} else {
				footLoc = event.getBlock().getLocation();
			}
		} else {
			footLoc = event.getBlock().getLocation();
		}

		// find matching stored bed and remove it
		UUID toRemove = null;
		for (Map.Entry<UUID, Location> entry : bedManager.getAllBeds().entrySet()) {
			Location stored = entry.getValue();
			if (stored.getWorld().equals(footLoc.getWorld())
					&& stored.getBlockX() == footLoc.getBlockX()
					&& stored.getBlockY() == footLoc.getBlockY()
					&& stored.getBlockZ() == footLoc.getBlockZ()) {
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

		event.setRespawnLocation(new Location(
				targetWorld,
				targetLoc.getBlockX() + 0.5,
				targetLoc.getBlockY(),
				targetLoc.getBlockZ() + 0.5
				));

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