package main;

import org.bukkit.Bukkit;
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

	private static final BossBar fogBossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID, BarFlag.CREATE_FOG);
	private static final Set<Player> fogPlayers = new HashSet<>();
	private final ResourcePackManager manager;
	private BukkitRunnable fogTask;

	private static final String ON_MESSAGE = Cc.logO(Cc.GREEN, "Fog Enabled");
	private static final String ON_WARNING_MESSAGE =  Cc.logO(Cc.YELLOW, "Fog is already enabled!");
	private static final String OFF_MESSAGE =  Cc.logO(Cc.RED, "Fog Disabled");
	private static final String OFF_WARNING_MESSAGE =  Cc.logO(Cc.YELLOW, "Fog is already disabled!");
	private static boolean FogToggle = PluginSettings.isFogAutoEnabled();

	public Fog(ResourcePackManager manager, PluginSettings settings) {
		this.manager = manager;
		fogBossBar.setVisible(true);
		settingCheck();
	}

	private void settingCheck(){
		if (FogToggle) {
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
				Cc.logSB(ON_WARNING_MESSAGE);
			}
			break;
		case "fogoff":
			if (fogTask != null) {
				clearFog(player);
				stopFogTask();
				manager.setEnabled(false);
			} else {
				Cc.logSB(OFF_WARNING_MESSAGE);
			}
			break;
		}
		return true;
	}

	private void applyFog(Player admin) {
		fogBossBar.removeAll();
		fogPlayers.clear();
		if (admin != null && WorldManager.isCustomWorld(admin)) {
			fogBossBar.addPlayer(admin);
			fogPlayers.add(admin);
		}
	}

	private void startFogTask() {
		Cc.logSB(ON_MESSAGE);
		fogTask = new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (WorldManager.isCustomWorld(player)) {
						if (!fogPlayers.contains(player)) {
							fogBossBar.addPlayer(player);
							fogPlayers.add(player);
						}
					} else {
						if (fogPlayers.contains(player)) {
							fogBossBar.removePlayer(player);
							fogPlayers.remove(player);
						}
					}
				}
			}
		};
		fogTask.runTaskTimer(JavaPlugin.getPlugin(SkyGridPlugin.class), 0, 20);
	}

	private void stopFogTask() {
		Cc.logSB(OFF_MESSAGE);
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
		if (admin != null && WorldManager.isCustomWorld(admin)) {
			fogBossBar.addPlayer(admin);
			fogPlayers.add(admin);
		}
	}
}