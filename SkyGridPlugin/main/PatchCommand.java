package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PatchCommand implements CommandExecutor {

    private static final String WARNING_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&e&lWARNING: &cThis command will wipe all default item and entity files. Enter &a&l'/patch confirm'&c to proceed.");
    private final SkyGridPlugin plugin;

    public PatchCommand(SkyGridPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("patch")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
                Patch.patchFiles(sender, plugin);
                return true;
            } else {
                sender.sendMessage(WARNING_MESSAGE);
                return true;
            }
        }
        return false;
    }
}