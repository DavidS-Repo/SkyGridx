package main;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Patch {

    private static final String SUCCESS_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&a&lFiles patched successfully.");
    private static final String INSTRUCTIONS_MESSAGE = ChatColor.translateAlternateColorCodes('&', "&7Please &e&l/reload&7 or &e&l/restart&7 the server for the changes to apply.");
    private static final String FAILED_MESSAGE = ChatColor.RED + "Failed to patch files. Please check server logs for details.";

    public static void patchFiles(CommandSender sender, SkyGridPlugin plugin) {
        boolean filesPatched = copyFilesFromJar("world.txt", "SkygridBlocks", plugin) &&
                copyFilesFromJar("world_nether.txt", "SkygridBlocks", plugin) &&
                copyFilesFromJar("world_the_end.txt", "SkygridBlocks", plugin) &&
                copyFilesFromJar("ores.yml", "OreGenBlock", plugin) &&
                copyFilesFromJar("ChestSettings.yml", "SkygridBlocks", plugin) &&
                copyFilesFromJar("SpawnerSettings.yml", "SkygridBlocks", plugin);
        		copyFilesFromJar("config.yml", "", plugin);
        if (filesPatched) {
            sender.sendMessage(SUCCESS_MESSAGE);
            sender.sendMessage(INSTRUCTIONS_MESSAGE);
        } else {
            sender.sendMessage(FAILED_MESSAGE);
        }
    }

    private static boolean copyFilesFromJar(String fileName, String destinationFolder, SkyGridPlugin plugin) {
        Path destinationPath = Paths.get(plugin.getDataFolder().getPath(), destinationFolder, fileName);
        try (InputStream inputStream = plugin.getResource(fileName)) {
            if (inputStream != null) {
                Files.deleteIfExists(destinationPath);
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