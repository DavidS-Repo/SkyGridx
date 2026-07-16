package main;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VersionedDefaultsApplierTest {
	private static final Logger LOGGER = Logger.getLogger(VersionedDefaultsApplierTest.class.getName());

	@Test
	void baseVersionsDoNotRewriteFiles(@TempDir Path temporary) throws Exception {
		Path dataFolder = createFreshConfig(temporary);
		Path worldFile = dataFolder.resolve("SkygridBlocks/world.yml");
		byte[] before = Files.readAllBytes(worldFile);

		VersionedDefaultsApplier.ApplyResult result = apply(dataFolder, "1.21.1");

		assertEquals(0, result.changedEntries());
		assertArrayEquals(before, Files.readAllBytes(worldFile));
	}

	@Test
	void appliesOnlyContentAvailableByDetectedVersion(@TempDir Path temporary) throws Exception {
		Path dataFolder = createFreshConfig(temporary);
		apply(dataFolder, "git-Paper (MC: 1.21.4)");

		YamlConfiguration world = load(dataFolder.resolve("SkygridBlocks/world.yml"));
		YamlConfiguration spawners = load(dataFolder.resolve("SkygridBlocks/SpawnerSettings.yml"));
		YamlConfiguration chests = load(dataFolder.resolve("SkygridBlocks/ChestSettings.yml"));
		YamlConfiguration settings = load(dataFolder.resolve("settings.yml"));

		assertTrue(world.contains("default_distribution.PALE_OAK_LOG"));
		assertTrue(world.contains("default_distribution.RESIN_BLOCK"));
		assertFalse(world.contains("default_distribution.BUSH"));
		assertTrue(names(spawners.getStringList("SpawnerSettings.Spawner3.Entities")).contains("CREAKING"));
		assertTrue(spawners.getStringList("SpawnerSettings.Spawner3.Biomes").contains("PALE_GARDEN"));
		assertTrue(chestMaterials(chests, "ChestSettings.CustomChest2.Items").contains("RESIN_CLUMP"));
		assertTrue(settings.getStringList("tprCommand.DANGEROUSBLOCKS.Materials").contains("OPEN_EYEBLOSSOM"));
		assertTrue(Files.readString(dataFolder.resolve("SkygridBlocks/world.yml"))
				.contains("# SkyGrid world block selection guide:"));
	}

	@Test
	void appliesAllCumulativeDefaultsAndRenamesThrough12111(@TempDir Path temporary) throws Exception {
		Path dataFolder = createFreshConfig(temporary);
		VersionedDefaultsApplier.ApplyResult result = apply(dataFolder, "1.21.11");

		YamlConfiguration world = load(dataFolder.resolve("SkygridBlocks/world.yml"));
		YamlConfiguration nether = load(dataFolder.resolve("SkygridBlocks/world_nether.yml"));
		YamlConfiguration spawners = load(dataFolder.resolve("SkygridBlocks/SpawnerSettings.yml"));
		YamlConfiguration chests = load(dataFolder.resolve("SkygridBlocks/ChestSettings.yml"));

		assertTrue(result.changedEntries() > 100);
		assertTrue(world.contains("default_distribution.COPPER_CHEST"));
		assertTrue(nether.contains("default_distribution.DRIED_GHAST"));
		assertTrue(chestMaterials(chests, "ChestSettings.CustomChest2.Items").contains("NETHERITE_SPEAR"));
		assertTrue(Files.readString(dataFolder.resolve("SkygridBlocks/ChestSettings.yml")).contains("LUNGE:"));
		Set<String> netherChest = chestMaterials(chests, "ChestSettings.Custom_Chest3.Items");
		assertTrue(netherChest.contains("IRON_CHAIN"));
		assertFalse(netherChest.contains("CHAIN"));
		Set<String> overworldEntities = names(spawners.getStringList("SpawnerSettings.Spawner3.Entities"));
		assertTrue(overworldEntities.containsAll(List.of("CREAKING", "HAPPY_GHAST", "COPPER_GOLEM", "CAMEL_HUSK", "PARCHED")));
		Set<String> waterEntities = names(spawners.getStringList("SpawnerSettings.Spawner4.Entities"));
		assertTrue(waterEntities.containsAll(List.of("NAUTILUS", "ZOMBIE_NAUTILUS")));
	}

	@Test
	void everyCatalogMaterialHasAPlacementOrExplicitExclusion() throws Exception {
		YamlConfiguration catalog = new YamlConfiguration();
		catalog.options().pathSeparator('/');
		try (Reader reader = resourceReader("versioned-defaults.yml")) {
			catalog.load(reader);
		}
		ConfigurationSection versions = catalog.getConfigurationSection("versions");
		for (String version : versions.getKeys(false)) {
			ConfigurationSection section = versions.getConfigurationSection(version);
			Set<String> catalogMaterials = new LinkedHashSet<>(section.getStringList("catalog/materials"));
			Set<String> accountedFor = new LinkedHashSet<>();
			ConfigurationSection world = section.getConfigurationSection("defaults/world-materials");
			ConfigurationSection nether = section.getConfigurationSection("defaults/nether-materials");
			if (world != null) accountedFor.addAll(world.getKeys(false));
			if (nether != null) accountedFor.addAll(nether.getKeys(false));
			List<?> chestItems = section.getList("defaults/chest-items");
			if (chestItems != null) for (Object item : chestItems) {
				if (item instanceof String text) {
					accountedFor.add(beforeColon(text));
				} else if (item instanceof Map<?, ?> map && map.size() == 1) {
					accountedFor.add(map.keySet().iterator().next().toString());
				}
			}
			accountedFor.addAll(section.getStringList("catalog/excluded-materials"));
			assertEquals(catalogMaterials, accountedFor, "Unaccounted material catalog entries for " + version);
		}
	}

	private VersionedDefaultsApplier.ApplyResult apply(Path dataFolder, String version) throws Exception {
		try (Reader reader = resourceReader("versioned-defaults.yml")) {
			return new VersionedDefaultsApplier(LOGGER).apply(
					dataFolder, reader, MinecraftVersion.parse(version).orElseThrow());
		}
	}

	private Path createFreshConfig(Path temporary) throws IOException {
		Path dataFolder = temporary.resolve("SkyGrid");
		Files.createDirectories(dataFolder.resolve("SkygridBlocks"));
		copyResource("world.yml", dataFolder.resolve("SkygridBlocks/world.yml"));
		copyResource("world_nether.yml", dataFolder.resolve("SkygridBlocks/world_nether.yml"));
		copyResource("ChestSettings.yml", dataFolder.resolve("SkygridBlocks/ChestSettings.yml"));
		copyResource("SpawnerSettings.yml", dataFolder.resolve("SkygridBlocks/SpawnerSettings.yml"));
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

	private YamlConfiguration load(Path file) {
		return YamlConfiguration.loadConfiguration(file.toFile());
	}

	private Set<String> chestMaterials(YamlConfiguration config, String path) {
		List<?> raw = config.getList(path);
		Set<String> materials = new LinkedHashSet<>();
		if (raw == null) return materials;
		for (Object item : raw) {
			if (item instanceof String text) {
				materials.add(beforeColon(text));
			} else if (item instanceof Map<?, ?> map && map.size() == 1) {
				materials.add(map.keySet().iterator().next().toString());
			}
		}
		return materials;
	}

	private Set<String> names(List<String> values) {
		Set<String> names = new LinkedHashSet<>();
		for (String value : values) names.add(beforeColon(value));
		return names;
	}

	private static String beforeColon(String value) {
		int colon = value.indexOf(':');
		return (colon < 0 ? value : value.substring(0, colon)).trim();
	}
}
