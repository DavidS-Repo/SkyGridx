package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SkyGridPlugin extends JavaPlugin implements Listener {

	private boolean pluginEnabled = false;

	@Override
	public void onEnable() {
		// Register listeners
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new ChunkClearer(), this);

		// Start the auto-save task
		startAutoSaveTask();

		// Register commands
		getCommand("sgon").setExecutor(new CommandEnable(this));
		getCommand("tpr").setExecutor(new TPRCommand());

		// Set the TPRAutoCompleter for the "/tpr" command
		TPRAutoCompleter tprAutoCompleter = new TPRAutoCompleter();
		getCommand("tpr").setTabCompleter(tprAutoCompleter);

		// Ensure plugin folders exist
		createFoldersIfNotExist("SkygridBlocks");
		createFoldersIfNotExist("OreGenBlock");

		// Copy required files if not present
		copyFileIfNotPresent("world.txt", "SkygridBlocks");
		copyFileIfNotPresent("world_nether.txt", "SkygridBlocks");
		copyFileIfNotPresent("world_the_end.txt", "SkygridBlocks");
		copyFileIfNotPresent("ores.txt", "OreGenBlock");
	}

	@Override
	public void onDisable() {
		// Save cleared chunks when the plugin is disabled
		ChunkClearer.saveClearedChunks();
		getLogger().info("SkyGridPlugin has been disabled.");
	}

	private void createFoldersIfNotExist(String folderName) {
		File folder = new File(getDataFolder(), folderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	private void copyFileIfNotPresent(String fileName, String destinationFolder) {
		Path destinationPath = Paths.get(getDataFolder().getPath(), destinationFolder, fileName);
		if (!Files.exists(destinationPath)) {
			try (InputStream inputStream = getResource(fileName)) {
				if (inputStream != null) {
					Files.copy(inputStream, destinationPath);
				} else {
					getLogger().warning("Could not find " + fileName + " in the plugin resources.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class CommandEnable implements CommandExecutor {
		private final SkyGridPlugin plugin;

		public CommandEnable(SkyGridPlugin plugin) {
			this.plugin = plugin;
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!plugin.pluginEnabled) {
				plugin.pluginEnabled = true;
				handleGeneration(sender);
			} else {
				sender.sendMessage(ChatColor.RED + "Custom Skygrid generation is already enabled.");
			}
			return true;
		}

		private void handleGeneration(CommandSender sender) {
			boolean clearChunksBeforeGeneration = true; // Set to true or false based on your requirement
			Generator generator = new Generator(plugin, clearChunksBeforeGeneration);
			sender.sendMessage(ChatColor.GREEN + "Custom Skygrid generation started.");
			generator.initialize();
		}
	}

	public void startAutoSaveTask() {
		// Schedule a task to save cleared chunks every 15 seconds (20 ticks per second)
		int saveInterval = 5 * 20; // 5 seconds
		getServer().getScheduler().runTaskTimer(this, () -> {
			// Call the method to save cleared chunks
			ChunkClearer.saveClearedChunks();
		}, saveInterval, saveInterval);
	}
}