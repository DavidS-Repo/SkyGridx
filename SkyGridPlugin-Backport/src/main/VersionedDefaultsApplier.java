package main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Applies cumulative, version-gated defaults after the base 1.21 files are copied.
 * Existing installations never call this class; see {@link FirstBootChecker}.
 */
public final class VersionedDefaultsApplier {
	private static final String CATALOG_RESOURCE = "versioned-defaults.yml";
	private static final String CHEST_ITEMS_PATH = "ChestSettings.CustomChest2.Items";
	private static final String OVERWORLD_ENTITIES_PATH = "SpawnerSettings.Spawner3.Entities";
	private static final String WATER_ENTITIES_PATH = "SpawnerSettings.Spawner4.Entities";
	private static final String NETHER_ENTITIES_PATH = "SpawnerSettings.Spawner2.Entities";
	private static final String SPAWNER_BIOMES_PATH = "SpawnerSettings.Spawner3.Biomes";
	private static final String CHEST_BIOMES_PATH = "ChestSettings.CustomChest2.Biomes";
	private static final String DANGEROUS_MATERIALS_PATH = "tprCommand.DANGEROUSBLOCKS.Materials";

	private final Logger logger;

	VersionedDefaultsApplier(Logger logger) {
		this.logger = logger;
	}

	public static boolean applyForRunningServer(JavaPlugin plugin) {
		String rawVersion = Bukkit.getMinecraftVersion();
		Optional<MinecraftVersion> parsedVersion = MinecraftVersion.parse(rawVersion);
		if (parsedVersion.isEmpty()) {
			plugin.getLogger().warning("Could not parse Minecraft version '" + rawVersion
					+ "'; keeping the base 1.21 fresh-install defaults.");
			return false;
		}

		try (InputStream input = plugin.getResource(CATALOG_RESOURCE)) {
			if (input == null) {
				plugin.getLogger().warning("Missing " + CATALOG_RESOURCE
						+ "; keeping the base 1.21 fresh-install defaults.");
				return false;
			}
			VersionedDefaultsApplier applier = new VersionedDefaultsApplier(plugin.getLogger());
			ApplyResult result = applier.apply(
					plugin.getDataFolder().toPath(),
					new InputStreamReader(input, StandardCharsets.UTF_8),
					parsedVersion.get());
			plugin.getLogger().info("Fresh-install defaults matched to Minecraft " + parsedVersion.get()
					+ ": " + result.changedEntries() + " versioned entries applied through "
					+ result.lastAppliedVersion() + ".");
			return true;
		} catch (IOException | InvalidConfigurationException e) {
			plugin.getLogger().warning("Could not apply versioned fresh-install defaults: " + e.getMessage());
			return false;
		}
	}

	ApplyResult apply(Path dataFolder, Reader catalogReader, MinecraftVersion serverVersion)
			throws IOException, InvalidConfigurationException {
		YamlConfiguration catalog = new YamlConfiguration();
		// Version keys contain dots (for example 1.21.4), so dots cannot be the
		// path separator while this catalog is loaded.
		catalog.options().pathSeparator('/');
		catalog.load(catalogReader);

		EditableYaml world = load(dataFolder.resolve("SkygridBlocks/world.yml"));
		EditableYaml nether = load(dataFolder.resolve("SkygridBlocks/world_nether.yml"));
		EditableYaml chests = load(dataFolder.resolve("SkygridBlocks/ChestSettings.yml"));
		EditableYaml spawners = load(dataFolder.resolve("SkygridBlocks/SpawnerSettings.yml"));
		EditableYaml settings = load(dataFolder.resolve("settings.yml"));

		ConfigurationSection versions = catalog.getConfigurationSection("versions");
		if (versions == null) {
			throw new InvalidConfigurationException("Missing versions section in " + CATALOG_RESOURCE);
		}

		List<VersionSection> applicableVersions = new ArrayList<>();
		for (String versionKey : versions.getKeys(false)) {
			Optional<MinecraftVersion> parsed = MinecraftVersion.parse(versionKey);
			ConfigurationSection section = versions.getConfigurationSection(versionKey);
			if (parsed.isEmpty() || section == null) {
				logger.warning("Ignoring invalid version catalog section: " + versionKey);
				continue;
			}
			if (parsed.get().compareTo(serverVersion) <= 0) {
				applicableVersions.add(new VersionSection(parsed.get(), section));
			}
		}
		applicableVersions.sort(Comparator.comparing(VersionSection::version));

		MutableResult result = new MutableResult();
		MinecraftVersion lastApplied = new MinecraftVersion(1, 21, 0);
		for (VersionSection versionSection : applicableVersions) {
			ConfigurationSection defaults = versionSection.section().getConfigurationSection("defaults");
			if (defaults != null) {
				result.materials += addMaterialMap(world, "default_distribution",
						defaults.getConfigurationSection("world-materials"));
				result.materials += addMaterialMap(nether, "default_distribution",
						defaults.getConfigurationSection("nether-materials"));
				List<?> chestItems = defaults.getList("chest-items");
				result.chestItems += addChestItems(chests, chestItems == null ? List.of() : chestItems);
				result.entities += addWeightedNames(spawners, OVERWORLD_ENTITIES_PATH,
						defaults.getStringList("overworld-entities"));
				result.entities += addWeightedNames(spawners, WATER_ENTITIES_PATH,
						defaults.getStringList("water-entities"));
				result.entities += addWeightedNames(spawners, NETHER_ENTITIES_PATH,
						defaults.getStringList("nether-entities"));
				List<String> biomes = defaults.getStringList("biomes");
				result.biomes += addPlainNames(spawners, SPAWNER_BIOMES_PATH, biomes);
				result.biomes += addPlainNames(chests, CHEST_BIOMES_PATH, biomes);
				result.materials += addPlainNames(settings, DANGEROUS_MATERIALS_PATH,
						defaults.getStringList("dangerous-materials"));
				result.renames += renameChestMaterials(chests,
						defaults.getConfigurationSection("chest-material-renames"));
			}
			lastApplied = versionSection.version();
		}

		saveIfDirty(world);
		saveIfDirty(nether);
		saveIfDirty(chests);
		saveIfDirty(spawners);
		saveIfDirty(settings);

		return new ApplyResult(result.materials, result.chestItems, result.entities,
				result.biomes, result.renames, lastApplied);
	}

	private EditableYaml load(Path path) throws IOException, InvalidConfigurationException {
		if (!Files.isRegularFile(path)) {
			throw new IOException("Required fresh-install file is missing: " + path);
		}
		YamlConfiguration config = new YamlConfiguration();
		config.options().parseComments(true);
		config.load(path.toFile());
		return new EditableYaml(path, config);
	}

	private int addMaterialMap(EditableYaml target, String basePath, ConfigurationSection additions) {
		if (additions == null) return 0;
		int changed = 0;
		for (String material : additions.getKeys(false)) {
			String path = basePath + "." + material;
			if (target.config.contains(path)) continue;
			target.config.set(path, additions.get(material));
			target.dirty = true;
			changed++;
		}
		return changed;
	}

	private int addChestItems(EditableYaml target, List<?> additions) {
		if (additions.isEmpty()) return 0;
		List<?> raw = target.config.getList(CHEST_ITEMS_PATH);
		List<Object> items = raw == null ? new ArrayList<>() : new ArrayList<>(raw);
		Set<String> presentMaterials = new LinkedHashSet<>();
		for (Object item : items) {
			String material = configuredMaterial(item);
			if (material != null) presentMaterials.add(material);
		}

		int changed = 0;
		for (Object addition : additions) {
			String material = configuredMaterial(addition);
			if (material == null || !presentMaterials.add(material)) continue;
			items.add(addition);
			changed++;
		}
		if (changed > 0) {
			target.config.set(CHEST_ITEMS_PATH, items);
			target.dirty = true;
		}
		return changed;
	}

	private int addWeightedNames(EditableYaml target, String path, List<String> additions) {
		if (additions.isEmpty()) return 0;
		List<String> entries = new ArrayList<>(target.config.getStringList(path));
		Set<String> names = new LinkedHashSet<>();
		for (String entry : entries) names.add(nameBeforeColon(entry));

		int changed = 0;
		for (String addition : additions) {
			if (!names.add(nameBeforeColon(addition))) continue;
			entries.add(addition);
			changed++;
		}
		if (changed > 0) {
			target.config.set(path, entries);
			target.dirty = true;
		}
		return changed;
	}

	private int addPlainNames(EditableYaml target, String path, List<String> additions) {
		if (additions.isEmpty()) return 0;
		List<String> entries = new ArrayList<>(target.config.getStringList(path));
		Set<String> names = new LinkedHashSet<>(entries);
		int changed = 0;
		for (String addition : additions) {
			if (!names.add(addition)) continue;
			entries.add(addition);
			changed++;
		}
		if (changed > 0) {
			target.config.set(path, entries);
			target.dirty = true;
		}
		return changed;
	}

	private int renameChestMaterials(EditableYaml target, ConfigurationSection renames) {
		if (renames == null) return 0;
		ConfigurationSection chestRoot = target.config.getConfigurationSection("ChestSettings");
		if (chestRoot == null) return 0;

		Map<String, String> renameMap = new LinkedHashMap<>();
		for (String oldName : renames.getKeys(false)) {
			String newName = renames.getString(oldName);
			if (newName != null && !newName.isBlank()) renameMap.put(oldName, newName);
		}
		int changed = 0;
		for (String chestKey : chestRoot.getKeys(false)) {
			String itemsPath = "ChestSettings." + chestKey + ".Items";
			List<?> rawItems = target.config.getList(itemsPath);
			if (rawItems == null) continue;
			List<Object> updated = new ArrayList<>(rawItems.size());
			boolean listChanged = false;
			for (Object item : rawItems) {
				Object replacement = renameItem(item, renameMap);
				updated.add(replacement);
				if (replacement != item) {
					listChanged = true;
					changed++;
				}
			}
			if (listChanged) target.config.set(itemsPath, updated);
		}
		if (changed > 0) target.dirty = true;
		return changed;
	}

	private Object renameItem(Object item, Map<String, String> renames) {
		if (item instanceof String text) {
			String material = configuredMaterial(text);
			String replacement = renames.get(material);
			if (replacement == null) return item;
			int colon = text.indexOf(':');
			return replacement + (colon >= 0 ? text.substring(colon) : "");
		}
		if (item instanceof Map<?, ?> map && map.size() == 1) {
			Map.Entry<?, ?> entry = map.entrySet().iterator().next();
			String replacement = renames.get(entry.getKey().toString());
			if (replacement == null) return item;
			Map<String, Object> updated = new LinkedHashMap<>();
			updated.put(replacement, entry.getValue());
			return updated;
		}
		return item;
	}

	private String configuredMaterial(Object item) {
		if (item instanceof String text) return nameBeforeColon(text);
		if (item instanceof Map<?, ?> map && map.size() == 1) {
			return map.keySet().iterator().next().toString();
		}
		return null;
	}

	private String nameBeforeColon(String value) {
		int colon = value.indexOf(':');
		return (colon >= 0 ? value.substring(0, colon) : value).trim();
	}

	private void saveIfDirty(EditableYaml editable) throws IOException {
		if (!editable.dirty) return;
		Path parent = editable.path.getParent();
		Path temporary = Files.createTempFile(parent, editable.path.getFileName().toString(), ".tmp");
		try {
			Files.writeString(temporary, editable.config.saveToString(), StandardCharsets.UTF_8);
			try {
				Files.move(temporary, editable.path, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, editable.path, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	record ApplyResult(int materials, int chestItems, int entities, int biomes, int renames,
			MinecraftVersion lastAppliedVersion) {
		int changedEntries() {
			return materials + chestItems + entities + biomes + renames;
		}
	}

	private record VersionSection(MinecraftVersion version, ConfigurationSection section) {}

	private static final class EditableYaml {
		private final Path path;
		private final YamlConfiguration config;
		private boolean dirty;

		private EditableYaml(Path path, YamlConfiguration config) {
			this.path = path;
			this.config = config;
		}
	}

	private static final class MutableResult {
		private int materials;
		private int chestItems;
		private int entities;
		private int biomes;
		private int renames;
	}
}
