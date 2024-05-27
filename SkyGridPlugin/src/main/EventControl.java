package main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;

public class EventControl implements Listener {

	private final Plugin plugin;
	private static final String INITIALIZE_MESSAGE = "EventControl Enabled.";
	private static final String ON_MESSAGE = ChatColor.GREEN + "Event control logging enabled.";
	private static final String OFF_MESSAGE = ChatColor.RED + "Event control logging disabled.";
	private boolean loggingEnabled = false;

	private final EnumSet<Material> GROWTH_BLOCKS;
	private final EnumSet<Material> GRAVITY_AFFECTED_BLOCKS;
	private static final EnumSet<Material> IS_FLOATING = EnumSet.of(
			Material.AIR, Material.VOID_AIR
			);

	public EventControl(Plugin plugin, PluginSettings settings) {
		this.plugin = plugin;
		this.GROWTH_BLOCKS = settings.getGrowthBlocks();
		this.GRAVITY_AFFECTED_BLOCKS = settings.getGravityAffectedBlocks();
	}

	public void initialize() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getLogger().info(INITIALIZE_MESSAGE);
	}

	public void enableLogging() {
		loggingEnabled = true;
		Bukkit.broadcastMessage(ON_MESSAGE);
	}

	public void disableLogging() {
		loggingEnabled = false;
		Bukkit.broadcastMessage(OFF_MESSAGE);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockGrow(BlockGrowEvent event) {
		Block block = event.getBlock();
		Block belowBlock = block.getRelative(BlockFace.DOWN);
		Material belowMaterial = belowBlock.getType();
		Block aboveBlock = block.getRelative(BlockFace.UP);
		Material aboveMaterial = aboveBlock.getType();
		Material materialTwoBelow = aboveBlock.getRelative(BlockFace.DOWN).getType();

		if ((GROWTH_BLOCKS.contains(aboveMaterial) || GROWTH_BLOCKS.contains(belowMaterial)) && IS_FLOATING.contains(materialTwoBelow)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockForm(BlockFormEvent event) {
		Block block = event.getBlock();
		Material newStateType = event.getNewState().getType();
		Block belowBlock = block.getRelative(BlockFace.DOWN);
		Material belowMaterial = belowBlock.getType();

		if (newStateType == Material.SNOW && GRAVITY_AFFECTED_BLOCKS.contains(belowMaterial)) {
			event.setCancelled(true);
			if (loggingEnabled) {
				plugin.getLogger().info("Cancelled snow form event at: " + block.getLocation());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFade(BlockFadeEvent event) {
		Block block = event.getBlock();
		Material originalType = event.getBlock().getType();
		Material newType = event.getNewState().getType();

		if (originalType == Material.ICE && newType == Material.WATER) {
			for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
				if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
					return;
				}
			}
			event.setCancelled(true);
			if (loggingEnabled) {
				plugin.getLogger().info("Cancelled melt event at: " + block.getLocation());
			}
		} else if ((originalType == Material.FIRE || originalType == Material.SOUL_FIRE) && newType == Material.AIR) {
			for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
				if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
					return;
				}
			}
			event.setCancelled(true);
			if (loggingEnabled) {
				plugin.getLogger().info("Cancelled fire extinguish event at: " + block.getLocation());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Block block = event.getBlock();

		for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
			if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
				return;
			}
		}

		event.setCancelled(true);
		if (loggingEnabled) {
			plugin.getLogger().info("Cancelled ignition event at: " + block.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntity() instanceof FallingBlock) {
			Block block = event.getBlock();
			Material blockType = block.getType();

			if (GRAVITY_AFFECTED_BLOCKS.contains(blockType)) {
				for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
					if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
						return;
					}
				}
				event.setCancelled(true);
				if (loggingEnabled) {
					plugin.getLogger().info("Cancelled falling block event at: " + block.getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromTo(BlockFromToEvent event) {
		Block block = event.getBlock();
		Material blockType = block.getType();
		Block toBlock = event.getToBlock();

		if (blockType == Material.WATER || blockType == Material.LAVA) {
			Block adjustedToBlock = toBlock.getRelative(BlockFace.UP);

			for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
				if (!IS_FLOATING.contains(adjustedToBlock.getRelative(face).getType())) {
					return;
				}
			}

			event.setCancelled(true);
			if (loggingEnabled) {
				plugin.getLogger().info("Cancelled BlockFromToEvent for " + blockType.name() + " at: " + toBlock.getLocation());
			}
		}
	}
}
