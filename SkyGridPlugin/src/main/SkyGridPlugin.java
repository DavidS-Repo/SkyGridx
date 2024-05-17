package main;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Filter;

public class SkyGridPlugin extends JavaPlugin implements Listener {

	private boolean firstBoot = false;
	private FirstBootChecker bootChecker;
	private Generator generator;

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
		bootChecker = new FirstBootChecker(this);
		bootChecker.createFoldersIfNotExist("SkygridBlocks");
		bootChecker.createFoldersIfNotExist("OreGenBlock");

		// Check for first boot
		firstBoot = bootChecker.checkForFirstBoot();
		if (firstBoot) {
			getLogger().info("Thank you for installing our plugin!");
			handleGeneration();
		} else {
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

		// Register the RegenCommand with the Generator instance
		getCommand("sgregen").setExecutor(new RegenerateCommand(this, generator));
	}

	public boolean isFirstBoot() {
		return firstBoot;
	}

	private void handleGeneration() {
		// Use existing Generator instance if available, otherwise create a new one
		if (generator == null) {
			generator = new Generator(this);
			generator.initialize();
		}
	}

	// Method to access the Generator instance if needed
	public Generator getGenerator() {
		return generator;
	}
}