package main;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;

public class ResourcePackManager implements Listener {

	private final String resourcePackURL = "https://www.dropbox.com/scl/fi/oxyjlfrt638mksgvb99zb/Fog.zip?rlkey=d0714prc4am2yeprw3ryoequ2&dl=1";
	private final String resourcePackHash = "68b3d33d76887ec0a64cada29a995f09240630fe";
	private final JavaPlugin plugin;
	private final ResourcePackRequest request;
	private volatile boolean enabled = false;

	public ResourcePackManager(JavaPlugin plugin) {
		this.plugin = plugin;
		ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
				.uri(URI.create(resourcePackURL))
				.hash(resourcePackHash)
				.build();
		this.request = ResourcePackRequest.resourcePackRequest()
				.packs(packInfo)
				.required(false)
				.build();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			applyResourcePackToAll();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (enabled) {
			Player player = event.getPlayer();
			applyResourcePack(player);
		}
	}

	private void applyResourcePack(Player player) {
		SkyGridScheduler.runEntity(player, plugin, () -> player.sendResourcePacks(request));
	}

	private void applyResourcePackToAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			applyResourcePack(player);
		}
	}
}
