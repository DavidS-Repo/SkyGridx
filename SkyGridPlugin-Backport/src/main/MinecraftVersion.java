package main;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A small, dependency-free Minecraft version value used for version-gated defaults.
 */
public record MinecraftVersion(int major, int minor, int patch) implements Comparable<MinecraftVersion> {
	private static final Pattern VERSION_PATTERN = Pattern.compile("(?<!\\d)(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

	public MinecraftVersion {
		if (major < 0 || minor < 0 || patch < 0) {
			throw new IllegalArgumentException("Version components cannot be negative");
		}
	}

	public static Optional<MinecraftVersion> parse(String rawVersion) {
		if (rawVersion == null || rawVersion.isBlank()) {
			return Optional.empty();
		}
		Matcher matcher = VERSION_PATTERN.matcher(rawVersion);
		if (!matcher.find()) {
			return Optional.empty();
		}
		try {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(2));
			int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
			return Optional.of(new MinecraftVersion(major, minor, patch));
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
	}

	@Override
	public int compareTo(MinecraftVersion other) {
		int result = Integer.compare(major, other.major);
		if (result != 0) return result;
		result = Integer.compare(minor, other.minor);
		if (result != 0) return result;
		return Integer.compare(patch, other.patch);
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}
}
