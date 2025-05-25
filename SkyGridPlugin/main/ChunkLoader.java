package main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ChunkLoader {

	private final JavaPlugin plugin;
	private static final boolean IS_PAPER = detectPaper(); // Paper detection
	private static final String WAIT_MESSAGE = Cc.logO(Cc.DARK_GREEN, "Please wait a couple seconds while we force load spawn chunks to ensure proper generation");
	private static final String READY_MESSAGE = Cc.logO(Cc.DARK_GREEN, "Chunks have been loaded. You can now connect!");
	private static final String THANKS_MESSAGE = Cc.logO(Cc.WHITE, "Thank you for installing SkyGridx :)");

	public ChunkLoader(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void loadChunksAndRun(Runnable task) {
		WorldManager.setupWorlds(plugin);
		if (plugin instanceof SkyGridPlugin) {
			((SkyGridPlugin) plugin).setChunksLoading(true);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				Cc.logSB(WAIT_MESSAGE);
			}
		}.runTask(plugin);

		List<World> worlds = Bukkit.getWorlds().stream().filter(world -> world.getName().startsWith(WorldManager.PREFIX)).collect(Collectors.toList());
		int centerX = 0;
		int centerZ = 0;
		int chunkRange = 6; // 4 chunks in each direction

		if (IS_PAPER) {
			CompletableFuture<?>[] futures = worlds.stream().flatMap(world -> {
				CompletableFuture<?>[] worldFutures = new CompletableFuture<?>[(2 * chunkRange + 1) * (2 * chunkRange + 1)];
				int index = 0;
				for (int x = -chunkRange; x <= chunkRange; x++) {
					for (int z = -chunkRange; z <= chunkRange; z++) {
						Location loc = new Location(world, (centerX + x) << 4, 0, (centerZ + z) << 4);
						worldFutures[index++] = world.getChunkAtAsync(loc).thenAccept(chunk -> {
							chunk.setForceLoaded(true);
						});
					}
				}
				return List.of(worldFutures).stream();
			}).toArray(CompletableFuture[]::new);

			CompletableFuture.allOf(futures).thenRun(() -> finishLoading(task));
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (World world : worlds) {
						for (int x = -chunkRange; x <= chunkRange; x++) {
							for (int z = -chunkRange; z <= chunkRange; z++) {
								Location loc = new Location(world, (centerX + x) << 4, 0, (centerZ + z) << 4);
								world.getChunkAt(loc).setForceLoaded(true);
							}
						}
					}
					finishLoading(task);
				}
			}.runTask(plugin);
		}
	}

	private void finishLoading(Runnable task) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Cc.logSB(READY_MESSAGE);
				Cc.logSB(THANKS_MESSAGE);
				if (plugin instanceof SkyGridPlugin) {
					((SkyGridPlugin) plugin).setChunksLoading(false);
				}
				task.run();
			}
		}.runTask(plugin);
	}

	/**
	 * Detects if the server is running PaperMC.
	 */
	private static boolean detectPaper() {
		try {
			Class.forName("com.destroystokyo.paper.PaperConfig");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}