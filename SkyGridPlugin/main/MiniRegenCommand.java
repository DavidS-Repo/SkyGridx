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
		World world   = player.getWorld();
		int chunkX    = player.getLocation().getChunk().getX();
		int chunkZ    = player.getLocation().getChunk().getZ();

		if (args.length == 0) {
			Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen <add|remove> ...");
			return true;
		}

		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 4) {
				Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen add <intervalSec> <alias> <distribution> [group]");
				return true;
			}
			int interval;
			try {
				interval = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				interval = 600;
				Cc.sendS(player, Cc.RED, "Invalid interval, using 600 s.");
			}

			String alias = args[2];
			String distribution = args[3];
			String group = args.length >= 5 ? args[4] : "";

			miniRegenManager.addMiniRegen(world.getName(), chunkX, chunkZ, alias, distribution, interval, group);

			Cc.sendS(player, Cc.GREEN, "Chunk added with alias '" + alias + "'.");
			return true;
		}

		if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen remove <alias> | /miniregen remove chunk <alias> | /miniregen remove group <group>");
				return true;
			}
			if (args[1].equalsIgnoreCase("group")) {
				if (args.length < 3) {
					Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen remove group <group>");
					return true;
				}
				String groupName = args[2];
				miniRegenManager.removeMiniRegenGroup(groupName);
				Cc.sendS(player, Cc.GREEN, "Removed all mini-regen settings in group '" + groupName + "'.");
				return true;
			}
			if (args[1].equalsIgnoreCase("chunk")) {
				if (args.length < 3) {
					Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen remove chunk <alias>");
					return true;
				}
				String alias = args[2];
				miniRegenManager.removeMiniRegen(alias);
				Cc.sendS(player, Cc.GREEN, "Removed mini-regen chunk with alias '" + alias + "'.");
				return true;
			}
			String alias = args[1];
			miniRegenManager.removeMiniRegen(alias);
			Cc.sendS(player, Cc.GREEN, "Removed mini-regen chunk with alias '" + alias + "'.");
			return true;
		}
		Cc.sendS(player, Cc.YELLOW, "Usage: /miniregen <add|remove> ...");
		return true;
	}
}
