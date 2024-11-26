package main;

import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Patch {

    private static final String SUccESS_MESSAGE =  "Files patched successfully.";
    private static final String INSTRUCTIONS_MESSAGE = "Please /reload or /restart the server for the changes to apply.";
    private static final String FAILED_MESSAGE =  "Failed to patch files. Please check server logs for details.";

    public static void patchFiles(CommandSender sender, SkyGridPlugin plugin) {
        boolean filesPatched = copyFilesFromJar("world.txt", "SkygridBlocks", plugin) &&
                copyFilesFromJar("world_nether.txt", "SkygridBlocks", plugin) &&
                copyFilesFromJar("world_the_end.txt", "SkygridBlocks", plugin) &&
                copyFilesFromJar("ores.yml", "OreGenBlock", plugin) &&
                copyFilesFromJar("ChestSettings.yml", "SkygridBlocks", plugin) &&
                copyFilesFromJar("SpawnerSettings.yml", "SkygridBlocks", plugin);
        		copyFilesFromJar("config.yml", "", plugin);
        if (filesPatched) {
            Cc.sendS(sender, Cc.GREEN, SUccESS_MESSAGE);
            Cc.sendS(sender, Cc.YELLOW, INSTRUCTIONS_MESSAGE);
        } else {
        	Cc.sendS(sender, Cc.RED, FAILED_MESSAGE);
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