package main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class EventControlCommands implements CommandExecutor {

	private final EventControl eventControl;

	public EventControlCommands(Plugin plugin, EventControl eventControl) {
		this.eventControl = eventControl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("eclogson")) {
			eventControl.enableLogging();
			return true;
		} else if (command.getName().equalsIgnoreCase("eclogsoff")) {
			eventControl.disableLogging();
			return true;
		}
		return false;
	}
}
