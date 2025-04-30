package main;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.List;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class MiniRegenManager {
	private final SkyGridPlugin plugin;
	private final Generator generator;
	private final Map<String, MiniRegenSettings> miniRegenSettings;
	private final Map<String, MaterialManager.MaterialDistribution> miniRegenDistributions;
	private final AsyncDelayedScheduler scheduler = new AsyncDelayedScheduler();
	private final File folder;
	private final File settingsFile;
	private final File materialsFile;

	public MiniRegenManager(SkyGridPlugin plugin, Generator generator) {
		this.plugin = plugin;
		this.generator = generator;
		this.miniRegenSettings = new HashMap<>();
		this.miniRegenDistributions = new HashMap<>();
		this.folder = new File(plugin.getDataFolder(), "MiniRegen");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		this.settingsFile = new File(folder, "mini_regen_settings.yml");
		this.materialsFile = new File(folder, "mini_regen_materials.yml");
	}

	public void initialize() {
		loadDefaultSettings();
		loadDefaultMaterials();
		loadMiniRegenSettings();
		loadMiniRegenDistributions();
		scheduleRegenerationTasks();
	}

	private void loadDefaultSettings() {
		if (!settingsFile.exists()) {
			try (InputStream in = plugin.getResource("mini_regen_settings.yml");
					FileOutputStream out = new FileOutputStream(settingsFile)) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
			} catch (Exception e) {
				Cc.logS(Cc.RED, "Failed to copy default mini_regen_settings.yml: " + e.getMessage());
			}
		}
	}

	private void loadDefaultMaterials() {
		if (!materialsFile.exists()) {
			try (InputStream in = plugin.getResource("mini_regen_materials.yml");
					FileOutputStream out = new FileOutputStream(materialsFile)) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
			} catch (Exception e) {
				Cc.logS(Cc.RED, "Failed to copy default mini_regen_materials.yml: " + e.getMessage());
			}
		}
	}

	private void loadMiniRegenSettings() {
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(settingsFile);
			ConfigurationSection section = config.getConfigurationSection("miniRegen");
			if (section != null) {
				for (String key : section.getKeys(false)) {
					String worldName = config.getString("miniRegen." + key + ".world");
					String chunkStr = config.getString("miniRegen." + key + ".chunk");
					int interval = config.getInt("miniRegen." + key + ".interval");
					String distribution = config.getString("miniRegen." + key + ".distribution");
					String group = config.getString("miniRegen." + key + ".group", "");
					if (worldName != null && chunkStr != null) {
						String[] coords = chunkStr.split(",");
						if (coords.length == 2) {
							int chunkX = Integer.parseInt(coords[0].trim());
							int chunkZ = Integer.parseInt(coords[1].trim());
							MiniRegenSettings settings = new MiniRegenSettings(key, worldName, chunkX, chunkZ, distribution, interval, group);
							miniRegenSettings.put(key, settings);
						}
					}
				}
			}
		} catch (Exception e) {
			Cc.logS(Cc.RED, "Failed to load mini regen settings: " + e.getMessage());
		}
	}

	private void loadMiniRegenDistributions() {
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(materialsFile);
			ConfigurationSection section = config.getConfigurationSection("distributions");
			if (section != null) {
				for (String key : section.getKeys(false)) {
					ConfigurationSection distSection = section.getConfigurationSection(key);
					if (distSection != null) {
						Object2DoubleOpenHashMap<Material> distributionMap = new Object2DoubleOpenHashMap<>();
						for (String matKey : distSection.getKeys(false)) {
							double percentage = distSection.getDouble(matKey);
							Material material = Material.getMaterial(matKey.trim().toUpperCase());
							if (material != null) {
								distributionMap.put(material, percentage);
							} else {
								plugin.getLogger().warning(Cc.YELLOW.getAnsi() + "Invalid material '" + matKey 
										+ "' in mini regen distribution '" + key + "'" + Cc.RESET.getAnsi());
							}
						}
						if (!distributionMap.isEmpty()) {
							MaterialManager.MaterialDistribution distribution = new MaterialManager.MaterialDistribution(distributionMap);
							miniRegenDistributions.put(key, distribution);
						}
					}
				}
			}
		} catch (Exception e) {
			Cc.logS(Cc.RED, "Failed to load mini regen distributions: " + e.getMessage());
		}
	}

	private void scheduleRegenerationTasks() {
		for (MiniRegenSettings settings : miniRegenSettings.values()) {
			scheduleTaskForChunk(settings);
		}
	}

	private void scheduleTaskForChunk(MiniRegenSettings settings) {
		String key = settings.alias;
		scheduler.scheduleAtFixedRate(() -> {
			if (!plugin.isEnabled()) return;
			if (!miniRegenSettings.containsKey(key)) return;
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				if (!plugin.isEnabled()) return;
				World world = plugin.getServer().getWorld(settings.worldName);
				if (world != null) {
					Chunk chunk = world.getChunkAt(settings.chunkX, settings.chunkZ);
					chunk.setForceLoaded(true);
					MaterialManager.MaterialDistribution distribution = miniRegenDistributions.get(settings.distribution);
					if (distribution != null && chunk != null) {
						generator.regenerateMiniChunk(chunk, distribution);
					}
				}
			});
		}, settings.interval, settings.interval, TimeUnit.SECONDS, () -> plugin.isEnabled());
	}

	public void addMiniRegen(String worldName, int chunkX, int chunkZ, String alias, String distribution, int interval, String group) {
		String key = alias;
		MiniRegenSettings settings = new MiniRegenSettings(alias, worldName, chunkX, chunkZ, distribution, interval, group);
		miniRegenSettings.put(key, settings);
		scheduleTaskForChunk(settings);
		updateSettingsFile();
	}

	public void removeMiniRegen(String alias) {
		miniRegenSettings.remove(alias);
		updateSettingsFile();
	}

	public void removeMiniRegenGroup(String group) {
		// Remove all settings that match the given group.
		for (String alias : miniRegenSettings.keySet().stream().collect(Collectors.toList())) {
			MiniRegenSettings settings = miniRegenSettings.get(alias);
			if (settings.group != null && settings.group.equalsIgnoreCase(group)) {
				miniRegenSettings.remove(alias);
			}
		}
		updateSettingsFile();
	}

	private void updateSettingsFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			for (MiniRegenSettings s : miniRegenSettings.values()) {
				String key = s.alias;
				config.set("miniRegen." + key + ".world", s.worldName);
				config.set("miniRegen." + key + ".chunk", s.chunkX + "," + s.chunkZ);
				config.set("miniRegen." + key + ".distribution", s.distribution);
				config.set("miniRegen." + key + ".interval", s.interval);
				config.set("miniRegen." + key + ".group", s.group);
			}
			config.save(settingsFile);
		} catch (Exception e) {
			Cc.logS(Cc.RED, "Failed to update mini regen settings file: " + e.getMessage());
		}
	}

	public void shutdown() {
		scheduler.setEnabled(false);
	}

	public Map<String, MiniRegenSettings> getMiniRegenSettings() {
		return miniRegenSettings;
	}

	public Map<String, MaterialManager.MaterialDistribution> getMiniRegenDistributions() {
		return miniRegenDistributions;
	}

	// Helper method to get existing unique groups.
	public List<String> getExistingGroups() {
		return miniRegenSettings.values().stream()
				.map(s -> s.group)
				.filter(g -> g != null && !g.isEmpty())
				.distinct()
				.collect(Collectors.toList());
	}

	public static class MiniRegenSettings {
		public final String alias;
		public final String worldName;
		public final int chunkX;
		public final int chunkZ;
		public final String distribution;
		public final int interval;
		public final String group;

		public MiniRegenSettings(String alias, String worldName, int chunkX, int chunkZ, String distribution, int interval, String group) {
			this.alias = alias;
			this.worldName = worldName;
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
			this.distribution = distribution;
			this.interval = interval;
			this.group = group;
		}
	}
}