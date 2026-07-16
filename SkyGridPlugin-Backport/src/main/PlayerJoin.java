package main;

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
			World customWorld = WorldManager.getWorldForEnvironment(World.Environment.NORMAL);
			if (customWorld != null) {
				tprCommand.teleportPlayerForFirstJoin(player, customWorld);
			} else {
				player.getServer().getLogger().severe("SkyGrid overworld not found: "
						+ WorldManager.getWorldName(World.Environment.NORMAL));
			}
		}
	}
}
