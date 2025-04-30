package main;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestOpenListener implements Listener {

	private final JavaPlugin plugin;

	public ChestOpenListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().toString().contains("RIGHT_CLICK_BLOCK")) {
			Block block = event.getClickedBlock();
			if (block != null && block.getState() instanceof Chest chest) {
				Byte unpopulated = chest.getPersistentDataContainer()
						.get(CustomChest.UNPOPULATED_KEY, PersistentDataType.BYTE);

				if (unpopulated != null && unpopulated == (byte) 1) {
					// Remove the flag so we don't populate again
					chest.getPersistentDataContainer().remove(CustomChest.UNPOPULATED_KEY);
					chest.update();

					// Populate with custom loot
					CustomChest.getInstance(plugin).populateChestOnOpen(chest);
				}
			}
		}
	}
}