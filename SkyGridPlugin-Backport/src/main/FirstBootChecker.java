package main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FirstBootChecker {
	private static final List<ManagedFile> MANAGED_FILES = List.of(
			new ManagedFile("world.yml", "SkygridBlocks"),
			new ManagedFile("world_nether.yml", "SkygridBlocks"),
			new ManagedFile("world_the_end.yml", "SkygridBlocks"),
			new ManagedFile("ores.yml", "OreGenBlock"),
			new ManagedFile("ChestSettings.yml", "SkygridBlocks"),
			new ManagedFile("SpawnerSettings.yml", "SkygridBlocks"),
			new ManagedFile("settings.yml", "")
			);
	private static final ManagedFile VERSION_CATALOG = new ManagedFile("versioned-defaults.yml", "");

	private final SkyGridPlugin plugin;

	public FirstBootChecker(SkyGridPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean checkForFirstBoot() {
		Path dataFolder = plugin.getDataFolder().toPath();
		boolean freshInstall = isFreshInstall(dataFolder);
		boolean copiedAnyManagedFile = false;

		for (ManagedFile managedFile : MANAGED_FILES) {
			copiedAnyManagedFile |= copyFileIfNotPresent(managedFile.fileName(), managedFile.destinationFolder());
		}
		// Keep a readable copy of the catalog in the plugin folder for server owners.
		// Its presence alone does not make an otherwise empty folder an existing install.
		copyFileIfNotPresent(VERSION_CATALOG.fileName(), VERSION_CATALOG.destinationFolder());

		if (freshInstall && allManagedFilesPresent(dataFolder)) {
			VersionedDefaultsApplier.applyForRunningServer(plugin);
		} else if (!freshInstall) {
			plugin.getLogger().info("Existing SkyGrid configuration detected; versioned defaults were not merged.");
		}

		return copiedAnyManagedFile;
	}

	static boolean isFreshInstall(Path dataFolder) {
		for (ManagedFile managedFile : MANAGED_FILES) {
			if (Files.exists(resolve(dataFolder, managedFile))) return false;
		}
		return true;
	}

	private static boolean allManagedFilesPresent(Path dataFolder) {
		for (ManagedFile managedFile : MANAGED_FILES) {
			if (!Files.isRegularFile(resolve(dataFolder, managedFile))) return false;
		}
		return true;
	}

	private static Path resolve(Path dataFolder, ManagedFile managedFile) {
		return managedFile.destinationFolder().isEmpty()
				? dataFolder.resolve(managedFile.fileName())
				: dataFolder.resolve(managedFile.destinationFolder()).resolve(managedFile.fileName());
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

	private record ManagedFile(String fileName, String destinationFolder) {}
}
