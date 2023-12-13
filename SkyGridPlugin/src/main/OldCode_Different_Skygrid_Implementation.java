package main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OldCode_Different_Skygrid_Implementation extends JavaPlugin implements Listener {

	private Set<Chunk> processedChunks;
	private List<Chunk> loadedChunks;
	private Chunk centerChunk;
	private boolean pluginEnabled = false; // Plugin is off by default
	private boolean isFirstJoin = true; // True for the first join of each player
	private File processedChunksFile;
	private Player lastPlayer; // Store the last player to handle reconnection
	private BukkitTask generationTask; // Task for chunk generation
	private int chunkLoadDelay = 1;
	private int completionDelay = 1;
	private Map<String, Set<Chunk>> processedChunksByWorld;

	@Override
	public void onEnable() {
		// Register the listener (SkyGridPlugin)
	    getServer().getPluginManager().registerEvents(this, this);
	    getServer().getPluginManager().registerEvents(new ChunkClearer(), this);

		// Initialize the processedChunks set and loadedChunks list
		processedChunks = new HashSet<>();
		loadedChunks = new ArrayList<>();

		// Initialize the processedChunksByWorld map
		processedChunksByWorld = new HashMap<>();

		// Register the commands /sgon and /sgoff
		getCommand("sgon").setExecutor(new CommandEnable(this));
		getCommand("sgoff").setExecutor(new CommandDisable(this));
        getCommand("tpr").setExecutor(new TPRCommand());

		// Check and create the plugin folder if it doesn't exist
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		// Create the folders "SkygridBlocks" and "OreGenBlock" if they don't exist
		File skygridBlocksFolder = new File(getDataFolder(), "SkygridBlocks");
		File oreGenBlockFolder = new File(getDataFolder(), "OreGenBlock");

		if (!skygridBlocksFolder.exists()) {
			skygridBlocksFolder.mkdirs();
		}

		if (!oreGenBlockFolder.exists()) {
			oreGenBlockFolder.mkdirs();
		}

		// Check for and copy the required files to the respective folders
		copyFileIfNotPresent("world.txt", "SkygridBlocks");
		copyFileIfNotPresent("world_nether.txt", "SkygridBlocks");
		copyFileIfNotPresent("world_the_end.txt", "SkygridBlocks");
		copyFileIfNotPresent("ores.txt", "OreGenBlock");

		// Load processed chunks from file if it exists
		processedChunksFile = new File(getDataFolder(), "processed_chunks.txt");
		if (processedChunksFile.exists()) {
			loadProcessedChunksAsync();
		} else {
			try {
				processedChunksFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Initialize the processedChunksByWorld map and load processed chunks synchronously
	    processedChunksByWorld = new HashMap<>();
	    ChunkClearer.loadClearedChunks();
	}

	@Override
	public void onDisable() {
		saveProcessedChunks();

		// Save cleared chunks when the plugin is disabled
		ChunkClearer.saveClearedChunks();

		getLogger().info("SkyGridPlugin has been disabled.");
	}

	// Helper method to copy a file to the specified folder if not present
	private void copyFileIfNotPresent(String fileName, String destinationFolder) {
		Path destinationPath = Paths.get(getDataFolder().getPath(), destinationFolder, fileName);
		if (!Files.exists(destinationPath)) {
			try (InputStream inputStream = getResource(fileName)) {
				if (inputStream != null) {
					Files.copy(inputStream, destinationPath);
				} else {
					getLogger().warning("Could not find " + fileName + " in the plugin resources.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Call this method from your plugin's onEnable() or any other appropriate place to load the data asynchronously.
	private void loadProcessedChunksAsync() {
		Bukkit.getScheduler().runTaskAsynchronously(OldCode_Different_Skygrid_Implementation.this, () -> loadProcessedChunks());
	}

	private void updateOuterChunks(Chunk chunk) {
		World world = chunk.getWorld();
		String worldName = world.getName();
		Set<Chunk> outerChunks = processedChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>());

		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		boolean isOuterChunk = false;

		// Check if the chunk is an outer chunk (not surrounded by other processed chunks)
		for (int xOffset = -1; xOffset <= 1; xOffset++) {
			for (int zOffset = -1; zOffset <= 1; zOffset++) {
				if ((xOffset == 0 && zOffset == 0) || processedChunks.contains(world.getChunkAt(chunkX + xOffset, chunkZ + zOffset))) {
					continue;
				}

				isOuterChunk = true;
				break;
			}
		}

		if (isOuterChunk) {
			outerChunks.add(chunk);
		} else {
			outerChunks.remove(chunk);
		}
	}

	private void loadProcessedChunks() {
		try (BufferedReader reader = new BufferedReader(new FileReader(processedChunksFile))) {
			String line;
			String currentWorld = null;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith("world{") && line.endsWith("}")) {
					// Start of a new world section
					if (currentWorld != null) {
						// Add the processed chunks of the previous world to the map
						processedChunks.addAll(processedChunksByWorld.getOrDefault(currentWorld, new HashSet<>()));
					}

					// Extract the world name and set it as the current world
					currentWorld = line.substring("world{".length(), line.length() - 1).trim();
					getLogger().info("Current World: " + currentWorld);
				} else {
					// Processed chunk entry (format: -x,z)
					String[] chunkCoords = line.split(",");
					if (chunkCoords.length == 2) {
						int chunkX = Integer.parseInt(chunkCoords[0].trim());
						int chunkZ = Integer.parseInt(chunkCoords[1].trim());
						Chunk chunk = Bukkit.getWorld(currentWorld).getChunkAt(chunkX, chunkZ);
						processedChunks.add(chunk); // Add the processed chunk to the set
					}
				}
			}

			// Add the last processed chunks to the map (if any)
			if (currentWorld != null) {
				processedChunks.addAll(processedChunksByWorld.getOrDefault(currentWorld, new HashSet<>()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void saveProcessedChunks() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(processedChunksFile));
			for (Map.Entry<String, Set<Chunk>> entry : processedChunksByWorld.entrySet()) {
				String worldName = entry.getKey();
				writer.write("world{" + worldName + "}\n");
				for (Chunk chunk : entry.getValue()) {
					writer.write(chunk.getX() + "," + chunk.getZ() + "\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		lastPlayer = player; // Store the last player to handle reconnection

		// Check if it's the first join and the plugin is enabled
		if (isFirstJoin && pluginEnabled) {
			isFirstJoin = false;

			// Set the center chunk as the player's chunk
			centerChunk = player.getLocation().getChunk();

			// Initialize the list of loaded chunks
			loadedChunks = new ArrayList<>(Arrays.asList(centerChunk.getWorld().getLoadedChunks()));

			// Remove already processed chunks from the loadedChunks list
			loadedChunks.removeAll(processedChunks);

			// Process all chunks at once
			processNextChunk(player);
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();

		// Add the loaded chunk to the list if it hasn't been processed already
		if (!processedChunks.contains(chunk) && !loadedChunks.contains(chunk)) {
			loadedChunks.add(chunk);
		}
	}


	private void processNextChunk(Player player) {
		if (!pluginEnabled || loadedChunks.isEmpty()) {
			// Either the plugin is disabled or there are no more chunks to process
			return;
		}

		Chunk currentChunk = player.getLocation().getChunk();
		List<Chunk> neighborChunks = getNeighborChunks(currentChunk);

		// Shuffle the neighbor chunks to give an equal chance to move in any direction
		Collections.shuffle(neighborChunks);

		// Find the closest neighboring chunk to the center among the shuffled neighbors
		final Chunk[] closestChunk = {null};
		final double[] closestDistance = {Double.MAX_VALUE};

		for (Chunk chunk : neighborChunks) {
			double distance = getDistanceSquared(centerChunk, chunk);
			if (distance < closestDistance[0]) {
				closestChunk[0] = chunk;
				closestDistance[0] = distance;
			}
		}

		// Teleport the player to the current chunk immediately
		moveToChunk(player, closestChunk[0]);

		// Process the chunk into a sky grid after 20 ticks (allowing the chunk to load)
		BukkitRunnable generationTask = new BukkitRunnable() {
			@Override
			public void run() {
				generateSkyGridInChunk(closestChunk[0], player);

				// Update processedChunks set
				processedChunks.add(closestChunk[0]);

				// Remove the processed chunk from the loadedChunks list
				loadedChunks.remove(closestChunk[0]);

				// Process the next chunk after 10 ticks (to ensure the chunk is fully processed)
				BukkitRunnable nextChunkTask = new BukkitRunnable() {
					@Override
					public void run() {
						processNextChunk(player);
					}
				};
				nextChunkTask.runTaskLater(OldCode_Different_Skygrid_Implementation.this, completionDelay);
			}
		};
		generationTask.runTaskLater(this, chunkLoadDelay);
	}


	private List<Chunk> getNeighborChunks(Chunk chunk) {
		List<Chunk> neighborChunks = new ArrayList<>();

		int x = chunk.getX();
		int z = chunk.getZ();

		// Add the neighboring chunks (up, down, left, right)
		neighborChunks.add(chunk.getWorld().getChunkAt(x, z + 1));
		neighborChunks.add(chunk.getWorld().getChunkAt(x, z - 1));
		neighborChunks.add(chunk.getWorld().getChunkAt(x - 1, z));
		neighborChunks.add(chunk.getWorld().getChunkAt(x + 1, z));

		// Add diagonal chunks (up-left, up-right, down-left, down-right)
		neighborChunks.add(chunk.getWorld().getChunkAt(x - 1, z + 1));
		neighborChunks.add(chunk.getWorld().getChunkAt(x + 1, z + 1));
		neighborChunks.add(chunk.getWorld().getChunkAt(x - 1, z - 1));
		neighborChunks.add(chunk.getWorld().getChunkAt(x + 1, z - 1));

		// Filter out chunks that are already processed or not loaded
		neighborChunks.removeIf(c -> processedChunks.contains(c) || !loadedChunks.contains(c));

		return neighborChunks;
	}

	private void moveToChunk(Player player, Chunk chunk) {
		World overworld = Bukkit.getWorld("world"); // Assuming the overworld is named "world"
		int chunkX = chunk.getX() << 4;
		int chunkZ = chunk.getZ() << 4;

		// Set the player's new location to the chunk's center at Y level 0 and looking down
		Location newLocation = new Location(overworld, chunkX + 8, 0, chunkZ + 8, 0, 90);
		player.teleport(newLocation);
	}

	private void generateSkyGridInChunk(Chunk chunk, Player player) {
		// Skip generation for non-overworld chunks or if the plugin is disabled
		if (!pluginEnabled || chunk.getWorld().getEnvironment() != World.Environment.NORMAL) {
			return;
		}

		if (isChunkProcessed(chunk)) {
			return; // Skip already processed chunks
		}

		int xStart = chunk.getX() << 4;
		int zStart = chunk.getZ() << 4;

		int xEnd = xStart + 15;
		int zEnd = zStart + 15;
		int minY = -64;
		int maxY = 256;

		// Batched block modification
		List<Block> blocksToModify = new ArrayList<>();

		for (int x = xStart; x <= xEnd; x++) {
			for (int z = zStart; z <= zEnd; z++) {
				for (int y = minY; y <= maxY; y++) {
					Block block = chunk.getBlock(x & 0xF, y, z & 0xF);

					// Your block removal logic here
					if (shouldRemoveBlock(x, y, z, chunk)) {
						blocksToModify.add(block);
					}
				}
			}
		}

		// Suppress block updates for adjacent blocks before modifying blocks
		suppressAdjacentBlockUpdates(chunk, blocksToModify);

		// Modify blocks on the main thread
		Bukkit.getScheduler().runTask(this, () -> {
			for (Block block : blocksToModify) {
				block.setType(Material.AIR, false);
				block.setBlockData(Bukkit.createBlockData(Material.AIR), false);
			}
		});

		// Update processedChunks set
		processedChunks.add(chunk);

		// Update processedChunksByWorld map
		String worldName = chunk.getWorld().getName();
		processedChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>()).add(chunk);
		updateOuterChunks(chunk);


		// Remove the processed chunk from the loadedChunks list
		loadedChunks.remove(chunk);
	}

	// Helper method to suppress block updates for adjacent blocks
	private void suppressAdjacentBlockUpdates(Chunk chunk, List<Block> blocks) {
		int[] offsets = {-1, 0, 1};
		for (Block block : blocks) {
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			for (int xOffset : offsets) {
				for (int yOffset : offsets) {
					for (int zOffset : offsets) {
						int adjX = x + xOffset;
						int adjY = y + yOffset;
						int adjZ = z + zOffset;
						// Only suppress block updates for blocks within the same chunk
						if (adjY >= 0 && adjY < chunk.getWorld().getMaxHeight() &&
								adjX >= 0 && adjX < 16 && adjZ >= 0 && adjZ < 16) {
							chunk.getBlock(adjX, adjY, adjZ).setType(chunk.getBlock(adjX, adjY, adjZ).getType(), false);
							chunk.getBlock(adjX, adjY, adjZ).setBlockData(chunk.getBlock(adjX, adjY, adjZ).getBlockData(), false);
						}
					}
				}
			}
		}
	}


	private boolean isChunkProcessed(Chunk chunk) {
		return processedChunks.contains(chunk);
	}

	private boolean shouldRemoveBlock(int x, int y, int z, Chunk chunk) {
		int xStart = chunk.getX() << 4;
		int zStart = chunk.getZ() << 4;
		int xEnd = xStart + 15;
		int zEnd = zStart + 15;
		int minY = -64;
		int maxY = 255;

		// Vertical Removal
		int xOffset = x - xStart;
		int zOffset = z - zStart;
		boolean isInRange = y >= minY && y <= maxY;
		boolean isOnBoundary = (y % 4) != 0 && x >= xStart && x <= xEnd && z >= zStart && z <= zEnd;

		if ((xOffset == 0 && zOffset >= 0 && zOffset <= 15) ||
				(xOffset == 1 && zOffset >= 0 && zOffset <= 1) ||
				(xOffset == 1 && zOffset >= 3 && zOffset <= 5) ||
				(xOffset == 1 && zOffset >= 7 && zOffset <= 9) ||
				(xOffset == 1 && zOffset >= 11 && zOffset <= 13) ||
				(xOffset == 1 && zOffset >= 15) ||
				(xOffset >= 2 && xOffset <= 4 && zOffset >= 0 && zOffset <= 15) ||
				(xOffset == 5 && zOffset >= 0 && zOffset <= 1) ||
				(xOffset == 5 && zOffset >= 3 && zOffset <= 5) ||
				(xOffset == 5 && zOffset >= 7 && zOffset <= 9) ||
				(xOffset == 5 && zOffset >= 11 && zOffset <= 13) ||
				(xOffset == 5 && zOffset >= 15) ||
				(xOffset >= 6 && xOffset <= 8 && zOffset >= 0 && zOffset <= 15) ||
				(xOffset == 9 && zOffset >= 0 && zOffset <= 1) ||
				(xOffset == 9 && zOffset >= 3 && zOffset <= 5) ||
				(xOffset == 9 && zOffset >= 7 && zOffset <= 9) ||
				(xOffset == 9 && zOffset >= 11 && zOffset <= 13) ||
				(xOffset == 9 && zOffset >= 15) ||
				(xOffset >= 10 && xOffset <= 12 && zOffset >= 0 && zOffset <= 15) ||
				(xOffset == 13 && zOffset >= 0 && zOffset <= 1) ||
				(xOffset == 13 && zOffset >= 3 && zOffset <= 5) ||
				(xOffset == 13 && zOffset >= 7 && zOffset <= 9) ||
				(xOffset == 13 && zOffset >= 11 && zOffset <= 13) ||
				(xOffset == 13 && zOffset >= 15) ||
				(xOffset >= 14 && xOffset <= 15 && zOffset >= 0 && zOffset <= 15)) {
			return true;
		}

		// Horizontal Removal
		if (isInRange && isOnBoundary) {
			return true;
		}

		return false;
	}

	private double getDistanceSquared(Chunk chunk1, Chunk chunk2) {
		int dx = (chunk1.getX() - chunk2.getX()) * 16;
		int dz = (chunk1.getZ() - chunk2.getZ()) * 16;
		return dx * dx + dz * dz;
	}

	// New command executor for /sgon
	private class CommandEnable implements CommandExecutor {
		private final OldCode_Different_Skygrid_Implementation plugin;

		public CommandEnable(OldCode_Different_Skygrid_Implementation plugin) {
			this.plugin = plugin;
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length > 0 && args[0].equalsIgnoreCase("gen")) {
				// Call the method to handle generation using the Generator class
				handleGeneration(sender);
			} else {
				// Enable the main SkyGridPlugin if not generating
				if (!plugin.pluginEnabled) {
					plugin.pluginEnabled = true;
					plugin.isFirstJoin = true; // Reset first join flag to true

					// Set the center chunk for the last player to handle reconnection
					if (plugin.lastPlayer != null) {
						plugin.centerChunk = plugin.lastPlayer.getLocation().getChunk();
					}

					// Initialize the list of loaded chunks
					plugin.loadedChunks = new ArrayList<>(Arrays.asList(plugin.centerChunk.getWorld().getLoadedChunks()));

					// Remove already processed chunks from the loadedChunks list
					plugin.loadedChunks.removeAll(plugin.processedChunks);

					// Start processing chunks
					plugin.processNextChunk(plugin.lastPlayer);

					sender.sendMessage(ChatColor.GREEN + "SkyGridPlugin has been enabled.");
				} else {
					sender.sendMessage(ChatColor.RED + "SkyGridPlugin is already enabled.");
				}
			}
			return true;
		}

		private void handleGeneration(CommandSender sender) {
			// Call the Generator class here for generation logic
			boolean clearChunksBeforeGeneration = true; // Set to true or false based on your requirement
			Generator generator = new Generator(plugin, clearChunksBeforeGeneration);
			sender.sendMessage(ChatColor.GREEN + "Custom Skygrid generation started.");
			generator.initialize();
		}
	}

	// New command executor for /sgoff
	private class CommandDisable implements CommandExecutor {
		private final OldCode_Different_Skygrid_Implementation plugin;

		public CommandDisable(OldCode_Different_Skygrid_Implementation plugin) {
			this.plugin = plugin;
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (plugin.pluginEnabled) {
				plugin.pluginEnabled = false;

				// Cancel the generation task if it is running
				if (plugin.generationTask != null) {
					plugin.generationTask.cancel();
					plugin.generationTask = null; // Reset the task to null
				}

				// Save the processed chunks data before disabling the plugin
				plugin.saveProcessedChunks();

				// Clear the list of loaded chunks
				plugin.loadedChunks.clear();

				sender.sendMessage(ChatColor.RED + "SkyGridPlugin has been disabled.");
			} else {
				sender.sendMessage(ChatColor.RED + "SkyGridPlugin is already disabled.");
			}
			return true;
		}
	}
}