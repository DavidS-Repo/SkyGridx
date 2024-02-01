package main;

import io.papermc.lib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class PreGenerator {
	private static final String DATA_FILE_NAME = "pregenerator_data.txt";
	private boolean enabled = false;
	private Timer timer;
	private final JavaPlugin plugin;
	private final World world;
	private int chunksPerRun, printTime;
	private int x = 0, z = 0, dx = 0, dz = -1, chunkPerMin = 0;
	private long totalWorldChunks = 14062361500009L, totalChunksProcessed = 0L;

	public PreGenerator(JavaPlugin plugin) {
		this.plugin = plugin;
		this.world = plugin.getServer().getWorlds().get(0);
	}

	public void enable() {
		if (enabled) {
			Bukkit.broadcastMessage("Pre-Generator is already enabled.");
			return;
		}
		enabled = true;
		Bukkit.broadcastMessage("Pre-generation has been enabled.");
		loadProcessedChunks();
		startGeneration();
		startPrintInfoTimer();
	}

	public void disable() {
		if (!enabled) {
			Bukkit.broadcastMessage("Pre-Generator is already disabled.");
			return;
		}
		saveProcessedChunks();
		stopPrintInfoTimer();
		enabled = false;
		Bukkit.broadcastMessage("Pre-generation disabled.");
	}

	private void startPrintInfoTimer() {
		if (timer == null) {
			timer = new Timer(true);
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					printInfo();
				}
			}, 0, (printTime * 60000)); // printTime * 60,000 milliseconds (60 seconds)
		}
	}

	private void stopPrintInfoTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	public void setValues(int chunksPerRun, int printTime) {
		this.chunksPerRun = chunksPerRun;
		this.printTime = printTime;
	}

	private void startGeneration() {
		generateChunkBatch();
	}

	private void generateChunkBatch() {
		if (!enabled) {
			return; // Stop generating if pre-generation is disabled
		}

		List<CompletableFuture<Void>> chunkLoadingTasks = new ArrayList<>();

		// Process chunksPerRun number of chunks in each iteration
		for (int i = 0; i < chunksPerRun; i++) {
			if (!enabled) {
				// If pre-generation is disabled, break out of the loop
				break;
			}

			CompletableFuture<Void> chunkLoadedTask = PaperLib.getChunkAtAsync(world, x, z, true)
					.thenAcceptAsync(chunk -> unloadChunkAsync(chunk, world))
					.thenRun(() -> {
						totalChunksProcessed++;
						chunkPerMin++;
						nextChunk();
					});

			chunkLoadingTasks.add(chunkLoadedTask);

			// Check if pre-generation is complete
			if (totalChunksProcessed >= totalWorldChunks) {
				disable(); // Disable pre-generation when completed
				break; // Break out of the loop to stop further processing
			}
		}

		// Wait for all chunk loading tasks to complete
		CompletableFuture<Void> allChunksLoaded = CompletableFuture.allOf(
				chunkLoadingTasks.toArray(new CompletableFuture[0])
				);

		// Continue with unloading and other tasks after all chunks are loaded
		allChunksLoaded.thenRun(() -> {
			if (enabled) {
				// Only save processed chunks and print info if pre-generation is still enabled
				saveProcessedChunks();
				// Start the next batch recursively
				generateChunkBatch();
			}
		});
	}

	// Unload chunk synchronously on the main thread
	private void unloadChunkAsync(Chunk chunk, World world) {
		Bukkit.getScheduler().runTask(plugin, () -> unloadChunk(chunk, world));
	}

	private void unloadChunk(Chunk chunk, World world) {
		// Ensure the chunk is loaded before unloading
		if (world.isChunkLoaded(chunk.getX(), chunk.getZ())) {
			world.unloadChunk(chunk.getX(), chunk.getZ(), true);
		}
	}

	private void printInfo() {
		long remainingChunks = totalWorldChunks - totalChunksProcessed;
		double remainingPercentage = 100 - ((double) remainingChunks / totalWorldChunks * 100);
		Bukkit.broadcastMessage(String.format("Completed %.10f%%, Chunks Remaining: %d, Chunks/Min: %d", remainingPercentage, remainingChunks, chunkPerMin));
		chunkPerMin = 0;
	}

	private void saveProcessedChunks() {
		File dataFile = new File(plugin.getDataFolder(), DATA_FILE_NAME);

		try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile, false))) {
			writer.println(x + "_" + z + "_" + dx + "_" + dz);
			writer.println(totalChunksProcessed);
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.broadcastMessage("Total Processed Chunks " + totalChunksProcessed + ".");
		}
	}

	private void loadProcessedChunks() {
		File dataFile = new File(plugin.getDataFolder(), DATA_FILE_NAME);
		if (dataFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
				String lastLine = reader.readLine();

				while (lastLine != null) {
					loadChunkCoordinatesAndDirection(lastLine, reader);
					lastLine = reader.readLine();
				}
				Bukkit.broadcastMessage("Loaded " + totalChunksProcessed + " processed chunks from: " + dataFile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadChunkCoordinatesAndDirection(String lastLine, BufferedReader reader) throws IOException {
		String[] coords = lastLine.split("_");
		x = Integer.parseInt(coords[0]);
		z = Integer.parseInt(coords[1]);
		dx = Integer.parseInt(coords[2]);
		dz = Integer.parseInt(coords[3]);

		String totalChunksLine = reader.readLine();
		if (totalChunksLine != null) {
			totalChunksProcessed = Long.parseLong(totalChunksLine);
		}
	}

	private void nextChunk() {
	    if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
	        int temp = dx;
	        dx = -dz;
	        dz = temp;
	    }
	    if (Math.abs(x) <= Math.abs(z)) {
	        x += (z >= 0) ? 1 : -1;
	    } else {
	        z += (x >= 0) ? -1 : 1;
	    }
	}
}
