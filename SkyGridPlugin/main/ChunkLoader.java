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
	private static final boolean IS_PAPER = detectPaper();
	private static final String WAIT_MESSAGE = Cc.logO(Cc.DARK_GREEN, "Please wait a couple seconds while we force load spawn chunks to ensure proper generation");
	private static final String READY_MESSAGE = Cc.logO(Cc.DARK_GREEN, "Chunks have been loaded. You can now connect!");
	private static final String THANKS_MESSAGE = Cc.logO(Cc.WHITE, "Thank you for installing SkyGridx :)");

	public ChunkLoader(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void loadChunksAndRun(Runnable task) {
		// ensure custom worlds exist and are loaded
		WorldManager.setupWorlds(plugin);

		if (plugin instanceof SkyGridPlugin) {
			((SkyGridPlugin) plugin).setChunksLoading(true);
		}

		// send wait message
		new BukkitRunnable() {
			@Override
			public void run() {
				Cc.logSB(WAIT_MESSAGE);
			}
		}.runTask(plugin);

		// gather only our skygrid worlds
		List<World> worlds = Bukkit.getWorlds().stream()
				.filter(w -> w.getName().startsWith(WorldManager.PREFIX))
				.collect(Collectors.toList());

		int centerX = 0;
		int centerZ = 0;
		int chunkRange = 8;

		if (IS_PAPER) {
			// async load on paper
			CompletableFuture<?>[] futures = worlds.stream().flatMap(world -> {
				CompletableFuture<?>[] worldFuts = new CompletableFuture<?>[(2 * chunkRange + 1) * (2 * chunkRange + 1)];
				int idx = 0;
				for (int dx = -chunkRange; dx <= chunkRange; dx++) {
					for (int dz = -chunkRange; dz <= chunkRange; dz++) {
						int cx = centerX + dx;
						int cz = centerZ + dz;
						Location loc = new Location(world, cx << 4, 0, cz << 4);
						worldFuts[idx++] = world.getChunkAtAsync(loc)
								.thenAccept(chunk -> {
									// ensure chunk is loaded before forcing
									world.loadChunk(cx, cz, false);
									chunk.setForceLoaded(true);
								});
					}
				}
				return List.of(worldFuts).stream();
			}).toArray(CompletableFuture[]::new);

			CompletableFuture.allOf(futures)
			.thenRun(() -> finishLoading(task));

		} else {
			// sync load on non-paper
			new BukkitRunnable() {
				@Override
				public void run() {
					for (World world : worlds) {
						for (int dx = -chunkRange; dx <= chunkRange; dx++) {
							for (int dz = -chunkRange; dz <= chunkRange; dz++) {
								int cx = centerX + dx;
								int cz = centerZ + dz;
								// load chunk synchronously
								world.loadChunk(cx, cz, false);
								world.getChunkAt(cx, cz).setForceLoaded(true);
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

	private static boolean detectPaper() {
		try {
			Class.forName("com.destroystokyo.paper.PaperConfig");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}