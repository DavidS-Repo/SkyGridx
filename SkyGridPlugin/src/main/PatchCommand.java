package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PatchCommand implements CommandExecutor {

    private static final String WARNING_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&e&lWARNING: &cThis command will wipe all default item and entity files. Enter &a&l'/patch confirm'&c to proceed.");
    private SkyGridPlugin plugin;

    public PatchCommand(SkyGridPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("patch")) {
            if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
                    Patch.patchFiles(sender, plugin);
                } else {
                    sender.sendMessage(WARNING_MESSAGE);
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
        }
        return false;
    }
}
