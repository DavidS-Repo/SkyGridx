package main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GrowthControlCommands implements CommandExecutor {

    private final GrowthControl growthControl;

    public GrowthControlCommands(SkyGridPlugin plugin, GrowthControl growthControl) {
        this.growthControl = growthControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gclogson")) {
            growthControl.enableLogging();
            return true;
        } else if (command.getName().equalsIgnoreCase("gclogsoff")) {
            growthControl.disableLogging();
            return true;
        }
        return false;
    }
}