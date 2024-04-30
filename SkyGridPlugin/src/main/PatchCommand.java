package main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PatchCommand implements CommandExecutor {

	private static final String WARNING_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&e&lWARNING: &cThis command will wipe all default item and entity files. Enter &a&l'/patch confirm'&c to proceed.");
	private static final String SUCCESS_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&a&lFiles patched successfully.");
	private static final String INSTRUCTIONS_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&7Please &e&l/reload&7 or &e&l/restart&7 the server for the changes to apply.");
	private static final String FAILED_MESSAGE = ChatColor.RED + "Failed to patch files. Please check server logs for details.";

	private SkyGridPlugin plugin;

	public PatchCommand(SkyGridPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("patch")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
				patchFiles(sender);
			} else {
				sender.sendMessage(WARNING_MESSAGE);
			}
			return true;
		}
		return false;
	}

	private void patchFiles(CommandSender sender) {
		// Replace existing files with updated ones
		boolean filesPatched = copyFilesFromJar("world.txt", "SkygridBlocks") &&
				copyFilesFromJar("world_nether.txt", "SkygridBlocks") &&
				copyFilesFromJar("world_the_end.txt", "SkygridBlocks") &&
				copyFilesFromJar("ores.yml", "OreGenBlock") &&
				copyFilesFromJar("ChestSettings.yml", "SkygridBlocks") &&
				copyFilesFromJar("SpawnerSettings.yml", "SkygridBlocks");

		if (filesPatched) {
			sender.sendMessage(SUCCESS_MESSAGE);
			sender.sendMessage(INSTRUCTIONS_MESSAGE);
		} else {
			sender.sendMessage(FAILED_MESSAGE);
		}
	}

	private boolean copyFilesFromJar(String fileName, String destinationFolder) {
		Path destinationPath = Paths.get(plugin.getDataFolder().getPath(), destinationFolder, fileName);
		try (InputStream inputStream = plugin.getResource(fileName)) {
			if (inputStream != null) {
				// Delete existing file if present
				Files.deleteIfExists(destinationPath);

				// Copy the new file
				Files.copy(inputStream, destinationPath);
				return true;
			} else {
				plugin.getLogger().warning("Could not find " + fileName + " in the plugin resources.");
				return false;
			}
		} catch (IOException e) {
			plugin.getLogger().warning("Error while copying " + fileName + ": " + e.getMessage());
			return false;
		}
	}
}