package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SkyGridPlugin extends JavaPlugin implements Listener {

	private boolean pluginEnabled = false;
	private boolean firstBoot = false;

	@Override
	public void onEnable() {
		// Register listeners
		getServer().getPluginManager().registerEvents(this, this);

		// Create an instance of ResourcePackManager
		ResourcePackManager manager = new ResourcePackManager();

		// Register the instance as a listener without immediately activating it
		getServer().getPluginManager().registerEvents(manager, this);

		// Create an instance of Fog and pass the ResourcePackManager instance
		Fog fogCommandExecutor = new Fog(manager);

		// Set the command executor for your fog commands
		getCommand("fogon").setExecutor(fogCommandExecutor);
		getCommand("fogoff").setExecutor(fogCommandExecutor);

		// Register commands
		getCommand("sgon").setExecutor(new CommandEnable(this));
		getCommand("tpr").setExecutor(new TPRCommand());

		// Set the TPRAutoCompleter for the "/tpr" command
		TPRAutoCompleter tprAutoCompleter = new TPRAutoCompleter();
		getCommand("tpr").setTabCompleter(tprAutoCompleter);

		// Ensure plugin folders exist
		createFoldersIfNotExist("SkygridBlocks");
		createFoldersIfNotExist("OreGenBlock");

		// Check for files and set the firstBoot flag accordingly
		firstBoot = checkForFirstBoot();
		if (firstBoot) {
			getLogger().info("Thank you for installing our plugin!");
		}

		// Register GrowthControl as a listener
		GrowthControl growthControl = new GrowthControl(this);
		growthControl.initialize();

		// Set the executor for GrowthControlOn and GrowthControlOff commands
		GrowthControlCommands commands = new GrowthControlCommands(this, growthControl);
		getCommand("gclogson").setExecutor(commands);
		getCommand("gclogsoff").setExecutor(commands);
	}

	private void createFoldersIfNotExist(String folderName) {
		File folder = new File(getDataFolder(), folderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	private boolean checkForFirstBoot() {
		boolean isFirstBoot = false;

		if (copyFileIfNotPresent("world.txt", "SkygridBlocks")) {
			isFirstBoot = true;
		}
		copyFileIfNotPresent("world_nether.txt", "SkygridBlocks");
		copyFileIfNotPresent("world_the_end.txt", "SkygridBlocks");
		copyFileIfNotPresent("ores.yml", "OreGenBlock");
		return isFirstBoot;
	}

	private boolean copyFileIfNotPresent(String fileName, String destinationFolder) {
		Path destinationPath = Paths.get(getDataFolder().getPath(), destinationFolder, fileName);
		if (!Files.exists(destinationPath)) {
			try (InputStream inputStream = getResource(fileName)) {
				if (inputStream != null) {
					Files.copy(inputStream, destinationPath);
					return true;
				} else {
					getLogger().warning("Could not find " + fileName + " in the plugin resources.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean isFirstBoot() {
		return firstBoot;
	}

	private class CommandEnable implements CommandExecutor {
		private final SkyGridPlugin plugin;
		public CommandEnable(SkyGridPlugin plugin) {
			this.plugin = plugin;
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			// Check if the sender is a player and has OP permission or if it's the console
			if (!(sender instanceof Player) && !sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			if (!plugin.pluginEnabled) {
				plugin.pluginEnabled = true;
				handleGeneration(sender);
			} else {
				sender.sendMessage(ChatColor.RED + "Custom Skygrid generation is already enabled.");
			}
			return true;
		}

		private void handleGeneration(CommandSender sender) {
			Generator generator = new Generator(plugin);
			generator.initialize();
		}
	}
}
