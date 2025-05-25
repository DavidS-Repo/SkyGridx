package main;

import org.bukkit.Bukkit;
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
	private MiniRegenManager miniRegenManager;
	private PortalLocationManager portalManager;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new ChestOpenListener(this), this);
		Bukkit.getPluginManager().registerEvents(this, this);

		// Create an instance of ResourcePackManager
		ResourcePackManager manager = new ResourcePackManager();

		// Register the instance as a listener
		getServer().getPluginManager().registerEvents(manager, this);

		// Ensure plugin folders exist
		bootChecker = new FirstBootChecker(this);
		bootChecker.createFoldersIfNotExist("SkygridBlocks");
		bootChecker.createFoldersIfNotExist("OreGenBlock");
		firstBoot = bootChecker.checkForFirstBoot();

		// Initialize PluginSettings
		settings = new PluginSettings(this);

		// Create an instance of Fog and pass the ResourcePackManager instance and settings
		Fog fogCommandExecutor = new Fog(manager, settings);

		// Create a custom filter to suppress specific warnings
		// Get the logger for your plugin and set the custom filter
		Filter logFilter = new LogFilter();
		getLogger().setFilter(logFilter);

		// Set the command executor for your fog commands
		getCommand("fogon").setExecutor(fogCommandExecutor);
		getCommand("fogoff").setExecutor(fogCommandExecutor);

		// Register the "/tpr" command
		TPRCommand tprCommand = new TPRCommand(this);
		getCommand("tpr").setTabCompleter(new TPRAutoCompleter());
		getCommand("tpr").setExecutor(tprCommand);
		getCommand("tpro").setExecutor(tprCommand);
		getCommand("tprn").setExecutor(tprCommand);
		getCommand("tpre").setExecutor(tprCommand);

		// Register GrowthControl as a listener
		EventControl eventControl = new EventControl(this, tprCommand);
		eventControl.initialize();
		
		CustomBedManager bedManager = new CustomBedManager(this);
		getServer().getPluginManager().registerEvents(new BedIsolation(bedManager, tprCommand, this), this);

		// Register PlayerJoin as a listener
		getServer().getPluginManager().registerEvents(new PlayerJoin(settings, tprCommand), this);

		// Set the executor for GrowthControlOn and GrowthControlOff commands
		EventControlCommands commands = new EventControlCommands(this, eventControl);
		getCommand("eclogson").setExecutor(commands);
		getCommand("eclogsoff").setExecutor(commands);

		// Register PatchCommand as command executor for /patch
		getCommand("patch").setExecutor(new PatchCommand(this));

		// generation logic (only trigger ChunkLoader on first boot)
		handleGeneration();

		// Register miniregen as command executor and tab completer for /miniregen
		miniRegenManager = new MiniRegenManager(this, generator);
		miniRegenManager.initialize();
		getCommand("miniregen").setExecutor(new MiniRegenCommand(miniRegenManager));
		getCommand("miniregen").setTabCompleter(new MiniRegenTabCompleter(miniRegenManager));

		// Register portal handling and pass events
		portalManager = new PortalLocationManager(this);
		portalManager.initialize();
		Bukkit.getPluginManager().registerEvents(new EyeThrowListener(this), this);
	}

	@Override
	public void onDisable() {
		if (miniRegenManager != null) {
			miniRegenManager.shutdown();
		}
	}

	public PortalLocationManager getPortalManager() {
		return portalManager;
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

			// Register the RegenCommand with the Generator instance
			getCommand("regen").setExecutor(new RegenerateCommand(this, generator));

			if (firstBoot) {
				new ChunkLoader(this).loadChunksAndRun(() -> {
				});
			}
		}
	}

	public Generator getGenerator() {
		return generator;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (chunksLoading) {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Cc.RED + "Chunks are still loading, please wait a moment and try again." + Cc.RESET);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!firstPlayerConnected && firstBoot) {
			firstPlayerConnected = true;
			new BukkitRunnable() {
				@Override
				public void run() {
					generator.regenerateAllLoadedChunks();
					portalManager.clearAllPortals();
				}
			}.runTaskLater(this, 40);
		}
	}
}