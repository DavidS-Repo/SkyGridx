package main;

import java.util.Locale;

public final class BiomeKeyUtil {
	private BiomeKeyUtil() {}

	public static String normalize(String biome) {
		if (biome == null) return "";
		String b = biome.trim();
		int colon = b.indexOf(':');
		if (colon >= 0) b = b.substring(colon + 1);
		b = b.replace(' ', '_').replace('-', '_');
		return b.toUpperCase(Locale.ROOT);
	}
}