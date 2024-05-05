package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PreGeneratorCommands implements CommandExecutor {
	private final PreGenerator preGenerator;

	public PreGeneratorCommands(PreGenerator preGenerator) {
		this.preGenerator = preGenerator;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("pregen")) {
			if (sender instanceof ConsoleCommandSender || sender.isOp()) {
				if (args.length == 2) {
					try {
						int chunksPerRun = Integer.parseInt(args[0]);
						int printTime = Integer.parseInt(args[1]);
						preGenerator.setValues(chunksPerRun, printTime);
						preGenerator.enable();
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Invalid numbers provided.");
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Usage: /pregen <chunksPerCycle> <PrintUpdate(DelayinMinutes)>");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
		} else if (label.equalsIgnoreCase("pregenoff")) {
			if (sender instanceof ConsoleCommandSender || sender.isOp()) {
				preGenerator.disable();
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
		}
		return false;
	}
}
