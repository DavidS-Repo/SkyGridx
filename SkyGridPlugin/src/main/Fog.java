package main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class Fog implements CommandExecutor {

	private static final BossBar fogBossBar = Bukkit.createBossBar(ChatColor.WHITE + "", BarColor.WHITE, BarStyle.SOLID, BarFlag.CREATE_FOG);
	private static final Set<Player> fogPlayers = new HashSet<>();
	private final ResourcePackManager manager;
	private final PluginSettings settings;
	private BukkitRunnable fogTask;

	private static final String ON_MESSAGE = ChatColor.GREEN + "Fog Enabled";
	private static final String ON_WARNING_MESSAGE = ChatColor.YELLOW + "Fog is already enabled!";
	private static final String OFF_MESSAGE = ChatColor.RED + "Fog Disabled";
	private static final String OFF_WARNING_MESSAGE = ChatColor.YELLOW + "Fog is already disabled!";

	public Fog(ResourcePackManager manager, PluginSettings settings) {
		this.manager = manager;
		this.settings = settings;
		fogBossBar.setVisible(true);
		settingCheck();
	}

	private void settingCheck(){
		if (settings.isFogAutoEnabled()) {
			manager.setEnabled(true);
			startFogTask();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (sender instanceof Player) ? (Player) sender : null;
		switch (label.toLowerCase()) {
		case "fogon":
			if (fogTask == null) {
				applyFog(player);
				startFogTask();
				manager.setEnabled(true);
			} else {
				Bukkit.broadcastMessage(ON_WARNING_MESSAGE);
			}
			break;
		case "fogoff":
			if (fogTask != null) {
				clearFog(player);
				stopFogTask();
				manager.setEnabled(false);
			} else {
				Bukkit.broadcastMessage(OFF_WARNING_MESSAGE);
			}
			break;
		}
		return true;
	}

	private void applyFog(Player admin) {
		fogBossBar.removeAll();
		fogPlayers.clear();
		if (admin != null) {
			fogBossBar.addPlayer(admin);
			fogPlayers.add(admin);
		}
	}

	private void startFogTask() {
		Bukkit.broadcastMessage(ON_MESSAGE);
		fogTask = new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					fogBossBar.addPlayer(player);
					fogPlayers.add(player);
				}
			}
		};
		fogTask.runTaskTimer(JavaPlugin.getPlugin(SkyGridPlugin.class), 0, 20);
	}

	private void stopFogTask() {
		Bukkit.broadcastMessage(OFF_MESSAGE);
		if (fogTask != null) {
			fogTask.cancel();
			fogTask = null;
		}
		fogBossBar.removeAll();
		fogPlayers.clear();
	}

	private void clearFog(Player admin) {
		fogBossBar.removeAll();
		fogPlayers.clear();
		if (admin != null) {
			fogBossBar.addPlayer(admin);
			fogPlayers.add(admin);
		}
	}
}