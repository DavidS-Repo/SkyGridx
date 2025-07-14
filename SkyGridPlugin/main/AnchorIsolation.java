package main;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener that isolates respawn via respawn anchors in custom worlds
 */
public class AnchorIsolation implements Listener {
    private final JavaPlugin plugin;
    private final CustomAnchorManager anchorManager;
    private final TPRCommand tpr;

    public AnchorIsolation(CustomAnchorManager manager, TPRCommand tpr, JavaPlugin plugin) {
        this.anchorManager = manager;
        this.tpr = tpr;
        this.plugin = plugin;
    }

    /**
     * Remove stored spawn when anchor charge drops to zero
     */
    @EventHandler
    public void onChargeAnchor(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.RESPAWN_ANCHOR) return;
        World world = block.getWorld();
        if (!world.getName().startsWith(WorldManager.PREFIX)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Block b = world.getBlockAt(block.getX(), block.getY(), block.getZ());
                if (b.getType() != Material.RESPAWN_ANCHOR) return;
                RespawnAnchor data = (RespawnAnchor) b.getBlockData();
                if (data.getCharges() == 0) {
                    anchorManager.removeCustomAnchor(event.getPlayer().getUniqueId());
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Set player spawn when they right click a charged anchor without glowstone
     */
    @EventHandler
    public void onSetSpawnAnchor(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.RESPAWN_ANCHOR) return;
        World world = block.getWorld();
        if (!world.getName().startsWith(WorldManager.PREFIX)) return;
        if (event.getItem() != null && event.getItem().getType() == Material.GLOWSTONE) return;

        RespawnAnchor data = (RespawnAnchor) block.getBlockData();
        if (data.getCharges() > 0) {
            anchorManager.setCustomAnchor(event.getPlayer(), block.getLocation());
        }
    }

    /**
     * Remove stored spawn when anchor is broken
     */
    @EventHandler
    public void onAnchorBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.RESPAWN_ANCHOR) return;
        World world = block.getWorld();
        if (!world.getName().startsWith(WorldManager.PREFIX)) return;

        Location loc = block.getLocation();
        UUID toRemove = null;
        for (Map.Entry<UUID, Location> entry : anchorManager.getAllAnchors().entrySet()) {
            Location stored = entry.getValue();
            if (stored.getWorld().equals(loc.getWorld())
             && stored.getBlockX() == loc.getBlockX()
             && stored.getBlockY() == loc.getBlockY()
             && stored.getBlockZ() == loc.getBlockZ()) {
                toRemove = entry.getKey();
                break;
            }
        }
        if (toRemove != null) {
            anchorManager.removeCustomAnchor(toRemove);
        }
    }

    /**
     * Redirect respawn to custom anchor or default world spawn
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World deathWorld = player.getWorld();
        if (!deathWorld.getName().startsWith(WorldManager.PREFIX)) return;

        UUID id = player.getUniqueId();
        World targetWorld;
        Location targetLoc;

        if (anchorManager.hasCustomAnchor(id)) {
            targetLoc = anchorManager.getCustomAnchor(id);
            if (targetLoc == null) return;
            targetWorld = targetLoc.getWorld();
        } else {
            targetWorld = Bukkit.getWorld(WorldManager.PREFIX + "world");
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
                Block b = targetWorld.getBlockAt(
                    targetLoc.getBlockX(),
                    targetLoc.getBlockY(),
                    targetLoc.getBlockZ()
                );
                if (b.getType() != Material.RESPAWN_ANCHOR) {
                    anchorManager.removeCustomAnchor(id);
                } else {
                    RespawnAnchor d = (RespawnAnchor) b.getBlockData();
                    if (d.getCharges() == 0) {
                        anchorManager.removeCustomAnchor(id);
                    }
                }
            }
        }.runTaskLater(plugin, 2L);
    }
}