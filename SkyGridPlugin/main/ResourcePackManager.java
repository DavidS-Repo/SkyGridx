package main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackManager implements Listener {

	private final String resourcePackURL = "https://www.dropbox.com/scl/fi/oxyjlfrt638mksgvb99zb/Fog.zip?rlkey=d0714prc4am2yeprw3ryoequ2&dl=1";
	private final String resourcePackHash = "68b3d33d76887ec0a64cada29a995f09240630fe";
	private boolean enabled = false;

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
		try {
			byte[] hashBytes = hexStringToByteArray(resourcePackHash);
			player.setResourcePack(resourcePackURL, hashBytes);
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + "Failed to apply the resource pack.");
			e.printStackTrace();
		}
	}

	private void applyResourcePackToAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			applyResourcePack(player);
		}
	}

	private byte[] hexStringToByteArray(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}
}