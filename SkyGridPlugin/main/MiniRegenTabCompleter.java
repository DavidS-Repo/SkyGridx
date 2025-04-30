package main;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MiniRegenTabCompleter implements TabCompleter {
	private final MiniRegenManager miniRegenManager;

	public MiniRegenTabCompleter(MiniRegenManager miniRegenManager) {
		this.miniRegenManager = miniRegenManager;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String aliasCmd, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			String partial = args[0].toLowerCase();
			if ("add".startsWith(partial)) {
				completions.add("add");
			}
			if ("remove".startsWith(partial)) {
				completions.add("remove");
			}
		} 
		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("add")) {
				completions.add("[interval in seconds]");
			} 
			else if (args[0].equalsIgnoreCase("remove")) {
				String partial = args[1].toLowerCase();
				if ("group".startsWith(partial)) {
					completions.add("group");
				}
				if ("chunk".startsWith(partial)) {
					completions.add("chunk");
				}
			}
		} 
		else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("add")) {
				completions.add("[alias]");
			} 
			else if (args[0].equalsIgnoreCase("remove")) {
				if (args[1].equalsIgnoreCase("group")) {
					for (String group : miniRegenManager.getExistingGroups()) {
						if (group.toLowerCase().startsWith(args[2].toLowerCase())) {
							completions.add(group);
						}
					}
				} 
				else if (args[1].equalsIgnoreCase("chunk")) {
					for (String aliasStr : miniRegenManager.getMiniRegenSettings().keySet()) {
						if (aliasStr.toLowerCase().startsWith(args[2].toLowerCase())) {
							completions.add(aliasStr);
						}
					}
				}
			}
		} 
		else if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
			for (String dist : miniRegenManager.getMiniRegenDistributions().keySet()) {
				if (dist.toLowerCase().startsWith(args[3].toLowerCase())) {
					completions.add(dist);
				}
			}
		} 
		else if (args.length == 5 && args[0].equalsIgnoreCase("add")) {
			completions.add("[group]");
			for (String group : miniRegenManager.getExistingGroups()) {
				if (group.toLowerCase().startsWith(args[4].toLowerCase())) {
					completions.add(group);
				}
			}
		}
		return completions;
	}
}