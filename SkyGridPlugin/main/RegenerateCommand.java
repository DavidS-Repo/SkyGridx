package main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RegenerateCommand implements CommandExecutor {

	private static final String WARNING_MESSAGE = "WARNING: This will regenerate all currently loaded chunks and may cause lag for a moment. Enter '/regen confirm' to proceed.";
	private static final String SUccESS_MESSAGE = "All currently loaded chunks have begun being regenerated.";

	private final Generator generator;

	public RegenerateCommand(SkyGridPlugin plugin, Generator generator) {
		this.generator = generator;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("regen")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
				generator.regenerateAllLoadedChunks();
				Cc.sendS(sender, Cc.GREEN, SUccESS_MESSAGE);
				return true;
			} else {
				Cc.sendS(sender, Cc.YELLOW, WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}
}