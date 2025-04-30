package main;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiniRegenCommand implements CommandExecutor {
	private final MiniRegenManager miniRegenManager;

	public MiniRegenCommand(MiniRegenManager miniRegenManager) {
		this.miniRegenManager = miniRegenManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Cc.sendS(sender, Cc.RED, "Only players can use this command.");
			return true;
		}
		Player player = (Player) sender;
		World world = player.getWorld();
		int currentChunkX = player.getLocation().getChunk().getX();
		int currentChunkZ = player.getLocation().getChunk().getZ();

		if (args.length == 0) {
			Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen <add|remove> [interval] [alias] [distribution] [group] OR /miniregen remove [alias] OR /miniregen remove group [groupName]");
			return true;
		}

		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 4) {
				Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen add [interval in seconds] [alias] [distribution] [group(optional)]");
				return true;
			}
			int interval = 600;
			try {
				interval = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				Cc.sendS(player, Cc.RED, "Invalid interval. Using default 600 seconds (10 Minutes).");
			}
			String alias = args[2];
			String distribution = args[3];
			String group = "";
			if (args.length >= 5) {
				group = args[4];
			}
			miniRegenManager.addMiniRegen(world.getName(), currentChunkX, currentChunkZ, alias, distribution, interval, group);
			Cc.sendS(player, Cc.GREEN, "This chunk has been added for mini regeneration with alias '" + alias + "', interval " 
					+ interval + " seconds, distribution '" + distribution + "'" + (group.isEmpty() ? "" : " and group '" + group + "'") + ".");
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen remove [alias] OR /miniregen remove group [groupName]");
				return true;
			}
			if (args[1].equalsIgnoreCase("group")) {
				if (args.length < 3) {
					Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen remove group [groupName]");
					return true;
				}
				String groupName = args[2];
				miniRegenManager.removeMiniRegenGroup(groupName);
				Cc.sendS(player, Cc.GREEN, "All mini regeneration settings in group '" + groupName + "' have been removed.");
			} else {
				String alias = args[1];
				miniRegenManager.removeMiniRegen(alias);
				Cc.sendS(player, Cc.GREEN, "The mini regeneration setting with alias '" + alias + "' has been removed.");
			}
		} else {
			Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen <add|remove> [interval] [alias] [distribution] [group] OR /miniregen remove [alias] OR /miniregen remove group [groupName]");
		}
		return true;
	}
}