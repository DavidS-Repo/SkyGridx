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
	private BukkitRunnable fogTask;

	public Fog(ResourcePackManager manager) {
		this.manager = manager;
		fogBossBar.setVisible(true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player) && !sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "Only OPs can use this command in the console.");
			return true;
		}
		Player player = (sender instanceof Player) ? (Player) sender : null;

		if (player != null && !player.isOp()) {
			player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			return true;
		}
		switch (label.toLowerCase()) {
		case "fogon":
			applyFog(player);
			startFogTask();
			sender.sendMessage(ChatColor.GREEN + "Fog enabled for Overworld and End.");
			manager.setEnabled(true);
			break;
		case "fogoff":
			clearFog(player);
			stopFogTask();
			sender.sendMessage(ChatColor.RED + "Fog disabled for Overworld and End.");
			manager.setEnabled(false);
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
