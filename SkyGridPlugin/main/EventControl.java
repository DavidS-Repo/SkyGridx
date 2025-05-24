package main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;

public class EventControl implements Listener {

	private final Plugin plugin;
	private static final String INITIALIZE_MESSAGE = "EventControl Enabled.";
	private static final String ON_MESSAGE = Cc.logO(Cc.GREEN, "Event control logging enabled.");
	private static final String OFF_MESSAGE = Cc.logO(Cc.GREEN, "Event control logging disabled.");
	private boolean loggingEnabled = false;
	private static boolean 
	BlockGrowEventToggle = PluginSettings.isBlockGrowEvent(), BlockFadeEventToggle = PluginSettings.isBlockFadeEvent(), 
	BlockFromToEventToggle = PluginSettings.isBlockFromToEvent(), StructureGrowEventToggle = PluginSettings.isStructureGrowEvent(),
	BlockSpreadEventToggle = PluginSettings.isBlockSpreadEvent(), BlockIgniteEventToggle = PluginSettings.isBlockIgniteEvent(), 
	BlockFormEventToggle = PluginSettings.isBlockFormEvent(), EntityChangeBlockEventToggle = PluginSettings.isEntityChangeBlockEvent();

	private static final EnumSet<Material> GRAVITY_AFFECTED_BLOCKS= PluginSettings.getGravityAffectedBlocks();
	private static final EnumSet<Material> IS_FLOATING = EnumSet.of(Material.AIR, Material.VOID_AIR);
	private static final EnumSet<Material> FARMLAND_CROPS = EnumSet.of(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.MELON_STEM, Material.PUMPKIN_STEM);
	private static final EnumSet<Material> VINES = EnumSet.of(Material.CAVE_VINES, Material.CAVE_VINES_PLANT);


	public EventControl(Plugin plugin) {
		this.plugin = plugin;
	}

	public void initialize() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		Cc.logSB(INITIALIZE_MESSAGE);
	}

	public void enableLogging() {
		loggingEnabled = true;
		Cc.logSB(ON_MESSAGE);
	}

	public void disableLogging() {
		loggingEnabled = false;
		Cc.logSB(OFF_MESSAGE);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockGrow(BlockGrowEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if (BlockGrowEventToggle) {
			Block block = event.getBlock();
			Material cropType = event.getNewState().getType();
			if (FARMLAND_CROPS.contains(cropType)) {
				Block blockBelow = block.getRelative(0, -1, 0);
				if (blockBelow.getType() != Material.FARMLAND) {
					event.setCancelled(true);
				}
			} else {
				Block blockBelow = block.getRelative(0, -2, 0);
				if (IS_FLOATING.contains(blockBelow.getType())) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStructureGrow(StructureGrowEvent event) {
		if (!WorldManager.isCustomWorld(event.getLocation().getBlock())) return;
		if (StructureGrowEventToggle) {
			Block block = event.getLocation().getBlock();
			Block blockBelow = block.getRelative(0 , -1, 0);
			if (IS_FLOATING.contains(blockBelow.getType())) {
				event.setCancelled(true);
				if (loggingEnabled) {
					Cc.logSB("Cancelled sapling growth event at: " + block.getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockSpread(BlockSpreadEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if (BlockSpreadEventToggle) {
			Block sourceBlock = event.getSource();
			Block targetBlock = event.getBlock();
			Material sourceType = sourceBlock.getType();
			if (sourceType == Material.BAMBOO_SAPLING) {
				Block blockBelow = targetBlock.getRelative(0, -2, 0);
				if (IS_FLOATING.contains(blockBelow.getType())) {
					event.setCancelled(true);
					if (loggingEnabled) {
						Cc.logSB("Cancelled Bamboo sapling growth event at: " + targetBlock.getLocation());
					}
				}
			}
			else if (VINES.contains(sourceType)) {
				Block attachmentBlock = targetBlock.getRelative(0, 2, 0);
				if (IS_FLOATING.contains(attachmentBlock.getType())) {
					event.setCancelled(true);
					if (loggingEnabled) {
						Cc.logSB("Cancelled Cave Vines growth event: No solid attachment block at " + targetBlock.getLocation());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockForm(BlockFormEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if (BlockFormEventToggle) {
			Block block = event.getBlock();
			Material newStateType = event.getNewState().getType();
			Block belowBlock = block.getRelative(BlockFace.DOWN);
			Material belowMaterial = belowBlock.getType();

			if (newStateType == Material.SNOW && GRAVITY_AFFECTED_BLOCKS.contains(belowMaterial)) {
				event.setCancelled(true);
				if (loggingEnabled) {
					Cc.logSB("Cancelled snow form event at: " + block.getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFade(BlockFadeEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if (BlockFadeEventToggle) {
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
					Cc.logSB("Cancelled melt event at: " + block.getLocation());
				}
			} else if ((originalType == Material.FIRE || originalType == Material.SOUL_FIRE) && newType == Material.AIR) {
				for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
					if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
						return;
					}
				}
				event.setCancelled(true);
				if (loggingEnabled) {
					Cc.logSB("Cancelled fire extinguish event at: " + block.getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if(BlockIgniteEventToggle) {
			Block block = event.getBlock();
			for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
				if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
					return;
				}
			}
			event.setCancelled(true);
			if (loggingEnabled) {
				Cc.logSB("Cancelled ignition event at: " + block.getLocation());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if (EntityChangeBlockEventToggle) {
			if (event.getEntity() instanceof FallingBlock) {
				Block block = event.getBlock();
				for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
					if (!IS_FLOATING.contains(block.getRelative(face).getType())) {
						return;
					}
				}
				event.setCancelled(true);
				if (loggingEnabled) {
					Cc.logSB("Cancelled falling block event at: " + block.getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromTo(BlockFromToEvent event) {
		if (!WorldManager.isCustomWorld(event.getBlock())) return;
		if (BlockFromToEventToggle) {
			Block sourceBlock = event.getBlock();
			Material sourceType = sourceBlock.getType();

			if (sourceType == Material.WATER || sourceType == Material.LAVA) {
				boolean allFloating = true;
				for (BlockFace face : EnumSet.of(BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP)) {
					Block adjacentBlock = sourceBlock.getRelative(face);
					if (!IS_FLOATING.contains(adjacentBlock.getType())) {
						allFloating = false;
						break;
					}
				}

				if (allFloating) {
					event.setCancelled(true);
					if (loggingEnabled) {
						Cc.logSB("Cancelled BlockFromToEvent for " + sourceType.name() + " at: " + sourceBlock.getLocation());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String command = event.getMessage().toLowerCase().trim();

		if (command.startsWith("/locate") && WorldManager.isCustomWorld(player)) {
			String[] args = command.split(" ");
			if (args.length > 1) {
				String locateType = args[1];
				if (locateType.equals("structure") || locateType.equals("poi")) {
					event.setCancelled(true);
					Cc.sendS(player, Cc.RED, "Locating structures and POIs is disabled in this world");
					if (loggingEnabled) {
						Cc.logSB(Cc.logO(Cc.YELLOW, "Blocked locate " + locateType + " in custom world by ") + 
								Cc.logO(Cc.AQUA, player.getName()));
					}
				}
			}
		}
	}
}