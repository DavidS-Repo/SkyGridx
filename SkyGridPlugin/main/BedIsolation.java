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

public class BedIsolation implements Listener {
	private final CustomBedManager bedManager;
	private final TPRCommand tpr;

	public BedIsolation(CustomBedManager manager, TPRCommand tpr) {
		this.bedManager = manager;
		this.tpr = tpr;
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
	    CustomBedManager bedManager = this.bedManager;

	    if (bedManager.hasCustomBed(id)) {
	        Location loc = bedManager.getCustomBed(id);
	        if (loc == null) return;

	        World w = loc.getWorld();

	        // Use your teleport helper to find a safe spot near the bed
	        tpr.findNonAirBlock(
	            player,
	            w,
	            loc.getBlockX(),
	            loc.getBlockY(),
	            loc.getBlockZ(),
	            true
	        );

	    } else {
	        String customOver = WorldManager.PREFIX + "world";
	        World overWorld = Bukkit.getWorld(customOver);
	        if (overWorld == null) return;

	        Location spawn = overWorld.getSpawnLocation();

	        // Use teleport helper to find safe spot near custom overworld spawn
	        tpr.findNonAirBlock(
	            player,
	            overWorld,
	            spawn.getBlockX(),
	            spawn.getBlockY(),
	            spawn.getBlockZ(),
	            true
	        );
	    }
	}

}
