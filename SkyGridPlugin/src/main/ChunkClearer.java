package main;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkClearer implements Listener {
	private static Set<String> clearedChunks = ConcurrentHashMap.newKeySet(); // Use a concurrent set for thread safety

	// Method to save the cleared chunks data to .txt files for each dimension
	public static synchronized void saveClearedChunks() {
		File clearedChunksFolder = new File(JavaPlugin.getPlugin(SkyGridPlugin.class).getDataFolder(), "cleared_chunks");
		if (!clearedChunksFolder.exists()) {
			clearedChunksFolder.mkdirs();
		}
		for (World.Environment dimension : World.Environment.values()) {
			File dimensionFile = new File(clearedChunksFolder, dimension.name() + ".txt");
			Set<String> clearedChunksForDimension = new HashSet<>();

			// Add only the chunks relevant to this dimension
			for (String chunkKey : clearedChunks) {
				String[] parts = chunkKey.split(",");
				if (parts.length == 4) {
					String worldName = parts[0];
					int x = Integer.parseInt(parts[2]);
					int z = Integer.parseInt(parts[3]);
					World world = Bukkit.getWorld(worldName);
					if (world != null && world.getEnvironment() == dimension && isChunkLoaded(world, x, z)) {
						String chunkCoord = x + "," + z;
						clearedChunksForDimension.add(chunkCoord);
					}
				}
			}

			// Load existing cleared chunks from the file (if any)
			Set<String> existingClearedChunks = readClearedChunksFromTxt(dimension);

			// Add the new chunks to the existing cleared chunks data
			existingClearedChunks.addAll(clearedChunksForDimension);

			// Save the updated cleared chunks for this dimension
			if (!existingClearedChunks.isEmpty()) {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(dimensionFile))) {
					String joinedChunks = String.join("/", existingClearedChunks);
					writer.write(joinedChunks);
					writer.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Clear the existing clearedChunks set after saving the data
		clearedChunks.clear();
	}

	// Helper method to check if a chunk is loaded in any world
	private static boolean isChunkLoaded(World world, int x, int z) {
		return world.isChunkLoaded(x, z);
	}


	// Method to load the cleared chunks data from .txt files for each dimension
	static void loadClearedChunks() {
		File clearedChunksFolder = new File(JavaPlugin.getPlugin(SkyGridPlugin.class).getDataFolder(), "cleared_chunks");
		if (!clearedChunksFolder.exists()) {
			return;
		}

		clearedChunks.clear(); // Clear the existing set to reload the data correctly

		for (World.Environment dimension : World.Environment.values()) {
			File dimensionFile = new File(clearedChunksFolder, dimension.name() + ".txt");
			if (dimensionFile.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(dimensionFile))) {
					String line = reader.readLine();
					if (line != null) {
						String[] chunks = line.split("/");
						for (String chunkCoord : chunks) {
							String chunkKey = dimension.name() + "," + chunkCoord;
							clearedChunks.add(chunkKey);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Helper method to clear all blocks in a chunk and kill entities in the chunk
	public static void clearChunk(Chunk chunk) {
		String chunkKey = getChunkKey(chunk);

		// Check if the chunk is already cleared before attempting to clear it again
		if (clearedChunks.contains(chunkKey)) {
			return;
		}

		// Add the cleared chunk to the set
		clearedChunks.add(chunkKey);

		int minY;
		int maxY;

		// Get the current world's dimension type
		World.Environment dimension = chunk.getWorld().getEnvironment();

		// Set height coordinates based on the dimension
		switch (dimension) {
		case NETHER:
			minY = 0;
			maxY = 129;
			break;
		case THE_END:
			minY = 0;
			maxY = 128;
			break;
		default:
			minY = -64;
			maxY = 64;
			break;
		}

		// Clear blocks in the chunk
		Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(SkyGridPlugin.class), () -> {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int y = minY; y < maxY; y++) {
						Block block = chunk.getBlock(x, y, z);
						block.setType(Material.AIR, false);
					}
				}
			}
		}, 1L);
		clearFallingWaterAndLava(chunk);

		// Kill all entities except players in the chunk
		for (Entity entity : chunk.getEntities()) {
			if (!(entity instanceof Player)) {
				entity.remove();
			}
		}
	}


	// Method to clear falling water and lava in a specific chunk
	public static void clearFallingWaterAndLava(Chunk chunk) {
		World world = chunk.getWorld();
		for (int x = chunk.getX() * 16; x < (chunk.getX() * 16) + 16; x++) {
			for (int z = chunk.getZ() * 16; z < (chunk.getZ() * 16) + 16; z++) {
				for (int y = 0; y < world.getMaxHeight(); y++) {
					Block block = world.getBlockAt(x, y, z);
					if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
						Levelled levelledBlock = (Levelled) block.getBlockData();
						if (levelledBlock.getLevel() != 0) {
							// The block is falling water or lava, set it to air
							block.setType(Material.AIR, false);
						}
					}
				}
			}
		}
	}

	private static Set<String> readClearedChunksFromTxt(World.Environment dimension) {
		Set<String> clearedChunksForDimension = new HashSet<>();
		File clearedChunksFolder = new File(JavaPlugin.getPlugin(SkyGridPlugin.class).getDataFolder(), "cleared_chunks");
		File dimensionFile = new File(clearedChunksFolder, dimension.name() + ".txt");

		if (dimensionFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(dimensionFile))) {
				String line = reader.readLine();
				if (line != null) {
					String[] chunks = line.split("/");
					for (String chunkCoord : chunks) {
						clearedChunksForDimension.add(chunkCoord);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return clearedChunksForDimension;
	}


	// Event handler for ChunkLoadEvent to clear blocks in newly generated chunks
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		String chunkKey = getChunkKey(chunk);

		// Check if the chunk has not been cleared before
		if (!clearedChunks.contains(chunkKey)) {
			World.Environment dimension = chunk.getWorld().getEnvironment();
			Set<String> clearedChunksForDimension = readClearedChunksFromTxt(dimension);

			// Check if the chunk is not present in the cleared chunks data for this dimension
			if (!clearedChunksForDimension.contains(chunk.getX() + "," + chunk.getZ())) {
				// Introduce a 5-tick delay before clearing the chunk
				Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(SkyGridPlugin.class), () -> {
					clearChunk(chunk);
					clearedChunks.add(chunkKey);
				}, 1L);
			}
		}
	}

	// Helper method to get a unique key for a chunk based on its coordinates
	private static String getChunkKey(Chunk chunk) {
		return chunk.getWorld().getName() + "," + chunk.getWorld().getEnvironment().name() + "," + chunk.getX() + "," + chunk.getZ();
	}
}
