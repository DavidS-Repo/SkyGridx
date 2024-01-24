package main;

import io.papermc.lib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PreGenerator {
	private static final String DATA_FILE_NAME = "pregenerator_data.txt";
	private boolean enabled = false;
	private final JavaPlugin plugin;
	private BukkitRunnable generationTask;

	private int chunksPerRun, taskDelay, printTime;
	private int lastProcessedX = 0,lastProcessedZ = 0, ticksElapsed = 0, startCoordX = 0, startCoordZ = 0, ring = 0;
	private final World world;
	private long totalWorldChunks = 14062361500009L;
	private long totalChunksProcessed = 0L;

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
			@Override
			public void run() {
				if (!enabled) {
					cancel();
					return;
				}
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					for (int i = 0; i < chunksPerRun; i++) {
						int targetX = lastProcessedX, targetZ = lastProcessedZ;

						// Create a custom executor
						Executor customExecutor = command -> Bukkit.getScheduler().runTask(plugin, command);

						CompletableFuture.runAsync(() -> {
							CompletableFuture<Chunk> chunkFuture = PaperLib.getChunkAtAsync(world, targetX, targetZ, true);

							chunkFuture.thenAcceptAsync(targetChunk -> {
								plugin.getServer().getScheduler().runTask(plugin, () -> {
									targetChunk.unload(true);
									// Update the lastProcessedX and lastProcessedZ
									lastProcessedX = targetX;
									lastProcessedZ = targetZ;
								});
							});
						}, customExecutor);

						totalChunksProcessed++;
						nextChunk();
					}
					saveProcessedChunks();
				});
			}

			private void nextChunk() {
				ring = Math.max(Math.abs(lastProcessedX), Math.abs(lastProcessedZ));

				if (lastProcessedX == ring && lastProcessedZ < ring) {
					changeDirection(0, 1); // Move upward
				} else if (lastProcessedZ == ring && lastProcessedX < ring) {
					changeDirection(1, 0); // Move rightward
				} else if (lastProcessedX == -ring && lastProcessedZ > -ring) {
					changeDirection(0, -1); // Move downward
				} else if (lastProcessedZ == -ring && lastProcessedX > -ring) {
					changeDirection(-1, 0); // Move leftward
				} else if (lastProcessedZ == ring && lastProcessedX == ring) {
					// Completed the ring, move to the next one
					moveToNextRing();
				} else {
					// If none of the conditions match, print stack trace and throw exception
					new IllegalStateException("Unexpected condition in nextChunk().").printStackTrace();
				}
			}

			private void changeDirection(int deltaX, int deltaY) {
				lastProcessedX += deltaX;
				lastProcessedZ += deltaY;

				if (lastProcessedX == startCoordX && lastProcessedZ == startCoordZ) {
					moveToNextRing();
				}
			}

			private void moveToNextRing() {
				ring++;
				startCoordX = 0;
				startCoordZ = ring;
				lastProcessedZ = startCoordZ;
				lastProcessedX = startCoordX - 1;
			}

		};
		generationTask.runTaskTimer(plugin, 0, taskDelay).getTaskId(); // Store the task ID
	}

	private void printInfo(){
		ticksElapsed -= taskDelay;
		if (ticksElapsed <= 0) {
			long remainingChunks = (long) (totalWorldChunks - (long) totalChunksProcessed);
			double remainingPercentage = 100 - ((double) remainingChunks / totalWorldChunks * 100);
			Bukkit.broadcastMessage(String.format("Completed %.10f%%, Chunks Remaining: %d", remainingPercentage, remainingChunks));
			ticksElapsed = printTime;
		}
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
		printInfo();
	}


	private void loadProcessedChunks() {
		File dataFile = new File(plugin.getDataFolder(), DATA_FILE_NAME);
		if (dataFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
				String lastLine = reader.readLine();

				while (lastLine != null) {
					String[] coords = lastLine.split("_");
					lastProcessedX = Integer.parseInt(coords[0]);
					lastProcessedZ = Integer.parseInt(coords[1]);

					String totalChunksLine = reader.readLine();
					if (totalChunksLine != null) {
						totalChunksProcessed = Long.parseLong(totalChunksLine);
					}
					lastLine = reader.readLine();
				}
				Bukkit.broadcastMessage("Loaded " + totalChunksProcessed + " processed chunks from: " + dataFile.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
