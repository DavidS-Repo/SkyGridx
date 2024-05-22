package main;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ChunkLoader {

	private final JavaPlugin plugin;

	public ChunkLoader(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void loadSpawnChunksAndRun(Runnable task) {
		CompletableFuture<?>[] futures = Bukkit.getWorlds().stream().flatMap(world -> {
			Location spawnLocation = world.getSpawnLocation();
			int spawnX = spawnLocation.getBlockX() >> 4;
			int spawnZ = spawnLocation.getBlockZ() >> 4;
			int radius = 8; // Spawn chunks are typically a 16x16 area centered around the spawn point

			return Stream.iterate(-radius, x -> x <= radius, x -> x + 1).flatMap(x -> 
			Stream.iterate(-radius, z -> z <= radius, z -> z + 1).map(z -> 
			PaperLib.getChunkAtAsync(new Location(world, (spawnX + x) << 4, 0, (spawnZ + z) << 4)).thenAccept(chunk -> {
				chunk.setForceLoaded(true);
			})));
		}).toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(futures).thenRun(() -> {
			new BukkitRunnable() {
				@Override
				public void run() {
					task.run();
				}
			}.runTask(plugin);
		});
	}
}