package main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DragonSpawnListener implements Listener {

	private final JavaPlugin plugin;

	public DragonSpawnListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDragonSpawn(CreatureSpawnEvent event) {
		if (event.getEntityType() != EntityType.ENDER_DRAGON) return;

		World world = event.getLocation().getWorld();
		if (world == null || !world.getName().equals("skygridx_world_the_end")) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				clearObstructions(world);
				generatePillars(world);
			}
		}.runTaskLater(plugin, 100L); // 5 seconds later (100 ticks)
	}

	private void clearObstructions(World world) {
		int clearRadius = VoidWorldGenerator.MAIN_RADIUS + 10;
		int r2 = clearRadius * clearRadius;

		for (int x = -clearRadius; x <= clearRadius; x++) {
			for (int z = -clearRadius; z <= clearRadius; z++) {
				if ((x * x + z * z) <= r2) {
					for (int y = 65; y <= 100; y++) {
						world.getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
		}
	}

	private void generatePillars(World world) {
		Random random = new Random();
		int pillarCount = VoidWorldGenerator.PILLAR_COUNT;
		int mainRadius = VoidWorldGenerator.MAIN_RADIUS;

		for (int i = 0; i < pillarCount; i++) {
			double angle = 2 * Math.PI * i / pillarCount;
			int centerX = (int) Math.round(mainRadius * Math.cos(angle));
			int centerZ = (int) Math.round(mainRadius * Math.sin(angle));

			int radius = 2 + random.nextInt(4); // 2-5
			int height = 73 + random.nextInt(18); // 73-90

			int[][] offsets;
			switch (radius) {
			case 2: offsets = VoidWorldGenerator.OFFSETS_RADIUS_2; break;
			case 3: offsets = VoidWorldGenerator.OFFSETS_RADIUS_3; break;
			case 4: offsets = VoidWorldGenerator.OFFSETS_RADIUS_4; break;
			case 5: offsets = VoidWorldGenerator.OFFSETS_RADIUS_5; break;
			default: offsets = new int[][]{{0, 0}}; break;
			}

			// Build the pillar
			for (int[] off : offsets) {
				int x = centerX + off[0];
				int z = centerZ + off[1];
				for (int y = 0; y <= height; y++) {
					world.getBlockAt(x, y, z).setType(Material.OBSIDIAN);
				}
			}

			// Place bedrock at the top center
			world.getBlockAt(centerX, height + 1, centerZ).setType(Material.BEDROCK);

			// Place the crystal on top of the bedrock
			world.spawn(new Location(world, centerX + 0.5, height + 2, centerZ + 0.5), EnderCrystal.class);
		}
	}
}