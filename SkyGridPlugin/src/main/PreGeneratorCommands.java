package main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PreGeneratorCommands implements CommandExecutor {
	private final PreGenerator preGenerator;

	public PreGeneratorCommands(PreGenerator preGenerator) {
		this.preGenerator = preGenerator;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("pregen")) {
			if (args.length == 2) {
				try {
					int chunksPerRun = Integer.parseInt(args[0]);
					int printTime = Integer.parseInt(args[1]);
					preGenerator.setValues(chunksPerRun, printTime);
					preGenerator.enable();
					return true;
				} catch (NumberFormatException e) {
					Bukkit.broadcastMessage("Invalid numbers provided.");
					return false;
				}
			}
			else {
				Bukkit.broadcastMessage("Usage: /pregen <chunksPerCycle> <PrintUpdate(DelayinMinutes)>");
				return false;
			}
		} 
		else if (label.equalsIgnoreCase("pregenoff")) {
			preGenerator.disable();
			return true;
		}
		return false;
	}
}
