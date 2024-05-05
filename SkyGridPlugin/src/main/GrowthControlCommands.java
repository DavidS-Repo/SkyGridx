package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class GrowthControlCommands implements CommandExecutor {

    private final GrowthControl growthControl;

    public GrowthControlCommands(SkyGridPlugin plugin, GrowthControl growthControl) {
        this.growthControl = growthControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gclogson")) {
            if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                growthControl.enableLogging();
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("gclogsoff")) {
            if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                growthControl.disableLogging();
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
        }
        return false;
    }
}