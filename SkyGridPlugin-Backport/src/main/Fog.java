package main;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Fog implements CommandExecutor {

	private static final BossBar fogBossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID, BarFlag.CREATE_FOG);
	private static final Set<UUID> fogPlayers = ConcurrentHashMap.newKeySet();
	private final JavaPlugin plugin;
	private final ResourcePackManager manager;
	private ScheduledTask fogTask;

	private static final String ON_MESSAGE = Cc.logO(Cc.GREEN, "Fog Enabled");
	private static final String ON_WARNING_MESSAGE =  Cc.logO(Cc.YELLOW, "Fog is already enabled!");
	private static final String OFF_MESSAGE =  Cc.logO(Cc.RED, "Fog Disabled");
	private static final String OFF_WARNING_MESSAGE =  Cc.logO(Cc.YELLOW, "Fog is already disabled!");
	private static boolean FogToggle = PluginSettings.isFogAutoEnabled();

	public Fog(JavaPlugin plugin, ResourcePackManager manager, PluginSettings settings) {
		this.plugin = plugin;
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
			SkyGridScheduler.runGlobal(plugin, () -> {
				if (fogTask == null) {
					applyFog(player);
					startFogTask();
					manager.setEnabled(true);
				} else {
					Cc.logSB(ON_WARNING_MESSAGE);
				}
			});
			break;
		case "fogoff":
			SkyGridScheduler.runGlobal(plugin, () -> {
				if (fogTask != null) {
					clearFog(player);
					stopFogTask();
					manager.setEnabled(false);
				} else {
					Cc.logSB(OFF_WARNING_MESSAGE);
				}
			});
			break;
		}
		return true;
	}

	private void applyFog(Player admin) {
		fogBossBar.removeAll();
		fogPlayers.clear();
		if (admin != null && WorldManager.isCustomWorld(admin)) {
			fogBossBar.addPlayer(admin);
			fogPlayers.add(admin.getUniqueId());
		}
	}

	private void startFogTask() {
		Cc.logSB(ON_MESSAGE);
		fogTask = SkyGridScheduler.runGlobalTimer(plugin, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				UUID playerId = player.getUniqueId();
				if (WorldManager.isCustomWorld(player)) {
					if (fogPlayers.add(playerId)) {
						fogBossBar.addPlayer(player);
					}
				} else {
					if (fogPlayers.remove(playerId)) {
						fogBossBar.removePlayer(player);
					}
				}
			}
		}, 1L, 20L);
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
			fogPlayers.add(admin.getUniqueId());
		}
	}
}
