package main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.entity.EnderCrystal;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {
	private static final int PILLAR_COUNT = 10;
	private static final int MAIN_RADIUS = 64;
	private static final int[] PILLAR_RADII  = {3, 3, 3, 3, 4, 4, 4, 4, 5, 5};
	private static final int[] PILLAR_HEIGHTS = {76, 79, 82, 85, 88, 91, 94, 97, 100, 103};

	@Override
	public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
		if (worldInfo.getEnvironment() == Environment.THE_END) {
			generatePillars(worldInfo, chunkX, chunkZ, chunkData);
			generatePortalBase(chunkX, chunkZ, chunkData);
		}
	}

	private void generatePillars(WorldInfo worldInfo, int chunkX, int chunkZ, ChunkData chunkData) {
		int baseX = chunkX << 4;
		int baseZ = chunkZ << 4;

		for (int i = 0; i < PILLAR_COUNT; i++) {
			double angle = 2 * Math.PI * i / PILLAR_COUNT;
			int centerX = (int) Math.round(MAIN_RADIUS * Math.cos(angle));
			int centerZ = (int) Math.round(MAIN_RADIUS * Math.sin(angle));
			int radius  = PILLAR_RADII[i];
			int height  = PILLAR_HEIGHTS[i];

			if (baseX + 15 < centerX - radius || baseX > centerX + radius ||
					baseZ + 15 < centerZ - radius || baseZ > centerZ + radius) {
				continue;
			}

			for (int x = baseX; x < baseX + 16; x++) {
				for (int z = baseZ; z < baseZ + 16; z++) {
					double dx = x - centerX;
					double dz = z - centerZ;
					if (dx * dx + dz * dz <= radius * radius) {
						for (int y = 0; y <= height; y++) {
							chunkData.setBlock(x - baseX, y, z - baseZ, Material.OBSIDIAN);
						}
					}
				}
			}
		}
	}

	private void generatePortalBase(int chunkX, int chunkZ, ChunkData chunkData) {
		if (chunkX == 0 && chunkZ == 0) {
			chunkData.setBlock(0, 64, 0, Material.BEDROCK);
		}
	}

	@Override public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}
	@Override public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}
	@Override public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}

	public static class CrystalManager {
		public static void spawnCrystals(World endWorld) {
			for (int i = 0; i < PILLAR_COUNT; i++) {
				double angle = 2 * Math.PI * i / PILLAR_COUNT;
				int x = (int) Math.round(MAIN_RADIUS * Math.cos(angle));
				int z = (int) Math.round(MAIN_RADIUS * Math.sin(angle));
				int height = PILLAR_HEIGHTS[i];

				Location loc = new Location(endWorld, x + 0.5, height + 2, z + 0.5);
				endWorld.spawn(loc, EnderCrystal.class);
			}
		}
	}
}
