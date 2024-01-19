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
			if (args.length == 3) {
				try {
					int chunksPerRun = Integer.parseInt(args[0]);
					int taskDelay = Integer.parseInt(args[1]);
					int printTime = Integer.parseInt(args[2]);
					printTime = printTime * 1200;
					preGenerator.setValues(chunksPerRun, taskDelay, printTime);
					preGenerator.enable();
					return true;
				} catch (NumberFormatException e) {
					Bukkit.broadcastMessage("Invalid numbers provided for chunksPerCycle and CycleDelayinTicks.");
					return false;
				}
			}
			else {
				Bukkit.broadcastMessage("Usage: /pregen <chunksPerCycle> <Cycle(DelayinTicks)> <PrintUpdate(DelayinMinutes)>");
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
