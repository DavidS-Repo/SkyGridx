package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RegenerateCommand implements CommandExecutor {

	private static final String WARNING_MESSAGE = ChatColor.translateAlternateColorCodes
			('&', "&e&lWARNING: &cThis will regenerate all currently loaded chunks and may cause lag for a moment. Enter &a&l'/regen confirm'&c to proceed.");
	private static final String SUCCESS_MESSAGE = ChatColor.GREEN + ("All currently loaded chunks have begun being regenerated.");

	private final Generator generator;

	public RegenerateCommand(SkyGridPlugin plugin, Generator generator) {
		this.generator = generator;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("regen")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
				generator.regenerateAllLoadedChunks();
				sender.sendMessage(SUCCESS_MESSAGE);
				return true;
			} else {
				sender.sendMessage(WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}
}