package main;

import java.util.Locale;

import org.bukkit.Keyed;

public final class BiomeKeyUtil {
	private BiomeKeyUtil() {}

	/**
	 * Reads a biome through the stable Keyed API. Bukkit changed Biome from an
	 * enum class to a registry interface during 1.21, so direct Biome method
	 * invocations are not binary-compatible across the whole supported line.
	 */
	public static String fromBiome(Object biome) {
		if (biome instanceof Keyed keyed) {
			return normalize(keyed.getKey().toString());
		}
		return normalize(biome == null ? null : biome.toString());
	}

	public static String normalize(String biome) {
		if (biome == null) return "";
		String b = biome.trim();
		int colon = b.indexOf(':');
		if (colon >= 0) b = b.substring(colon + 1);
		b = b.replace(' ', '_').replace('-', '_');
		return b.toUpperCase(Locale.ROOT);
	}
}
