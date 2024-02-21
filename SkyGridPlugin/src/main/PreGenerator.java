// there is currently a lot of duplicate code, I tried different ways to 
// simplify it but due to the way the end, nether and Overworld generate at 
// different rates it always caused issues where chunks where skipped
// because the end generates at about 2-3 times the rate
// this is a more simple way to ensure everything just works, 
// just gave all of them their own code for anything that would have lead to issues


package main;

import io.papermc.lib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class PreGenerator {
	private boolean enabled = false;
	private Timer timer;
	private final JavaPlugin plugin;
	private int chunksPerRun, printTime;
	private int chunksPerSec;

	//overworld
	private static final String o = "overwrold_pregenerator_data.txt";
	private final World overworld;
	private int o_x = 0, o_z = 0, o_dx = 0, o_dz = -1, o_chunkPerMin = 0, o_chunksPerSec = 0;
	private long o_totalChunksProcessed = 0L, o_totalWorldChunks = 14062361500009L;

	//nether
	private static final String n = "nether_pregenerator_data.txt";
	private final World nether;
	private int n_x = 0, n_z = 0, n_dx = 0, n_dz = -1, n_chunkPerMin = 0, n_chunksPerSec = 0;
	private long n_totalChunksProcessed = 0L, n_totalWorldChunks = 14062361500009L;

	//end
	private static final String e = "end_pregenerator_data.txt";
	private final World end;
	private int e_x = 0, e_z = 0, e_dx = 0, e_dz = -1, e_chunkPerMin = 0, e_chunksPerSec = 0;
	private long e_totalChunksProcessed = 0L, e_totalWorldChunks = 14062361500009L;


	public PreGenerator(JavaPlugin plugin) {
		this.plugin = plugin;
		this.overworld = plugin.getServer().getWorlds().get(0);
		this.nether = plugin.getServer().getWorlds().get(1);
		this.end = plugin.getServer().getWorlds().get(2);
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
	
	// start generation
	private void startGeneration() {o_startGeneration();n_startGeneration();e_startGeneration();}
	private void o_startGeneration() {o_generateChunkBatch();}
	private void n_startGeneration() {n_generateChunkBatch();}
	private void e_startGeneration() {e_generateChunkBatch();}

	private void o_generateChunkBatch() {
		if (!enabled) {
			return; // Stop generating if pre-generation is disabled
		}

		CompletableFuture<Void> o_allChunksLoaded = CompletableFuture.completedFuture(null);

		// Process chunksPerRun number of chunks in each iteration
		for (int i = 0; i < chunksPerRun; i++) {
			if (!enabled) {
				// If pre-generation is disabled, break out of the loop
				break;
			}

			o_allChunksLoaded = o_allChunksLoaded.thenComposeAsync(ignored ->
			PaperLib.getChunkAtAsync(overworld, o_x, o_z, true)
			.thenAcceptAsync(chunk -> unloadChunkAsync(chunk, overworld))
					).thenComposeAsync(ignored -> CompletableFuture.runAsync(() -> {
						o_totalChunksProcessed++;
						o_chunkPerMin++;

						if (o_x == o_z || (o_x < 0 && o_x == -o_z) || (o_x > 0 && o_x == 1 - o_z)) {
							int o_temp = o_dx;
							o_dx = -o_dz;
							o_dz = o_temp;
						}
						o_x += o_dx;
						o_z += o_dz;

					}));

			// Check if pre-generation is complete
			if (o_totalChunksProcessed >= o_totalWorldChunks) {
				disable(); // Disable pre-generation when completed
				break; // Break out of the loop to stop further processing
			}
		}

		// Continue with unloading and other tasks after all chunks are loaded
		o_allChunksLoaded.thenRun(() -> {
			if (enabled) {
				// Only save processed chunks and print info if pre-generation is still enabled
				o_saveProcessedChunks();
				// Start the next batch recursively
				o_generateChunkBatch();
			}
		});
	}

	private void n_generateChunkBatch() {
		if (!enabled) {
			return;
		}
		CompletableFuture<Void> n_allChunksLoaded = CompletableFuture.completedFuture(null);
		for (int i = 0; i < chunksPerRun; i++) {
			if (!enabled) {
				break;
			}
			n_allChunksLoaded = n_allChunksLoaded.thenComposeAsync(ignored ->
			PaperLib.getChunkAtAsync(nether, n_x, n_z, true)
			.thenAcceptAsync(chunk -> unloadChunkAsync(chunk, nether))
					).thenComposeAsync(ignored -> CompletableFuture.runAsync(() -> {
						n_totalChunksProcessed++;
						n_chunkPerMin++;

						if (n_x == n_z || (n_x < 0 && n_x == -n_z) || (n_x > 0 && n_x == 1 - n_z)) {
							int n_temp = n_dx;
							n_dx = -n_dz;
							n_dz = n_temp;
						}
						n_x += n_dx;
						n_z += n_dz;
					}));
			if (n_totalChunksProcessed >= n_totalWorldChunks) {
				disable();
				break;
			}
		}
		n_allChunksLoaded.thenRun(() -> {
			if (enabled) {
				n_saveProcessedChunks();
				n_generateChunkBatch();
			}
		});
	}

	private void e_generateChunkBatch() {
		if (!enabled) {
			return;
		}
		CompletableFuture<Void> e_allChunksLoaded = CompletableFuture.completedFuture(null);
		for (int i = 0; i < chunksPerRun; i++) {
			if (!enabled) {
				break;
			}
			e_allChunksLoaded = e_allChunksLoaded.thenComposeAsync(ignored ->
			PaperLib.getChunkAtAsync(end, e_x, e_z, true)
			.thenAcceptAsync(chunk -> unloadChunkAsync(chunk, end))
					).thenComposeAsync(ignored -> CompletableFuture.runAsync(() -> {
						e_totalChunksProcessed++;
						e_chunkPerMin++;

						if (e_x == e_z || (e_x < 0 && e_x == -e_z) || (e_x > 0 && e_x == 1 - e_z)) {
							int e_temp = e_dx;
							e_dx = -e_dz;
							e_dz = e_temp;
						}
						e_x += e_dx;
						e_z += e_dz;
					}));
			if (e_totalChunksProcessed >= e_totalWorldChunks) {
				disable();
				break;
			}
		}
		e_allChunksLoaded.thenRun(() -> {
			if (enabled) {
				e_saveProcessedChunks();
				e_generateChunkBatch();
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
		//overworld
		o_chunksPerSec = ((o_chunkPerMin)/ (60 * printTime));
		long o_remainingChunks = o_totalWorldChunks - o_totalChunksProcessed;
		double o_remainingPercentage = 100 - ((double) o_remainingChunks / o_totalWorldChunks * 100);
		Bukkit.broadcastMessage(String.format("Overworld - Completed %.10f%%, Chunks Remaining: %d, Chunks/Sec: %d", 
				o_remainingPercentage, o_remainingChunks, o_chunksPerSec));

		//nether
		n_chunksPerSec = ((n_chunkPerMin)/ (60 * printTime));
		long n_remainingChunks = n_totalWorldChunks - n_totalChunksProcessed;
		double n_remainingPercentage = 100 - ((double) n_remainingChunks / n_totalWorldChunks * 100);
		Bukkit.broadcastMessage(String.format("   Nether - Completed %.10f%%, Chunks Remaining: %d, Chunks/Sec: %d", 
				n_remainingPercentage, n_remainingChunks, n_chunksPerSec));

		// end
		e_chunksPerSec = ((e_chunkPerMin)/ (60 * printTime));
		long e_remainingChunks = e_totalWorldChunks - e_totalChunksProcessed;
		double e_remainingPercentage = 100 - ((double) e_remainingChunks / e_totalWorldChunks * 100);
		Bukkit.broadcastMessage(String.format("      End - Completed %.10f%%, Chunks Remaining: %d, Chunks/Sec: %d", 
				e_remainingPercentage, e_remainingChunks, e_chunksPerSec));

		//total
		Bukkit.broadcastMessage("------------------------------------------------------------------------------------");
		chunksPerSec = o_chunksPerSec + n_chunksPerSec + e_chunksPerSec;
		Bukkit.broadcastMessage(String.format("                                                                 Total Chunks/Sec: %d", chunksPerSec));

		//reset
		o_chunksPerSec = 0;
		o_chunkPerMin = 0;
		n_chunksPerSec = 0;
		n_chunkPerMin = 0;
		e_chunksPerSec = 0;
		e_chunkPerMin = 0;

	}
	
	private void saveProcessedChunks() {
		o_saveProcessedChunks();
		n_saveProcessedChunks();
		e_saveProcessedChunks();
	}

	private void o_saveProcessedChunks() {
		File dataFile = new File(plugin.getDataFolder(), o);

		try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile, false))) {
			writer.println(o_x + "_" + o_z + "_" + o_dx + "_" + o_dz);
			writer.println(o_totalChunksProcessed);
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.broadcastMessage("Total Processed Overworld Chunks " + o_totalChunksProcessed + ".");
		}
	}

	private void n_saveProcessedChunks() {
		File dataFile = new File(plugin.getDataFolder(), n);

		try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile, false))) {
			writer.println(n_x + "_" + n_z + "_" + n_dx + "_" + n_dz);
			writer.println(n_totalChunksProcessed);
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.broadcastMessage("Total Processed Nether Chunks " + n_totalChunksProcessed + ".");
		}
	}

	private void e_saveProcessedChunks() {
		File dataFile = new File(plugin.getDataFolder(), e);

		try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile, false))) {
			writer.println(e_x + "_" + e_z + "_" + e_dx + "_" + e_dz);
			writer.println(e_totalChunksProcessed);
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.broadcastMessage("Total Processed End Chunks " + e_totalChunksProcessed + ".");
		}
	}

	private void loadProcessedChunks() {
		File o_dataFile = new File(plugin.getDataFolder(), o);
		File n_dataFile = new File(plugin.getDataFolder(), n);
		File e_dataFile = new File(plugin.getDataFolder(), e);

		if (o_dataFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(o_dataFile))) {
				String lastLine = reader.readLine();

				while (lastLine != null) {
					String[] coords = lastLine.split("_");
					o_x = Integer.parseInt(coords[0]);
					o_z = Integer.parseInt(coords[1]);
					o_dx = Integer.parseInt(coords[2]);
					o_dz = Integer.parseInt(coords[3]);

					String totalChunksLine = reader.readLine();

					if (totalChunksLine != null) {
						o_totalChunksProcessed = Long.parseLong(totalChunksLine);
					}
					lastLine = reader.readLine();
				}
				Bukkit.broadcastMessage("Loaded " + o_totalChunksProcessed + " processed chunks from: " + o_dataFile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (n_dataFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(n_dataFile))) {
				String lastLine = reader.readLine();

				while (lastLine != null) {
					String[] coords = lastLine.split("_");
					n_x = Integer.parseInt(coords[0]);
					n_z = Integer.parseInt(coords[1]);
					n_dx = Integer.parseInt(coords[2]);
					n_dz = Integer.parseInt(coords[3]);

					String totalChunksLine = reader.readLine();

					if (totalChunksLine != null) {
						n_totalChunksProcessed = Long.parseLong(totalChunksLine);
					}
					lastLine = reader.readLine();
				}
				Bukkit.broadcastMessage("Loaded " + n_totalChunksProcessed + " processed chunks from: " + n_dataFile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (e_dataFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(e_dataFile))) {
				String lastLine = reader.readLine();

				while (lastLine != null) {
					String[] coords = lastLine.split("_");
					e_x = Integer.parseInt(coords[0]);
					e_z = Integer.parseInt(coords[1]);
					e_dx = Integer.parseInt(coords[2]);
					e_dz = Integer.parseInt(coords[3]);

					String totalChunksLine = reader.readLine();

					if (totalChunksLine != null) {
						e_totalChunksProcessed = Long.parseLong(totalChunksLine);
					}
					lastLine = reader.readLine();
				}
				Bukkit.broadcastMessage("Loaded " + e_totalChunksProcessed + " processed chunks from: " + e_dataFile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
