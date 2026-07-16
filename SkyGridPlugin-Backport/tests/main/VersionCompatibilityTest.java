package main;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Keyed;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Enabled by the compatibility-matrix build with the Paper API version under test.
 */
class VersionCompatibilityTest {
	@Test
	void generatedDefaultsUseOnlyNamesFromTheTargetPaperApi(@TempDir Path temporary) throws Exception {
		String targetText = System.getProperty("skygrid.test.minecraftVersion");
		assumeTrue(targetText != null && !targetText.isBlank(),
				"Compatibility API validation is enabled by the matrix build");
		MinecraftVersion target = MinecraftVersion.parse(targetText).orElseThrow();
		Path dataFolder = createFreshConfig(temporary);

		try (Reader reader = resourceReader("versioned-defaults.yml")) {
			new VersionedDefaultsApplier(Logger.getAnonymousLogger()).apply(dataFolder, reader, target);
		}

		validateWorld(dataFolder.resolve("SkygridBlocks/world.yml"));
		validateWorld(dataFolder.resolve("SkygridBlocks/world_nether.yml"));
		validateWorld(dataFolder.resolve("SkygridBlocks/world_the_end.yml"));
		validateMaterialMap(dataFolder.resolve("OreGenBlock/ores.yml"));
		validateMaterialMap(dataFolder.resolve("mini_regen_materials.yml"));
		validateSettings(dataFolder.resolve("settings.yml"));
		validateSpawners(dataFolder.resolve("SkygridBlocks/SpawnerSettings.yml"));
		validateChests(dataFolder.resolve("SkygridBlocks/ChestSettings.yml"));
		validateCatalog(target);
		assertEquals("PLAINS", BiomeKeyUtil.fromBiome((Keyed) () -> NamespacedKey.minecraft("plains")));
		assertDoesNotThrow(() -> Sound.class.getField("ENTITY_ENDER_EYE_LAUNCH"));
	}

	private void validateWorld(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		ConfigurationSection biomes = config.getConfigurationSection("biomes");
		if (biomes != null) for (String biomeGroup : biomes.getKeys(false)) {
			for (String biome : biomeGroup.split(",")) validateBiome(biome.trim());
		}
		ConfigurationSection distributions = config.getConfigurationSection("distributions");
		if (distributions != null) for (String name : distributions.getKeys(false)) {
			validateDistribution(distributions.getConfigurationSection(name));
		}
		validateDistribution(config.getConfigurationSection("default_distribution"));
	}

	private void validateDistribution(ConfigurationSection distribution) {
		if (distribution == null) return;
		for (String material : distribution.getKeys(false)) validateMaterial(material);
	}

	private void validateMaterialMap(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		validateMaterialMapSection(config);
	}

	private void validateMaterialMapSection(ConfigurationSection section) {
		for (String key : section.getKeys(false)) {
			ConfigurationSection child = section.getConfigurationSection(key);
			if (isMaterial(key)) validateMaterial(key);
			if (child != null) validateMaterialMapSection(child);
		}
	}

	private void validateSettings(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		for (String material : config.getStringList("tprCommand.DANGEROUSBLOCKS.Materials")) validateMaterial(material);
		for (String material : config.getStringList("EventControl.GRAVITY_AFFECTED_BLOCKS.Materials")) validateMaterial(material);
	}

	private void validateSpawners(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		ConfigurationSection root = config.getConfigurationSection("SpawnerSettings");
		for (String key : root.getKeys(false)) {
			ConfigurationSection spawner = root.getConfigurationSection(key);
			for (String entity : spawner.getStringList("Entities")) {
				String name = beforeColon(entity);
				assertDoesNotThrow(() -> EntityType.valueOf(name), "Missing EntityType " + name);
			}
			for (String biome : spawner.getStringList("Biomes")) validateBiome(biome);
		}
	}

	private void validateChests(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		ConfigurationSection root = config.getConfigurationSection("ChestSettings");
		Set<String> customChestNames = root.getKeys(false);
		for (String key : customChestNames) {
			ConfigurationSection chest = root.getConfigurationSection(key);
			List<?> items = chest.getList("Items");
			if (items != null) for (Object item : items) {
				if (item instanceof String text) validateMaterial(beforeColon(text));
				if (item instanceof Map<?, ?> map && map.size() == 1) {
					validateMaterial(map.keySet().iterator().next().toString());
					validateEnchantments(map.values().iterator().next());
				}
			}
			for (String biome : chest.getStringList("Biomes")) validateBiome(biome);
			List<?> lootTables = chest.getList("LootTables");
			if (lootTables != null) for (Object entry : lootTables) {
				if (!(entry instanceof Map<?, ?> map) || map.isEmpty()) continue;
				String name = map.keySet().iterator().next().toString();
				if (!customChestNames.contains(name)) {
					assertDoesNotThrow(() -> LootTables.valueOf(name), "Missing LootTables value " + name);
				}
			}
		}
	}

	private void validateEnchantments(Object node) {
		if (node instanceof List<?> list) {
			for (Object child : list) validateEnchantments(child);
			return;
		}
		if (!(node instanceof Map<?, ?> map)) return;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey().toString().equals("Enchantments") && entry.getValue() instanceof List<?> enchantments) {
				for (Object enchantment : enchantments) {
					if (enchantment instanceof Map<?, ?> enchantmentMap && !enchantmentMap.isEmpty()) {
						validateEnchantment(enchantmentMap.keySet().iterator().next().toString());
					}
				}
			} else {
				validateEnchantments(entry.getValue());
			}
		}
	}

	private void validateCatalog(MinecraftVersion target) throws Exception {
		YamlConfiguration catalog = new YamlConfiguration();
		catalog.options().pathSeparator('/');
		try (Reader reader = resourceReader("versioned-defaults.yml")) {
			catalog.load(reader);
		}
		ConfigurationSection versions = catalog.getConfigurationSection("versions");
		for (String key : versions.getKeys(false)) {
			MinecraftVersion version = MinecraftVersion.parse(key).orElseThrow();
			if (version.compareTo(target) > 0) continue;
			ConfigurationSection section = versions.getConfigurationSection(key);
			for (String name : section.getStringList("catalog/materials")) validateMaterial(name);
			for (String name : section.getStringList("catalog/entities")) {
				assertDoesNotThrow(() -> EntityType.valueOf(name), "Missing catalog EntityType " + name);
			}
			for (String name : section.getStringList("catalog/biomes")) validateBiome(name);
			for (String name : section.getStringList("catalog/enchantments")) validateEnchantment(name);
			for (String name : section.getStringList("catalog/removed-materials")) assertNull(Material.getMaterial(name));
			for (String name : section.getStringList("catalog/removed-entities")) {
				assertDoesNotThrow(() -> {
					try {
						EntityType.valueOf(name);
						throw new AssertionError("Removed EntityType still exists: " + name);
					} catch (IllegalArgumentException expected) {
						// Expected removal.
					}
				});
			}
		}
	}

	private void validateMaterial(String name) {
		assertNotNull(Material.getMaterial(name), "Missing Material " + name);
	}

	private boolean isMaterial(String name) {
		return Material.getMaterial(name) != null;
	}

	private void validateBiome(String name) {
		assertDoesNotThrow(() -> Biome.class.getField(name), "Missing Biome field " + name);
	}

	private void validateEnchantment(String name) {
		assertDoesNotThrow(() -> Enchantment.class.getField(name), "Missing Enchantment field " + name);
	}

	private Path createFreshConfig(Path temporary) throws IOException {
		Path dataFolder = temporary.resolve("SkyGrid");
		Files.createDirectories(dataFolder.resolve("SkygridBlocks"));
		Files.createDirectories(dataFolder.resolve("OreGenBlock"));
		for (String file : List.of("world.yml", "world_nether.yml", "world_the_end.yml", "ChestSettings.yml", "SpawnerSettings.yml")) {
			copyResource(file, dataFolder.resolve("SkygridBlocks").resolve(file));
		}
		copyResource("ores.yml", dataFolder.resolve("OreGenBlock/ores.yml"));
		copyResource("mini_regen_materials.yml", dataFolder.resolve("mini_regen_materials.yml"));
		copyResource("settings.yml", dataFolder.resolve("settings.yml"));
		return dataFolder;
	}

	private void copyResource(String name, Path destination) throws IOException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(name)) {
			if (input == null) throw new IOException("Missing test resource: " + name);
			Files.copy(input, destination);
		}
	}

	private Reader resourceReader(String name) throws IOException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(name);
		if (input == null) throw new IOException("Missing test resource: " + name);
		return new InputStreamReader(input, StandardCharsets.UTF_8);
	}

	private static String beforeColon(String value) {
		int colon = value.indexOf(':');
		return (colon < 0 ? value : value.substring(0, colon)).trim();
	}
}
