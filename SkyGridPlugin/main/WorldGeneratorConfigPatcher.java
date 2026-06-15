package main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class WorldGeneratorConfigPatcher {
	private WorldGeneratorConfigPatcher() {
	}

	public static void applyGeneratorSettings(JavaPlugin plugin) {
		File configFile = new File(Bukkit.getWorldContainer(), "bukkit.yml");
		if (!configFile.exists()) {
			return;
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		for (String world : WorldManager.getConfiguredWorldNames()) {
			config.set("worlds." + world + ".generator", plugin.getName());
		}
		for (String world : WorldManager.getInactiveWorldNames()) {
			config.set("worlds." + world + ".generator", null);
		}

		try {
			config.save(configFile);
			plugin.getLogger().info("Updated Paper/Folia world generator settings.");
		} catch (IOException e) {
			plugin.getLogger().warning("Failed to write bukkit.yml generator config: " + e.getMessage());
		}
	}
}
