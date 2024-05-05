package main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class GrowthControl implements Listener {

	private final Plugin plugin;
	private Consumer<String> logger;

	public GrowthControl(Plugin plugin) {
		this.plugin = plugin;
		this.logger = message -> {}; // Empty logger by default
	}

	public void initialize() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getLogger().info("GrowthControl Enabled.");
	}

	public void enableLogging() {
		this.logger = plugin.getLogger()::info;
		logger.accept("GrowthControl logging enabled.");
	}

	public void disableLogging() {
		logger.accept("GrowthControl logging disabled.");
		this.logger = message -> {};
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockGrow(BlockGrowEvent event) {
		Block block = event.getBlock();
		Block locationBlock = block.getWorld().getBlockAt(block.getLocation().subtract(0, 1, 0));
		Material blockMaterial = locationBlock.getType();
		if (blockMaterial == Material.SUGAR_CANE || blockMaterial == Material.CACTUS) {
			Ageable ageable = (Ageable) locationBlock.getBlockData();
			int age = ageable.getAge();
			if (age == 15) {
				Block twoBlocksBelow = block.getWorld().getBlockAt(block.getLocation().subtract(0, 2, 0));
				Material materialTwoBelow = twoBlocksBelow.getType();

				if (materialTwoBelow == Material.AIR || materialTwoBelow == Material.VOID_AIR) {
					event.setCancelled(true);
					logger.accept("Cancelled growth event for block at: " + locationBlock.getLocation() + " For the Material: " + blockMaterial);
				} else {
					logger.accept("Event not cancelled. Block at: " + locationBlock.getLocation() + " met growth criteria but was not canceled.");
				}
			}
		}
	}
}