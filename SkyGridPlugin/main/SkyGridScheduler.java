package main;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SkyGridScheduler {
	private SkyGridScheduler() {
	}

	public static void runGlobal(Plugin plugin, Runnable task) {
		Bukkit.getGlobalRegionScheduler().execute(plugin, task);
	}

	public static ScheduledTask runGlobalLater(Plugin plugin, Runnable task, long delayTicks) {
		return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, callback(task), delayTicks);
	}

	public static ScheduledTask runGlobalTimer(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
		return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, callback(task), initialDelayTicks, periodTicks);
	}

	public static void runRegion(Plugin plugin, Location location, Runnable task) {
		Bukkit.getRegionScheduler().execute(plugin, location, task);
	}

	public static void runRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
		Bukkit.getRegionScheduler().execute(plugin, world, chunkX, chunkZ, task);
	}

	public static ScheduledTask runRegionLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
		return Bukkit.getRegionScheduler().runDelayed(plugin, location, callback(task), delayTicks);
	}

	public static ScheduledTask runRegionLater(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
		return Bukkit.getRegionScheduler().runDelayed(plugin, world, chunkX, chunkZ, callback(task), delayTicks);
	}

	public static ScheduledTask runRegionTimer(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task,
			long initialDelayTicks, long periodTicks) {
		return Bukkit.getRegionScheduler().runAtFixedRate(plugin, world, chunkX, chunkZ, callback(task),
				initialDelayTicks, periodTicks);
	}

	public static ScheduledTask runRegionTimer(Plugin plugin, World world, int chunkX, int chunkZ,
			java.util.function.Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
		return Bukkit.getRegionScheduler().runAtFixedRate(plugin, world, chunkX, chunkZ, task,
				initialDelayTicks, periodTicks);
	}

	public static boolean runEntity(Entity entity, Plugin plugin, Runnable task) {
		return entity.getScheduler().execute(plugin, task, null, 1L);
	}

	public static boolean runEntityLater(Entity entity, Plugin plugin, Runnable task, long delayTicks) {
		return entity.getScheduler().execute(plugin, task, null, delayTicks);
	}

	public static ScheduledTask runEntityTimer(Entity entity, Plugin plugin, java.util.function.Consumer<ScheduledTask> task,
			long initialDelayTicks, long periodTicks) {
		return entity.getScheduler().runAtFixedRate(plugin, task, null, initialDelayTicks, periodTicks);
	}

	public static CompletableFuture<Void> runAsync(Plugin plugin, Runnable task) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
			Objects.requireNonNull(scheduledTask);
			try {
				task.run();
				future.complete(null);
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});
		return future;
	}

	public static <T> CompletableFuture<T> supplyAsync(Plugin plugin, Supplier<T> supplier) {
		CompletableFuture<T> future = new CompletableFuture<>();
		Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
			Objects.requireNonNull(scheduledTask);
			try {
				future.complete(supplier.get());
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});
		return future;
	}

	public static ScheduledTask runAsyncTimer(Plugin plugin, Runnable task, long initialDelay, long period, TimeUnit unit) {
		return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, callback(task), initialDelay, period, unit);
	}

	private static Consumer<ScheduledTask> callback(Runnable task) {
		return scheduledTask -> {
			Objects.requireNonNull(scheduledTask);
			task.run();
		};
	}
}
