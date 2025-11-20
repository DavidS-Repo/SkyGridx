package main;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles chest open events for custom and vanilla loot.
 */
public class ChestOpenListener implements Listener {
	private final JavaPlugin plugin;
	private final ChestRegionData regionData;

	public ChestOpenListener(JavaPlugin plugin) {
		this.plugin = plugin;
		this.regionData = ChestRegionData.getInstance(plugin);
	}

	/**
	 * Populates chests on right click.
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getAction().toString().contains("RIGHT_CLICK_BLOCK")) {
			return;
		}

		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}

		if (!(block.getState() instanceof Chest chest)) {
			return;
		}

		ChestRegionData.ChestInfo info = regionData.getChestInfo(chest.getLocation());

		if (info != null) {
			if (info.type == ChestRegionData.ChestType.CUSTOM) {
				CustomChest.getInstance(plugin).populateChestOnOpen(chest, info);
				regionData.markChestLooted(chest.getLocation());
			} else if (info.type == ChestRegionData.ChestType.VANILLA) {
				LootTable table = plugin.getServer().getLootTable(NamespacedKey.fromString(info.lootTableKey));
				if (table != null) {
					chest.setLootTable(table);
					chest.update();
				}
				regionData.markChestLooted(chest.getLocation());
			}
			return;
		}

		Byte unpopulated = chest.getPersistentDataContainer()
				.get(CustomChest.UNPOPULATED_KEY, PersistentDataType.BYTE);

		if (unpopulated == null || unpopulated != (byte) 1) {
			return;
		}

		chest.getPersistentDataContainer().remove(CustomChest.UNPOPULATED_KEY);
		chest.update();

		CustomChest.getInstance(plugin).populateLegacyChestOnOpen(chest);
	}
}
