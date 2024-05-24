package main;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PluginSettings {
	private final JavaPlugin plugin;
	private final CustomConfig customConfig;
	private YamlConfiguration config;

	public PluginSettings(JavaPlugin plugin) {
		this.plugin = plugin;
		this.customConfig = new CustomConfig(plugin, "settings.yml");
		this.customConfig.saveDefaultConfig();
		loadConfig();
	}

	private void loadConfig() {
		String configContent = customConfig.loadConfig();
		config = YamlConfiguration.loadConfiguration(new StringReader(configContent));
		loadDefaults();
	}

	private void loadDefaults() {
		config.addDefault("generator.processDelay", 10);
		config.addDefault("generator.nether.minY", 0);
		config.addDefault("generator.nether.maxY", 128);
		config.addDefault("generator.end.minY", 0);
		config.addDefault("generator.end.maxY", 128);
		config.addDefault("generator.normal.minY", -64);
		config.addDefault("generator.normal.maxY", 64);
		config.addDefault("generator.default.minY", 0);
		config.addDefault("generator.default.maxY", 128);
		
		config.addDefault("tprCommand.b2bDelay", 10);
		config.addDefault("tprCommand.tprDelay", 30);
		config.addDefault("tprCommand.tprNetherDelay", 30);
		config.addDefault("tprCommand.tprEndDelay", 30);
		config.addDefault("tprCommand.maxX", 29999983);
		config.addDefault("tprCommand.maxZ", 29999983);
		config.addDefault("tprCommand.minX", -29999983);
		config.addDefault("tprCommand.minZ", -29999983);
		config.addDefault("tprCommand.destinationY", 64);

		config.addDefault("fog.autoEnable", false);

		config.options().copyDefaults(true);
		saveConfig();
	}

	public void saveConfig() {
		try {
			File tempFile = File.createTempFile("temp", ".yml");
			config.save(tempFile);
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new FileReader(tempFile));
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write(System.lineSeparator());
			}
			reader.close();
			customConfig.saveConfig(writer.toString());
			tempFile.delete();
		} catch (Exception e) {
			plugin.getLogger().severe("Could not save the settings.yml file!");
			e.printStackTrace();
		}
	}

	public int getProcessDelay() {
		return config.getInt("generator.processDelay");
	}

	public int netherMinY() {
		return config.getInt("generator.nether.minY");
	}

	public int netherMaxY() {
		return config.getInt("generator.nether.maxY");
	}

	public int endMaxY() {
		return config.getInt("generator.end.maxY");
	}

	public int endMinY() {
		return config.getInt("generator.end.minY");
	}

	public int normalMaxY() {
		return config.getInt("generator.normal.maxY");
	}

	public int normalMinY() {
		return config.getInt("generator.normal.minY");
	}

	public int defaultMaxY() {
		return config.getInt("generator.default.maxY");
	}

	public int defaultMinY() {
		return config.getInt("generator.default.minY");
	}
	
	public int getb2bDelay() {
		return config.getInt("tprCommand.b2bDelay");
	}

	public int getTprDelay() {
		return config.getInt("tprCommand.tprDelay");
	}
	
	public int getTprNetherDelay() {
		return config.getInt("tprCommand.tprNetherDelay");
	}
	
	public int getTprEndDelay() {
		return config.getInt("tprCommand.tprEndDelay");
	}

	public int getMaxX() {
		return config.getInt("tprCommand.maxX");
	}

	public int getMaxZ() {
		return config.getInt("tprCommand.maxZ");
	}

	public int getMinX() {
		return config.getInt("tprCommand.minX");
	}

	public int getMinZ() {
		return config.getInt("tprCommand.minZ");
	}

	public int getDestinationY() {
		return config.getInt("tprCommand.destinationY");
	}

	public boolean isFogAutoEnabled() {
		return config.getBoolean("fog.autoEnable");
	}
}