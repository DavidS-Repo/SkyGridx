package main;

import java.io.*;
import java.nio.file.*;

public class FirstBootChecker {
    private SkyGridPlugin plugin;

    public FirstBootChecker(SkyGridPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean checkForFirstBoot() {
        boolean isFirstBoot = false;

        if (copyFileIfNotPresent("world.txt", "SkygridBlocks")) {
            isFirstBoot = true;
        }
        copyFileIfNotPresent("world_nether.txt", "SkygridBlocks");
        copyFileIfNotPresent("world_the_end.txt", "SkygridBlocks");
        copyFileIfNotPresent("ores.yml", "OreGenBlock");
        copyFileIfNotPresent("settings.yml", "");

        return isFirstBoot;
    }

    private boolean copyFileIfNotPresent(String fileName, String destinationFolder) {
        Path destinationPath = Paths.get(plugin.getDataFolder().getPath(), destinationFolder, fileName);
        if (!Files.exists(destinationPath)) {
            try (InputStream inputStream = plugin.getResource(fileName)) {
                if (inputStream != null) {
                    Files.createDirectories(destinationPath.getParent());
                    Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    return true;
                } else {
                    plugin.getLogger().warning("Could not find " + fileName + " in the plugin resources.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void createFoldersIfNotExist(String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}