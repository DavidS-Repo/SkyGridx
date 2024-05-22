package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PreGeneratorCommands implements CommandExecutor {
    private final PreGenerator preGenerator;
    private static final String WARNING_MESSAGE = ChatColor.RED + ("Invalid numbers provided.");
	private static final String USAGE_MESSAGE = ChatColor.GREEN + ("Usage: /pregen <chunksPerCycle> <PrintUpdate(DelayinMinutes)>");

    public PreGeneratorCommands(PreGenerator preGenerator) {
        this.preGenerator = preGenerator;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("pregen")) {
            if (args.length == 2) {
                try {
                    int chunksPerRun = Integer.parseInt(args[0]);
                    int printTime = Integer.parseInt(args[1]);
                    preGenerator.setValues(chunksPerRun, printTime);
                    preGenerator.enable();
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(WARNING_MESSAGE);
                    return true;
                }
            } else {
                sender.sendMessage(USAGE_MESSAGE);
                return true;
            }
        } else if (label.equalsIgnoreCase("pregenoff")) {
            preGenerator.disable();
            return true;
        }
        return false;
    }
}