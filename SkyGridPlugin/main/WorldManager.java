package main;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

public class WorldManager {
	public static final String PREFIX = "skygridx_";
	private static final Map<World.Environment, String> NAMES = new EnumMap<>(World.Environment.class);

	static {
		NAMES.put(World.Environment.NORMAL, "world");
		NAMES.put(World.Environment.NETHER, "world_nether");
		NAMES.put(World.Environment.THE_END, "world_the_end");
	}

	public static boolean isCustomWorld(Block block) {
		return block.getWorld().getName().startsWith(PREFIX);
	}

	public static boolean isCustomWorld(Player player) {
		return player.getWorld().getName().startsWith(PREFIX);
	}

	public static boolean isCustomWorld(World world) {
		return world.getName().startsWith(PREFIX);
	}

	public static void setupWorlds(JavaPlugin plugin) {
		for (Map.Entry<World.Environment, String> e : NAMES.entrySet()) {
			World.Environment env = e.getKey();
			String vanilla = e.getValue();
			String customName = PREFIX + vanilla;
			if (Bukkit.getWorld(customName) != null) continue;
			World world = new WorldCreator(customName)
					.environment(env)
					.generator(new VoidWorldGenerator())
					.createWorld();
			world.setSpawnLocation(0, 64, 0);
			if (env == World.Environment.THE_END) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					VoidWorldGenerator.CrystalManager.spawnCrystals(world);
				}, 20L);
			}
		}
	}
}