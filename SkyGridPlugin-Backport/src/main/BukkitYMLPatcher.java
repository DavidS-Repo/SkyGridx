package main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class BukkitYMLPatcher {

	public static void applyGeneratorSettings(JavaPlugin plugin) {
		File bukkitYml = new File(Bukkit.getWorldContainer(), "bukkit.yml");
		if (!bukkitYml.exists()) return;
		YamlConfiguration config = YamlConfiguration.loadConfiguration(bukkitYml);
		String[] worlds = {
				"skygridx_world",
				"skygridx_world_nether",
				"skygridx_world_the_end"
		};

		for (String world : worlds) {
			String path = "worlds." + world + ".generator";
			config.set(path, plugin.getName());
		}
		try {
			config.save(bukkitYml);
			Bukkit.getLogger().info("[SkyGrid] bukkit.yml updated with generator settings.");
		} catch (IOException e) {
			Bukkit.getLogger().warning("[SkyGrid] Failed to write generator config to bukkit.yml: " + e.getMessage());
		}
	}
}