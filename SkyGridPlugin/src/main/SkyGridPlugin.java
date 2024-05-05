package main;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Filter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SkyGridPlugin extends JavaPlugin implements Listener {

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

		//  Register command and Set the TPRAutoCompleter for the "/tpr" command
		getCommand("tpr").setExecutor(new TPRCommand());
		TPRAutoCompleter tprAutoCompleter = new TPRAutoCompleter();
		getCommand("tpr").setTabCompleter(tprAutoCompleter);

		// Ensure plugin folders exist
		createFoldersIfNotExist("SkygridBlocks");
		createFoldersIfNotExist("OreGenBlock");

		// Check for files and set the firstBoot flag accordingly
		firstBoot = checkForFirstBoot();
		if (firstBoot) {
			getLogger().info("Thank you for installing our plugin!");
			handleGeneration();
		} else {
			// Start the generator if it's not the first boot
			handleGeneration();
		}

		// Register GrowthControl as a listener
		GrowthControl growthControl = new GrowthControl(this);
		growthControl.initialize();

		// Set the executor for GrowthControlOn and GrowthControlOff commands
		GrowthControlCommands commands = new GrowthControlCommands(this, growthControl);
		getCommand("gclogson").setExecutor(commands);
		getCommand("gclogsoff").setExecutor(commands);

		// Register the PreGeneratorCommands as the executor and tab completer for the /pregen command
		PreGenerator preGenerator = new PreGenerator(this);
		PreGeneratorCommands preGeneratorCommands = new PreGeneratorCommands(preGenerator);
		getCommand("pregen").setExecutor(preGeneratorCommands);
		getCommand("pregenoff").setExecutor(preGeneratorCommands);

		// Create a custom filter to suppress specific warnings
		Filter logFilter = new LogFilter();

		// Get the logger for your plugin and set the custom filter
		getLogger().setFilter(logFilter);

		// Register PatchCommand as command executor for /patch
		getCommand("patch").setExecutor(new PatchCommand(this));
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

	private void handleGeneration() {
		Generator generator = new Generator(this);
		generator.initialize();
	}
}