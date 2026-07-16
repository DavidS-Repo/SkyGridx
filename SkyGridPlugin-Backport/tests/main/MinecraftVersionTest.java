package main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftVersionTest {
	@Test
	void parsesPlainAndDecoratedVersions() {
		assertEquals(new MinecraftVersion(1, 21, 0), MinecraftVersion.parse("1.21").orElseThrow());
		assertEquals(new MinecraftVersion(1, 21, 11), MinecraftVersion.parse("1.21.11").orElseThrow());
		assertEquals(new MinecraftVersion(1, 21, 9),
				MinecraftVersion.parse("git-Paper-123 (MC: 1.21.9)").orElseThrow());
		assertTrue(MinecraftVersion.parse("not-a-version").isEmpty());
	}

	@Test
	void comparesPatchVersionsNumerically() {
		MinecraftVersion versionNine = new MinecraftVersion(1, 21, 9);
		MinecraftVersion versionEleven = new MinecraftVersion(1, 21, 11);
		assertTrue(versionNine.compareTo(versionEleven) < 0);
		assertTrue(versionEleven.compareTo(versionNine) > 0);
	}

	@Test
	void freshInstallRequiresNoManagedConfigurationFiles(@TempDir Path dataFolder) throws Exception {
		assertTrue(FirstBootChecker.isFreshInstall(dataFolder));
		Files.writeString(dataFolder.resolve("unrelated.txt"), "kept");
		assertTrue(FirstBootChecker.isFreshInstall(dataFolder));
		Files.writeString(dataFolder.resolve("settings.yml"), "generator: {}");
		assertFalse(FirstBootChecker.isFreshInstall(dataFolder));
	}
}
