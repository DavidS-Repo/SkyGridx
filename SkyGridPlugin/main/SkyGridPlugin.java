package main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import java.util.logging.Filter;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkyGridPlugin extends JavaPlugin implements Listener {
	private boolean firstBoot = false;
	private final AtomicBoolean firstPlayerConnected = new AtomicBoolean(false);
	private final AtomicBoolean chunksLoading = new AtomicBoolean(false);
	private final AtomicBoolean multiverseWarningLogged = new AtomicBoolean(false);
	private FirstBootChecker bootChecker;
	private PluginSettings settings;
	private Generator generator;
	private MiniRegenManager miniRegenManager;
	private PortalLocationManager portalManager;
	private ChestRegionData chestRegionData;

	@Override
	public void onLoad() {
		WorldGeneratorConfigPatcher.applyGeneratorSettings(this);
	}

	@Override
	public void onEnable() {
		// Initialize ChestRegionData early
		chestRegionData = ChestRegionData.getInstance(this);

		Bukkit.getPluginManager().registerEvents(new ChestOpenListener(this), this);
		getServer().getPluginManager().registerEvents(new DragonSpawnListener(this), this);
		Bukkit.getPluginManager().registerEvents(this, this);
		// Create an instance of ResourcePackManager
		ResourcePackManager manager = new ResourcePackManager(this);
		// Register the instance as a listener
		getServer().getPluginManager().registerEvents(manager, this);
		// Ensure plugin folders exist
		bootChecker = new FirstBootChecker(this);
		bootChecker.createFoldersIfNotExist("SkygridBlocks");
		bootChecker.createFoldersIfNotExist("OreGenBlock");
		firstBoot = bootChecker.checkForFirstBoot();
		// Initialize PluginSettings
		settings = new PluginSettings(this);
		warnIfMultiverseDetected();
		// Create an instance of Fog and pass the ResourcePackManager instance and settings
		Fog fogCommandExecutor = new Fog(this, manager, settings);
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

		CustomAnchorManager anchorManager = new CustomAnchorManager(this);
		getServer().getPluginManager().registerEvents(new AnchorIsolation(anchorManager, tprCommand, this),this);
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
		// Properly shutdown chest region data system
		if (chestRegionData != null) {
			chestRegionData.shutdown();
		}
	}

	public PortalLocationManager getPortalManager() {
		return portalManager;
	}

	public boolean isFirstBoot() {
		return firstBoot;
	}

	public void setChunksLoading(boolean chunksLoading) {
		this.chunksLoading.set(chunksLoading);
	}

	public boolean areChunksLoading() {
		return chunksLoading.get();
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		if (chunksLoading.get()) {
			event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			event.kickMessage(Component.text("Chunks are still loading, please wait a moment and try again."));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (firstBoot && firstPlayerConnected.compareAndSet(false, true)) {
			SkyGridScheduler.runGlobalLater(this, () -> {
				generator.regenerateAllLoadedChunks();
				portalManager.clearAllPortals();
			}, 40L);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		if ("Multiverse-Core".equalsIgnoreCase(event.getPlugin().getName())) {
			warnIfMultiverseDetected();
		}
	}

	private void warnIfMultiverseDetected() {
		if (settings == null || !PluginSettings.isMultiverseWarningsEnabled()) {
			return;
		}
		if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") == null) {
			return;
		}
		if (!multiverseWarningLogged.compareAndSet(false, true)) {
			return;
		}
		getLogger().warning("Multiverse-Core detected. For Paper custom SkyGrid worlds, import or create skygridx_world, skygridx_world_nether, and skygridx_world_the_end in Multiverse with generator 'SkyGrid' before generating new chunks.");
		getLogger().warning("If Multiverse, CMI, or another spawn plugin should control death respawns, set compatibility.respawnIsolation=false in SkyGrid settings.yml.");
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		if (WorldManager.isConfiguredWorldName(worldName)) {
			return new VoidWorldGenerator();
		}
		return null;
	}
}
