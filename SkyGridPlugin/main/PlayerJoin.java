package main;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
	private final TPRCommand tprCommand;

	public PlayerJoin(PluginSettings settings, TPRCommand tprCommand) {
		this.tprCommand = tprCommand;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore() && PluginSettings.isOnFistJoinEnabled()) {
			World customWorld = Bukkit.getWorld(WorldManager.PREFIX + "world");
			if (customWorld != null) {
				tprCommand.teleportPlayerForFirstJoin(player, customWorld);
			} else {
				Bukkit.getLogger().severe("Custom world not found: " + WorldManager.PREFIX + "world");
			}
		}
	}
}