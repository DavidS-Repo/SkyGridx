package main;

import io.papermc.lib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PreGenerator {
	private static final String DATA_FILE_NAME = "pregenerator_data.txt";
	private boolean enabled = false;
	private final JavaPlugin plugin;
	private BukkitRunnable generationTask;

	private int chunksPerRun;
	private int taskDelay;
	private int printTime;
	private final World world;
	private Set<String> newProcessedChunks = new HashSet<>();
	private int lastProcessedX = 0;
	private int lastProcessedZ = 0;
	private int ticksElapsed = 0;
	private long totalWorldChunks = 14062361500009L;
	private long totalChunksProcessed = 0L;

	private int generationTaskId = -1; // Variable to store the ID of the generation task
	private CompletableFuture<Void> generationFuture; // CompletableFuture to track generation task completion

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
	}

	public void disable() {
		if (!enabled) {
			Bukkit.broadcastMessage("Pre-Generator is already disabled.");
			return;
		}
		saveProcessedChunks();
		if (generationTask != null) {
			generationTask.cancel();
		}
		enabled = false;
		Bukkit.broadcastMessage("Pre-generation disabled.");
	}

	public void setValues(int chunksPerRun, int taskDelay, int printTime) {
		this.chunksPerRun = chunksPerRun;
		this.taskDelay = taskDelay;
		this.printTime = printTime;
	}

	private void startGeneration() {
		generationTask = new BukkitRunnable() {
			int x = lastProcessedX;
			int z = lastProcessedZ;

			@Override
			public void run() {
				if (!enabled) {
					cancel();
					return;
				}
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					for (int i = 0; i < chunksPerRun; i++) {
						int targetX = x, targetZ = z;
						String chunkKey = targetX + "_" + targetZ;

						if (newProcessedChunks.add(chunkKey)) {
							// Create a custom executor
							Executor customExecutor = command -> Bukkit.getScheduler().runTask(plugin, command);

							generationFuture = CompletableFuture.runAsync(() -> {
								CompletableFuture<Chunk> chunkFuture = PaperLib.getChunkAtAsync(world, targetX, targetZ, true);

								chunkFuture.thenAcceptAsync(targetChunk -> {
									plugin.getServer().getScheduler().runTask(plugin, () -> {
										targetChunk.load(true);
										lastProcessedX = x;
										lastProcessedZ = z;
									});
								});
							}, customExecutor);
						}
						totalChunksProcessed++;
						nextChunk();
					}
					saveProcessedChunks();

					ticksElapsed -= taskDelay;
					if (ticksElapsed <= 0) {
						long remainingChunks = (long) (totalWorldChunks - (long) totalChunksProcessed);
						double remainingPercentage = 100 - ((double) remainingChunks / totalWorldChunks * 100);
						Bukkit.broadcastMessage(String.format("Completed %.10f%%, Chunks Remaining: %d", remainingPercentage, remainingChunks));
						ticksElapsed = printTime;
					}
				});
				unload();
			}

			private void nextChunk() {
				int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
				int directionIndex = (Math.abs(x) == Math.abs(z)) ? 1 : 0;

				x += directions[directionIndex][0];
				z += directions[directionIndex][1];

				if (Math.abs(x) == Math.abs(z) + 1 || Math.abs(x) == Math.abs(z)) {
					directionIndex = (directionIndex + 1) % 4;
				}
			}
		};
		generationTaskId = generationTask.runTaskTimer(plugin, 0, taskDelay).getTaskId(); // Store the task ID
	}

	private void unload() {
	    int[] chunkUnload = new int[1];
	    chunkUnload[0] = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
	        for (String chunkKey : newProcessedChunks) {
	            String[] coords = chunkKey.split("_");
	            int x = Integer.parseInt(coords[0]);
	            int z = Integer.parseInt(coords[1]);
	            world.unloadChunk(x, z, true);
	        }

	        // After unloading, cancel the generation task and reset the ID
	        plugin.getServer().getScheduler().cancelTask(chunkUnload[0]);
	        if (generationTaskId != -1) {
	            plugin.getServer().getScheduler().cancelTask(generationTaskId);
	            generationTaskId = -1;
	        }

	        // Check if generationFuture is completed before starting a new generation task
	        if (generationFuture != null && !generationFuture.isDone()) {
	            // Handle the completion of generationFuture
	            CompletableFuture<Void> previousFuture = generationFuture;
	            generationFuture = null;

	            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
	                try {
	                    // Wait for the previous future to complete (timeout: 1 second)
	                    previousFuture.get(1, TimeUnit.SECONDS);
	                } catch (InterruptedException | ExecutionException | TimeoutException e) {
	                    e.printStackTrace();
	                } finally {
	                    // Resume the generation task
	                    startGeneration();
	                }
	            });
	        } else {
	            // Generation future is already completed, resume the generation task
	            startGeneration();
	        }
	    }, 0, 0);
	}

	private void saveProcessedChunks() {
	    File dataFile = new File(plugin.getDataFolder(), DATA_FILE_NAME);

	    try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile, false))) {
	        writer.println(lastProcessedX + "_" + lastProcessedZ);
	        writer.println(totalChunksProcessed);
	    } catch (IOException e) {
	        e.printStackTrace();
	        Bukkit.broadcastMessage("Total Processed Chunks " + totalChunksProcessed + ".");
	    }
	    newProcessedChunks.clear();
	}

	private void loadProcessedChunks() {
	    File dataFile = new File(plugin.getDataFolder(), DATA_FILE_NAME);
	    if (dataFile.exists()) {
	        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
	            String lastLine = reader.readLine();
	            int loadedChunks = 0;

	            while (lastLine != null) {
	                String[] coords = lastLine.split("_");
	                lastProcessedX = Integer.parseInt(coords[0]);
	                lastProcessedZ = Integer.parseInt(coords[1]);
	                loadedChunks++;

	                String totalChunksLine = reader.readLine();
	                if (totalChunksLine != null) {
	                    totalChunksProcessed = Long.parseLong(totalChunksLine);
	                }

	                lastLine = reader.readLine();
	            }

	            Bukkit.broadcastMessage("Loaded " + loadedChunks + " processed chunks from: " + dataFile.getPath());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
}