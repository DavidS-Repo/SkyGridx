package main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TPRAutoCompleter implements TabCompleter {

    private List<String> options = new ArrayList<>();

    public TPRAutoCompleter() {
    	options.add("overworld");
        options.add("nether");
        options.add("end");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(partial)) {
                    suggestions.add(option);
                }
            }
            return suggestions;
        }
        return null;
    }
}
