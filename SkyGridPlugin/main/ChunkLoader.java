package main;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkLoader {

	private final JavaPlugin plugin;
	private static final String WAIT_MESSAGE = ChatColor.BOLD + "" + ChatColor.GREEN + "Please wait a couple seconds while we force load spawn chunks to ensure proper generation";
	private static final String READY_MESSAGE = ChatColor.BOLD + "" + ChatColor.GREEN + "Chunks have been loaded. You can now connect!";
	private static final String THANKS_MESSAGE = ChatColor.BOLD + "" + ChatColor.WHITE + "Thank you for installing SkyGridx :)";

	public ChunkLoader(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void loadChunksAndRun(Runnable task) {
		if (plugin instanceof SkyGridPlugin) {
			((SkyGridPlugin) plugin).setChunksLoading(true);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.broadcastMessage(WAIT_MESSAGE);
			}
		}.runTask(plugin);

		List<World> worlds = Bukkit.getWorlds();
		int centerX = 0;
		int centerZ = 0;
		int chunkRange = 4; // 4 chunks in each direction

		CompletableFuture<?>[] futures = worlds.stream().flatMap(world -> {
			CompletableFuture<?>[] worldFutures = new CompletableFuture<?>[(2 * chunkRange + 1) * (2 * chunkRange + 1)];
			int index = 0;
			for (int x = -chunkRange; x <= chunkRange; x++) {
				for (int z = -chunkRange; z <= chunkRange; z++) {
					Location loc = new Location(world, (centerX + x) << 4, 0, (centerZ + z) << 4);
					worldFutures[index++] = PaperLib.getChunkAtAsync(loc).thenAccept(chunk -> {
						chunk.setForceLoaded(true);
					});
				}
			}
			return List.of(worldFutures).stream();
		}).toArray(CompletableFuture[]::new);

		CompletableFuture.allOf(futures).thenRun(() -> {
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.broadcastMessage(READY_MESSAGE);
					Bukkit.broadcastMessage(THANKS_MESSAGE);
					if (plugin instanceof SkyGridPlugin) {
						((SkyGridPlugin) plugin).setChunksLoading(false);
					}
					task.run();
				}
			}.runTask(plugin);
		});
	}
}