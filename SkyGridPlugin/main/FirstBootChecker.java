package main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FirstBootChecker {
	private SkyGridPlugin plugin;

	public FirstBootChecker(SkyGridPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean checkForFirstBoot() {
		boolean isFirstBoot = false;

		isFirstBoot |= copyFileIfNotPresent("world.yml", "SkygridBlocks");
		isFirstBoot |= copyFileIfNotPresent("world_nether.yml", "SkygridBlocks");
		isFirstBoot |= copyFileIfNotPresent("world_the_end.yml", "SkygridBlocks");
		isFirstBoot |= copyFileIfNotPresent("ores.yml", "OreGenBlock");
		isFirstBoot |= copyFileIfNotPresent("ChestSettings.yml", "SkygridBlocks");
		isFirstBoot |= copyFileIfNotPresent("SpawnerSettings.yml", "SkygridBlocks");
		isFirstBoot |= copyFileIfNotPresent("settings.yml", "");

		return isFirstBoot;
	}

	private boolean copyFileIfNotPresent(String fileName, String destinationFolder) {
		Path destinationPath = Paths.get(plugin.getDataFolder().getPath(), destinationFolder, fileName);
		if (!Files.exists(destinationPath)) {
			try (InputStream inputStream = plugin.getResource(fileName)) {
				if (inputStream != null) {
					Files.createDirectories(destinationPath.getParent());
					Files.copy(inputStream, destinationPath);
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