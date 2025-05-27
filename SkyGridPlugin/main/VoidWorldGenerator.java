package main;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {
	public static final int PILLAR_COUNT = 10;
	public static final int MAIN_RADIUS = 42;

	// radius 2 (5×5 grid: -xxx-, xxxxx, xxoxx, xxxxx, -xxx-)
	public static final int[][] OFFSETS_RADIUS_2 = {
			{ -2, -1 }, { -2, 0 }, { -2, 1 },
			{ -1, -2 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { -1, 2 },
			{  0, -2 }, {  0, -1 }, {  0, 0 }, {  0, 1 }, {  0, 2 },
			{  1, -2 }, {  1, -1 }, {  1, 0 }, {  1, 1 }, {  1, 2 },
			{  2, -1 }, {  2, 0 }, {  2, 1 }
	};

	// radius 3 (7×7 grid: --xxx--, -xxxxx-, xxxxxxx, xxxoxxx, xxxxxxx, -xxxxx-, --xxx--)
	public static final int[][] OFFSETS_RADIUS_3 = {
			{ -3, -1 }, { -3, 0 }, { -3, 1 },
			{ -2, -2 }, { -2, -1 }, { -2, 0 }, { -2, 1 }, { -2, 2 },
			{ -1, -3 }, { -1, -2 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { -1, 2 }, { -1, 3 },
			{  0, -3 }, {  0, -2 }, {  0, -1 }, {  0, 0 }, {  0, 1 }, {  0, 2 }, {  0, 3 },
			{  1, -3 }, {  1, -2 }, {  1, -1 }, {  1, 0 }, {  1, 1 }, {  1, 2 }, {  1, 3 },
			{  2, -2 }, {  2, -1 }, {  2, 0 }, {  2, 1 }, {  2, 2 },
			{  3, -1 }, {  3, 0 }, {  3, 1 }
	};

	// radius 4 (9×9 grid: ---xxx---, --xxxxx--, -xxxxxxx-, xxxxxxxxx, xxxxoxxxx, xxxxxxxxx, -xxxxxxx-, --xxxxx--, ---xxx---)
	public static final int[][] OFFSETS_RADIUS_4 = {
			{ -4, -1 }, { -4, 0 }, { -4, 1 },
			{ -3, -2 }, { -3, -1 }, { -3, 0 }, { -3, 1 }, { -3, 2 },
			{ -2, -3 }, { -2, -2 }, { -2, -1 }, { -2, 0 }, { -2, 1 }, { -2, 2 }, { -2, 3 },
			{ -1, -4 }, { -1, -3 }, { -1, -2 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { -1, 2 }, { -1, 3 }, { -1, 4 },
			{  0, -4 }, {  0, -3 }, {  0, -2 }, {  0, -1 }, {  0, 0 }, {  0, 1 }, {  0, 2 }, {  0, 3 }, {  0, 4 },
			{  1, -4 }, {  1, -3 }, {  1, -2 }, {  1, -1 }, {  1, 0 }, {  1, 1 }, {  1, 2 }, {  1, 3 }, {  1, 4 },
			{  2, -3 }, {  2, -2 }, {  2, -1 }, {  2, 0 }, {  2, 1 }, {  2, 2 }, {  2, 3 },
			{  3, -2 }, {  3, -1 }, {  3, 0 }, {  3, 1 }, {  3, 2 },
			{  4, -1 }, {  4, 0 }, {  4, 1 }
	};

	// radius 5 (11×11 grid: ----xxx----, --xxxxxxx--, -xxxxxxxxx-, -xxxxxxxxx-, xxxxxxxxxxx, xxxxxoxxxxx, xxxxxxxxxxx, -xxxxxxxxx-, -xxxxxxxxx-, --xxxxxxx--, ----xxx----)
	public static final int[][] OFFSETS_RADIUS_5 = {
			// z = -5
			{ -1, -5 }, {  0, -5 }, {  1, -5 },
			{ -3, -4 }, { -2, -4 }, { -1, -4 }, {  0, -4 }, {  1, -4 }, {  2, -4 }, {  3, -4 },
			{ -4, -3 }, { -3, -3 }, { -2, -3 }, { -1, -3 }, {  0, -3 }, {  1, -3 }, {  2, -3 }, {  3, -3 }, {  4, -3 },
			{ -4, -2 }, { -3, -2 }, { -2, -2 }, { -1, -2 }, {  0, -2 }, {  1, -2 }, {  2, -2 }, {  3, -2 }, {  4, -2 },
			{ -5, -1 }, { -4, -1 }, { -3, -1 }, { -2, -1 }, { -1, -1 }, {  0, -1 }, {  1, -1 }, {  2, -1 }, {  3, -1 }, {  4, -1 }, {  5, -1 },
			{ -5,  0 }, { -4,  0 }, { -3,  0 }, { -2,  0 }, { -1,  0 }, {  0,  0 }, {  1,  0 }, {  2,  0 }, {  3,  0 }, {  4,  0 }, {  5,  0 },
			{ -5,  1 }, { -4,  1 }, { -3,  1 }, { -2,  1 }, { -1,  1 }, {  0,  1 }, {  1,  1 }, {  2,  1 }, {  3,  1 }, {  4,  1 }, {  5,  1 },
			{ -4,  2 }, { -3,  2 }, { -2,  2 }, { -1,  2 }, {  0,  2 }, {  1,  2 }, {  2,  2 }, {  3,  2 }, {  4,  2 },
			{ -4,  3 }, { -3,  3 }, { -2,  3 }, { -1,  3 }, {  0,  3 }, {  1,  3 }, {  2,  3 }, {  3,  3 }, {  4,  3 },
			{ -3,  4 }, { -2,  4 }, { -1,  4 }, {  0,  4 }, {  1,  4 }, {  2,  4 }, {  3,  4 },
			{ -1,  5 }, {  0,  5 }, {  1,  5 }
	};

	@Override public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}
	@Override public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}
	@Override public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}
}
