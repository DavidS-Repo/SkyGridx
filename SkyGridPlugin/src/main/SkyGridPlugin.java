package main;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.logging.Filter;

public class SkyGridPlugin extends JavaPlugin implements Listener {

	private boolean firstBoot = false;
	private boolean firstPlayerConnected = false;
	private boolean chunksLoading = false;
	private FirstBootChecker bootChecker;
	private PluginSettings settings;
	private Generator generator;

	@Override
	public void onEnable() {
		// Register this class as a listener
		getServer().getPluginManager().registerEvents(this, this);

		// Create an instance of ResourcePackManager
		ResourcePackManager manager = new ResourcePackManager();

		// Register the instance as a listener
		getServer().getPluginManager().registerEvents(manager, this);

		// Initialize PluginSettings
		settings = new PluginSettings(this);

		// Register the PreGenerator
		PreGenerator preGenerator = new PreGenerator(this);

		// Create an instance of Fog and pass the ResourcePackManager instance and settings
		Fog fogCommandExecutor = new Fog(manager, settings);

		// Register GrowthControl as a listener
		GrowthControl growthControl = new GrowthControl(this);
		growthControl.initialize();

		// Create a custom filter to suppress specific warnings
		Filter logFilter = new LogFilter();

		// Get the logger for your plugin and set the custom filter
		getLogger().setFilter(logFilter);

		// Set the command executor for your fog commands
		getCommand("fogon").setExecutor(fogCommandExecutor);
		getCommand("fogoff").setExecutor(fogCommandExecutor);

		// Register the "/tpr" command
		TPRCommand tprCommand = new TPRCommand(settings);
		getCommand("tpr").setTabCompleter(new TPRAutoCompleter());
		getCommand("tpr").setExecutor(tprCommand);
		getCommand("tpro").setExecutor(tprCommand);
		getCommand("tprn").setExecutor(tprCommand);
		getCommand("tpre").setExecutor(tprCommand);

		// Set the executor for GrowthControlOn and GrowthControlOff commands
		GrowthControlCommands commands = new GrowthControlCommands(this, growthControl);
		getCommand("gclogson").setExecutor(commands);
		getCommand("gclogsoff").setExecutor(commands);

		// PreGeneratorCommands as the executor and tab completer for the /pregen command
		PreGeneratorCommands preGeneratorCommands = new PreGeneratorCommands(preGenerator);
		getCommand("pregen").setExecutor(preGeneratorCommands);
		getCommand("pregenoff").setExecutor(preGeneratorCommands);

		// Register PatchCommand as command executor for /patch
		getCommand("patch").setExecutor(new PatchCommand(this));

		// Register the RegenCommand with the Generator instance
		getCommand("regen").setExecutor(new RegenerateCommand(this, generator));

		// Ensure plugin folders exist
		bootChecker = new FirstBootChecker(this);
		bootChecker.createFoldersIfNotExist("SkygridBlocks");
		bootChecker.createFoldersIfNotExist("OreGenBlock");

		// Check for first boot
		firstBoot = bootChecker.checkForFirstBoot();
		handleGeneration();
	}

	public boolean isFirstBoot() {
		return firstBoot;
	}

	public void setChunksLoading(boolean chunksLoading) {
		this.chunksLoading = chunksLoading;
	}

	public boolean areChunksLoading() {
		return chunksLoading;
	}

	private void handleGeneration() {
		if (generator == null) {
			generator = new Generator(this);
			generator.initialize();

			if (firstBoot) {
				new ChunkLoader(this).loadChunksAndRun(() -> {
				});
			}
		}
	}

	public Generator getGenerator() {
		return generator;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (chunksLoading) {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Chunks are still loading, please wait a moment and try again.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!firstPlayerConnected && firstBoot) {
			firstPlayerConnected = true;

			// Schedule a sync task to re-process all loaded chunks when the first player joins
			new BukkitRunnable() {
				@Override
				public void run() {
					generator.processAllLoadedChunks();
				}
			}.runTask(this);
		}
	}
}