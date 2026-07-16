package main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorldManager {
	public static final String PREFIX = "skygridx_";
	private static volatile boolean forceBaseWorlds;
	private static final Map<World.Environment, String> NAMES = new EnumMap<>(World.Environment.class);

	static {
		NAMES.put(World.Environment.NORMAL, "world");
		NAMES.put(World.Environment.NETHER, "world_nether");
		NAMES.put(World.Environment.THE_END, "world_the_end");
	}

	public static boolean isCustomWorld(Block block) {
		return isCustomWorld(block.getWorld());
	}

	public static boolean isCustomWorld(Player player) {
		return isCustomWorld(player.getWorld());
	}

	public static boolean isCustomWorld(World world) {
		if (world == null) {
			return false;
		}
		String name = world.getName();
		if (name.startsWith(PREFIX)) {
			return true;
		}
		return usesBaseWorlds() && isBaseSkyGridWorld(world);
	}

	public static boolean usesBaseWorlds() {
		return forceBaseWorlds || isFolia();
	}

	public static boolean isFolia() {
		try {
			String serverName = Bukkit.getServer().getName();
			if (serverName != null && serverName.toLowerCase(Locale.ROOT).contains("folia")) {
				return true;
			}
			String version = Bukkit.getServer().getVersion();
			return version != null && version.toLowerCase(Locale.ROOT).contains("folia");
		} catch (Throwable ignored) {
			return false;
		}
	}

	public static String getWorldName(World.Environment environment) {
		String baseName = NAMES.getOrDefault(environment, NAMES.get(World.Environment.NORMAL));
		return usesBaseWorlds() ? baseName : PREFIX + baseName;
	}

	public static World getWorldForEnvironment(World.Environment environment) {
		return Bukkit.getWorld(getWorldName(environment));
	}

	public static World getWorldByConfiguredName(String worldName) {
		World world = Bukkit.getWorld(worldName);
		if (world != null) {
			return world;
		}
		if (worldName == null) {
			return null;
		}
		if (usesBaseWorlds() && worldName.startsWith(PREFIX)) {
			return Bukkit.getWorld(getBaseWorldName(worldName));
		}
		if (!usesBaseWorlds() && isBaseWorldName(worldName)) {
			return Bukkit.getWorld(PREFIX + worldName);
		}
		return null;
	}

	public static List<World> getSkyGridWorlds() {
		List<World> worlds = new ArrayList<>(NAMES.size());
		for (World.Environment environment : NAMES.keySet()) {
			World world = getWorldForEnvironment(environment);
			if (world != null) {
				worlds.add(world);
			}
		}
		return worlds;
	}

	public static List<String> getConfiguredWorldNames() {
		return usesBaseWorlds() ? getBaseWorldNames() : getCustomWorldNames();
	}

	public static List<String> getInactiveWorldNames() {
		return usesBaseWorlds() ? getCustomWorldNames() : getBaseWorldNames();
	}

	private static List<String> getBaseWorldNames() {
		List<String> names = new ArrayList<>(NAMES.size());
		for (String name : NAMES.values()) {
			names.add(name);
		}
		return names;
	}

	private static List<String> getCustomWorldNames() {
		List<String> names = new ArrayList<>(NAMES.size());
		for (String name : NAMES.values()) {
			names.add(PREFIX + name);
		}
		return names;
	}

	public static boolean isSkyGridEndWorld(World world) {
		return isCustomWorld(world) && world.getEnvironment() == World.Environment.THE_END;
	}

	public static boolean isConfiguredWorldName(String worldName) {
		if (worldName == null) {
			return false;
		}
		return worldName.startsWith(PREFIX) || usesBaseWorlds() && isBaseWorldName(worldName);
	}

	public static String getBaseWorldName(String worldName) {
		if (worldName != null && worldName.startsWith(PREFIX)) {
			return worldName.substring(PREFIX.length());
		}
		return worldName;
	}

	public static void setupWorlds(JavaPlugin plugin) {
		setupWorlds(plugin, null);
	}

	public static void setupWorlds(JavaPlugin plugin, Runnable afterSetup) {
		SkyGridScheduler.runGlobal(plugin, () -> {
			if (usesBaseWorlds()) {
				prepareBaseWorlds(plugin);
			} else {
				createWorlds(plugin);
			}
			CustomBedManager.reloadBeds();
			if (afterSetup != null) {
				afterSetup.run();
			}
		});
	}

	private static void createWorlds(JavaPlugin plugin) {
		for (Map.Entry<World.Environment, String> e : NAMES.entrySet()) {
			World.Environment env = e.getKey();
			String vanilla = e.getValue();
			String customName = PREFIX + vanilla;
			if (Bukkit.getWorld(customName) != null) continue;
			World world;
			try {
				world = new WorldCreator(customName).environment(env).generator(new VoidWorldGenerator()).createWorld();
			} catch (UnsupportedOperationException ex) {
				forceBaseWorlds = true;
				plugin.getLogger().warning("World creation is unsupported on this server. Falling back to base-world SkyGrid mode.");
				prepareBaseWorlds(plugin);
				return;
			}
			if (world == null) continue;
			world.setSpawnLocation(0, 64, 0);
			if (env == World.Environment.THE_END) {
				SkyGridScheduler.runRegion(plugin, world, 0, 0,
						() -> world.getBlockAt(0, 65, 0).setType(Material.BEDROCK));
			}
		}
	}

	private static void prepareBaseWorlds(JavaPlugin plugin) {
		for (Map.Entry<World.Environment, String> e : NAMES.entrySet()) {
			World.Environment env = e.getKey();
			World world = Bukkit.getWorld(e.getValue());
			if (world == null) {
				plugin.getLogger().warning("Folia SkyGrid base world is not loaded: " + e.getValue());
				continue;
			}
			if (!isBaseSkyGridWorld(world)) {
				plugin.getLogger().warning("Expected " + e.getValue() + " to be a " + env + " world, but it is " + world.getEnvironment());
				continue;
			}
			world.setSpawnLocation(0, 64, 0);
			if (env == World.Environment.THE_END) {
				SkyGridScheduler.runRegion(plugin, world, 0, 0,
						() -> world.getBlockAt(0, 65, 0).setType(Material.BEDROCK));
			}
		}
	}

	private static boolean isBaseSkyGridWorld(World world) {
		String expectedName = NAMES.get(world.getEnvironment());
		return expectedName != null && expectedName.equals(world.getName());
	}

	private static boolean isBaseWorldName(String worldName) {
		return NAMES.containsValue(worldName);
	}
}
