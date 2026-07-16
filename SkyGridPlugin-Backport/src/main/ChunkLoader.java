package main;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkLoader {

	private final JavaPlugin plugin;
	private static final String WAIT_MESSAGE = Cc.logO(Cc.DARK_GREEN, "Please wait a couple seconds while we force load spawn chunks to ensure proper generation");
	private static final String READY_MESSAGE = Cc.logO(Cc.DARK_GREEN, "Chunks have been loaded. You can now connect!");
	private static final String THANKS_MESSAGE = Cc.logO(Cc.WHITE, "Thank you for installing SkyGridx :)");

	public ChunkLoader(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void loadChunksAndRun(Runnable task) {
		// ensure custom worlds exist and are loaded
		WorldManager.setupWorlds(plugin, () -> startChunkLoad(task));
	}

	private void startChunkLoad(Runnable task) {
		if (plugin instanceof SkyGridPlugin skyGridPlugin) {
			skyGridPlugin.setChunksLoading(true);
		}

		Cc.logSB(WAIT_MESSAGE);

		List<World> worlds = WorldManager.getSkyGridWorlds();

		int centerX = 0;
		int centerZ = 0;
		int chunkRange = 8;

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (World world : worlds) {
			for (int dx = -chunkRange; dx <= chunkRange; dx++) {
				for (int dz = -chunkRange; dz <= chunkRange; dz++) {
					int cx = centerX + dx;
					int cz = centerZ + dz;
					Location loc = new Location(world, cx << 4, 0, cz << 4);
					futures.add(world.getChunkAtAsync(loc).thenCompose(loadedChunk ->
							forceChunkLoaded(loadedChunk.getWorld(), loadedChunk.getX(), loadedChunk.getZ())));
				}
			}
		}

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.thenRun(() -> finishLoading(task))
				.exceptionally(error -> {
					plugin.getLogger().warning("Failed while preloading spawn chunks: " + error.getMessage());
					finishLoading(task);
					return null;
				});
	}

	private CompletableFuture<Void> forceChunkLoaded(World world, int chunkX, int chunkZ) {
		CompletableFuture<Void> result = new CompletableFuture<>();
		SkyGridScheduler.runGlobal(plugin, () -> {
			try {
				world.setChunkForceLoaded(chunkX, chunkZ, true);
				result.complete(null);
			} catch (Throwable t) {
				result.completeExceptionally(t);
			}
		});
		return result;
	}

	private void finishLoading(Runnable task) {
		SkyGridScheduler.runGlobal(plugin, () -> {
				Cc.logSB(READY_MESSAGE);
				Cc.logSB(THANKS_MESSAGE);
				if (plugin instanceof SkyGridPlugin skyGridPlugin) {
					skyGridPlugin.setChunksLoading(false);
				}
				task.run();
		});
	}
}
