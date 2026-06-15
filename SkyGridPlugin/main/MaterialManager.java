package main;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class MaterialManager {
	private static final BlockFace[] FIXED_FACE_ORDER = {
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.EAST,
			BlockFace.WEST,
			BlockFace.NORTH,
			BlockFace.SOUTH
	};
	private static final EnumSet<BlockFace> END_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
	private static final EnumSet<BlockFace> SIDE_FACES = EnumSet.of(
			BlockFace.EAST,
			BlockFace.WEST,
			BlockFace.NORTH,
			BlockFace.SOUTH
			);
	private static final FaceAttachment[] NO_ATTACHMENTS = new FaceAttachment[0];

	private final SkyGridPlugin plugin;
	private final Object2ObjectMap<String, MaterialDistribution> materialDistributions;

	public MaterialManager(SkyGridPlugin plugin) {
		this.plugin = plugin;
		this.materialDistributions = new Object2ObjectOpenHashMap<>();
	}

	/**
	 * Reloads all distributions from disk, replacing any existing cached mappings.
	 */
	public synchronized void reloadAll() {
		materialDistributions.clear();
		loadMaterialsForWorld("world.yml");
		loadMaterialsForWorld("world_nether.yml");
		loadMaterialsForWorld("world_the_end.yml");
	}

	public void loadMaterialsForWorld(String fileName) {
		File file = new File(plugin.getDataFolder(), "SkygridBlocks/" + fileName);
		if (!file.exists()) {
			plugin.getLogger().warning("File not found: " + file.getPath());
			return;
		}

		FileConfiguration config = loadSkyGridBlockConfig(file);
		String worldName = getWorldNameFromFileName(fileName);
		boolean hasDefaultDistribution = config.contains("default_distribution");

		ConfigurationSection distributionsSection = config.getConfigurationSection("distributions");
		Object2ObjectOpenHashMap<String, MaterialDistribution> distributionCache = new Object2ObjectOpenHashMap<>();

		if (distributionsSection == null) {
			if (!hasDefaultDistribution) {
				plugin.getLogger().warning("'distributions' section not found in " + fileName);
			}
		} else {
			for (String key : distributionsSection.getKeys(false)) {
				ConfigurationSection distSection = distributionsSection.getConfigurationSection(key);
				if (distSection == null) {
					plugin.getLogger().warning("No section found for distribution key: " + key + " in file " + fileName);
					continue;
				}

				MaterialDistribution distribution = loadDistribution(distSection, "distribution '" + key + "'", fileName);
				if (distribution == null) {
					plugin.getLogger().warning("Empty distribution '" + key + "' in file " + fileName);
					continue;
				}

				distributionCache.put(key, distribution);
			}
		}

		ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
		if (biomesSection == null) {
			if (!hasDefaultDistribution) {
				plugin.getLogger().warning("'biomes' section not found in " + fileName);
			}
		} else {
			for (String biomeKey : biomesSection.getKeys(false)) {
				String distributionKey = biomesSection.getString(biomeKey + ".distribution");
				if (distributionKey == null) {
					plugin.getLogger().warning("No distribution defined for biome '" + biomeKey + "' in file " + fileName);
					continue;
				}
				distributionKey = distributionKey.trim();

				MaterialDistribution distribution = distributionCache.get(distributionKey);
				if (distribution == null) {
					plugin.getLogger().warning("Distribution '" + distributionKey + "' not found for biome '" + biomeKey + "' in file " + fileName);
					continue;
				}

				String[] biomes = biomeKey.split(",");
				for (String biome : biomes) {
					String trimmedBiome = BiomeKeyUtil.normalize(biome);
					if (trimmedBiome.isEmpty()) {
						plugin.getLogger().warning("Empty biome name in key: " + biomeKey + " in file " + fileName);
						continue;
					}
					String alias = worldName + "-" + trimmedBiome;
					setMaterialDistribution(alias, distribution);
				}
			}
		}

		if (config.contains("default_distribution")) {
			ConfigurationSection defaultDistSection = config.getConfigurationSection("default_distribution");
			if (defaultDistSection != null) {
				MaterialDistribution defaultDistribution = loadDistribution(defaultDistSection, "default_distribution", fileName);
				if (defaultDistribution != null) {
					setMaterialDistribution(worldName + "-DEFAULT", defaultDistribution);
				} else {
					plugin.getLogger().warning("Default distribution is empty in file " + fileName);
				}
			}
		}
	}

	private FileConfiguration loadSkyGridBlockConfig(File file) {
		try {
			String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			String normalized = normalizeDistributionShorthand(content);
			return YamlConfiguration.loadConfiguration(new StringReader(normalized));
		} catch (IOException e) {
			plugin.getLogger().warning("Could not read " + file.getPath() + ": " + e.getMessage());
			return YamlConfiguration.loadConfiguration(file);
		}
	}

	private String normalizeDistributionShorthand(String content) {
		String[] lines = content.split("\\R", -1);
		StringBuilder normalized = new StringBuilder(content.length());

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			ParentChanceLine parentChance = parseParentChanceLine(line);
			if (parentChance != null && hasAttachmentChildren(lines, i, parentChance.indentLength())) {
				normalized.append(parentChance.indent()).append(parentChance.materialKey()).append(":").append('\n');
				normalized.append(parentChance.indent()).append("  chance: ").append(parentChance.chance());
				if (!parentChance.trailingComment().isEmpty()) {
					normalized.append(parentChance.trailingComment());
				}
			} else {
				normalized.append(line);
			}

			if (i < lines.length - 1) {
				normalized.append('\n');
			}
		}

		return normalized.toString();
	}

	private ParentChanceLine parseParentChanceLine(String line) {
		int firstNonWhitespace = firstNonWhitespace(line);
		if (firstNonWhitespace < 0) {
			return null;
		}

		int colon = line.indexOf(':', firstNonWhitespace);
		if (colon < 0) {
			return null;
		}

		String materialKey = line.substring(firstNonWhitespace, colon).trim();
		if (materialKey.isEmpty() || Material.getMaterial(materialKey.toUpperCase(Locale.ROOT)) == null) {
			return null;
		}

		String valueAndComment = line.substring(colon + 1);
		int commentStart = valueAndComment.indexOf('#');
		String value = commentStart >= 0 ? valueAndComment.substring(0, commentStart).trim() : valueAndComment.trim();
		if (!isPositiveNumberText(value)) {
			return null;
		}

		String trailingComment = commentStart >= 0 ? valueAndComment.substring(commentStart) : "";
		if (!trailingComment.isEmpty()) {
			trailingComment = " " + trailingComment.trim();
		}

		return new ParentChanceLine(line.substring(0, firstNonWhitespace), materialKey, value, trailingComment);
	}

	private boolean hasAttachmentChildren(String[] lines, int parentIndex, int parentIndent) {
		for (int i = parentIndex + 1; i < lines.length; i++) {
			String line = lines[i];
			int firstNonWhitespace = firstNonWhitespace(line);
			if (firstNonWhitespace < 0 || line.substring(firstNonWhitespace).startsWith("#")) {
				continue;
			}
			if (firstNonWhitespace <= parentIndent) {
				return false;
			}

			String childKey = readMappingKey(line.substring(firstNonWhitespace));
			return childKey != null && isAttachmentKey(childKey.trim().toLowerCase(Locale.ROOT));
		}
		return false;
	}

	private int firstNonWhitespace(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (!Character.isWhitespace(line.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	private String readMappingKey(String text) {
		int colon = text.indexOf(':');
		if (colon <= 0) {
			return null;
		}
		return text.substring(0, colon);
	}

	private boolean isPositiveNumberText(String text) {
		try {
			return Double.parseDouble(text) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean isAttachmentKey(String key) {
		return faceForKey(key) != null || isRandomEndsKey(key) || isRandomSidesKey(key);
	}

	private String getWorldNameFromFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private MaterialDistribution loadDistribution(ConfigurationSection section, String context, String fileName) {
		Object2DoubleMap<ParentMaterial> distributionMap = new Object2DoubleOpenHashMap<>();
		for (String materialKey : section.getKeys(false)) {
			Material material = parseMaterial(materialKey, context, fileName);
			if (material == null) {
				continue;
			}

			ParentMaterial parentMaterial;
			double chance;
			ConfigurationSection parentSection = section.getConfigurationSection(materialKey);
			if (parentSection == null) {
				chance = section.getDouble(materialKey);
				parentMaterial = new ParentMaterial(material);
			} else {
				chance = readParentChance(parentSection, materialKey, context, fileName);
				parentMaterial = loadParentMaterial(material, parentSection, materialKey, context, fileName);
			}

			if (chance > 0) {
				distributionMap.put(parentMaterial, chance);
			} else {
				plugin.getLogger().warning("Invalid chance for material '" + materialKey + "' in " + context + " of file " + fileName);
			}
		}
		return distributionMap.isEmpty() ? null : new MaterialDistribution(distributionMap, true);
	}

	private ParentMaterial loadParentMaterial(Material material, ConfigurationSection section, String materialKey, String context, String fileName) {
		ParentMaterial parentMaterial = new ParentMaterial(material);

		for (String optionKey : section.getKeys(false)) {
			String normalized = optionKey.trim().toLowerCase(Locale.ROOT);
			if (isChanceKey(normalized)) {
				continue;
			}

			ConfigurationSection optionSection = section.getConfigurationSection(optionKey);
			if (optionSection == null) {
				plugin.getLogger().warning("Expected a material list under '" + materialKey + "." + optionKey + "' in " + context + " of file " + fileName);
				continue;
			}

			if (isRandomEndsKey(normalized)) {
				parentMaterial.randomEnds = loadMaterialPicker(optionSection, materialKey + "." + optionKey, context, fileName);
				continue;
			}
			if (isRandomSidesKey(normalized)) {
				parentMaterial.randomSides = loadMaterialPicker(optionSection, materialKey + "." + optionKey, context, fileName);
				continue;
			}

			BlockFace face = faceForKey(normalized);
			if (face != null) {
				WeightedPicker<Material> picker = loadMaterialPicker(optionSection, materialKey + "." + optionKey, context, fileName);
				if (picker != null) {
					parentMaterial.faceMaterials.put(face, picker);
				}
				continue;
			}

			plugin.getLogger().warning("Unknown grid attachment key '" + optionKey + "' under material '" + materialKey + "' in " + context + " of file " + fileName);
		}

		return parentMaterial;
	}

	private WeightedPicker<Material> loadMaterialPicker(ConfigurationSection section, String sectionPath, String context, String fileName) {
		Object2DoubleMap<Material> materials = new Object2DoubleOpenHashMap<>();
		for (String materialKey : section.getKeys(false)) {
			Material material = parseMaterial(materialKey, sectionPath + " in " + context, fileName);
			double chance = section.getDouble(materialKey);
			if (material != null && chance > 0) {
				materials.put(material, chance);
			} else {
				plugin.getLogger().warning("Invalid attached material '" + materialKey + "' in " + sectionPath + " of file " + fileName);
			}
		}
		return materials.isEmpty() ? null : new WeightedPicker<>(materials);
	}

	private Material parseMaterial(String materialKey, String context, String fileName) {
		Material material = Material.getMaterial(materialKey.trim().toUpperCase(Locale.ROOT));
		if (material == null) {
			plugin.getLogger().warning("Invalid material '" + materialKey + "' in " + context + " of file " + fileName);
		}
		return material;
	}

	private double readParentChance(ConfigurationSection section, String materialKey, String context, String fileName) {
		for (String optionKey : section.getKeys(false)) {
			if (isChanceKey(optionKey.trim().toLowerCase(Locale.ROOT))) {
				return section.getDouble(optionKey);
			}
		}
		plugin.getLogger().warning("Expanded material '" + materialKey + "' in " + context + " of file " + fileName + " must define chance: <number>");
		return 0;
	}

	private boolean isChanceKey(String key) {
		return key.equals("chance") || key.equals("weight") || key.equals("percentage");
	}

	private boolean isRandomEndsKey(String key) {
		return key.equals("rends") || key.equals("randomends") || key.equals("random_ends") || key.equals("random-ends");
	}

	private boolean isRandomSidesKey(String key) {
		return key.equals("rsides") || key.equals("randomsides") || key.equals("random_sides") || key.equals("random-sides");
	}

	private BlockFace faceForKey(String key) {
		return switch (key) {
		case "top", "up" -> BlockFace.UP;
		case "bottom", "down" -> BlockFace.DOWN;
		case "east" -> BlockFace.EAST;
		case "west" -> BlockFace.WEST;
		case "north" -> BlockFace.NORTH;
		case "south" -> BlockFace.SOUTH;
		default -> null;
		};
	}

	private void setMaterialDistribution(String alias, MaterialDistribution materialDistribution) {
		materialDistributions.put(alias, materialDistribution);
	}

	/**
	 * Looks up a distribution in this order:
	 * 1) exact worldName-biome
	 * 2) baseWorldName-biome (without "skygridx_" prefix)
	 * 3) exact worldName-DEFAULT
	 * 4) baseWorldName-DEFAULT
	 */
	public GridBlock getRandomGridBlockForWorld(String worldName, String biomeName) {
		biomeName = BiomeKeyUtil.normalize(biomeName);

		String key = worldName + "-" + biomeName;
		MaterialDistribution dist = materialDistributions.get(key);
		if (dist != null) {
			return dist.next();
		}

		String base = WorldManager.getBaseWorldName(worldName);

		String baseKey = base + "-" + biomeName;
		dist = materialDistributions.get(baseKey);
		if (dist != null) {
			return dist.next();
		}

		String defaultKey = worldName + "-DEFAULT";
		dist = materialDistributions.get(defaultKey);
		if (dist != null) {
			return dist.next();
		}

		String baseDefaultKey = base + "-DEFAULT";
		dist = materialDistributions.get(baseDefaultKey);
		if (dist != null) {
			return dist.next();
		}

		plugin.getLogger().warning("No material distribution found for alias: " + key + " and no default distribution available.");
		return null;
	}

	public Material getRandomMaterialForWorld(String worldName, String biomeName) {
		GridBlock gridBlock = getRandomGridBlockForWorld(worldName, biomeName);
		return gridBlock == null ? null : gridBlock.material();
	}

	public record GridBlock(Material material, FaceAttachment[] attachments) {}

	public record FaceAttachment(BlockFace face, Material material) {}

	private record ParentChanceLine(String indent, String materialKey, String chance, String trailingComment) {
		private int indentLength() {
			return indent.length();
		}
	}

	private static final class ParentMaterial {
		private final Material material;
		private final EnumMap<BlockFace, WeightedPicker<Material>> faceMaterials = new EnumMap<>(BlockFace.class);
		private WeightedPicker<Material> randomEnds;
		private WeightedPicker<Material> randomSides;

		private ParentMaterial(Material material) {
			this.material = material;
		}

		private GridBlock nextGridBlock() {
			if (faceMaterials.isEmpty() && randomEnds == null && randomSides == null) {
				return new GridBlock(material, NO_ATTACHMENTS);
			}

			List<FaceAttachment> attachments = new ArrayList<>(6);
			EnumSet<BlockFace> occupiedFaces = EnumSet.noneOf(BlockFace.class);

			for (BlockFace face : FIXED_FACE_ORDER) {
				WeightedPicker<Material> picker = faceMaterials.get(face);
				if (picker == null) {
					continue;
				}
				Material attachment = picker.next();
				attachments.add(new FaceAttachment(face, attachment));
				occupiedFaces.add(face);
			}

			addRandomAttachment(attachments, occupiedFaces, END_FACES, randomEnds);
			addRandomAttachment(attachments, occupiedFaces, SIDE_FACES, randomSides);

			return new GridBlock(material, attachments.toArray(new FaceAttachment[0]));
		}

		private static void addRandomAttachment(
				List<FaceAttachment> attachments,
				EnumSet<BlockFace> occupiedFaces,
				EnumSet<BlockFace> candidateFaces,
				WeightedPicker<Material> picker
				) {
			if (picker == null) {
				return;
			}
			BlockFace face = randomAvailableFace(occupiedFaces, candidateFaces);
			if (face == null) {
				return;
			}
			attachments.add(new FaceAttachment(face, picker.next()));
			occupiedFaces.add(face);
		}

		private static BlockFace randomAvailableFace(EnumSet<BlockFace> occupiedFaces, EnumSet<BlockFace> candidateFaces) {
			int available = 0;
			for (BlockFace face : candidateFaces) {
				if (!occupiedFaces.contains(face)) {
					available++;
				}
			}
			if (available == 0) {
				return null;
			}

			int pick = ThreadLocalRandom.current().nextInt(available);
			for (BlockFace face : candidateFaces) {
				if (occupiedFaces.contains(face)) {
					continue;
				}
				if (pick-- == 0) {
					return face;
				}
			}
			return null;
		}
	}

	static class MaterialDistribution {
		private final WeightedPicker<ParentMaterial> picker;

		public MaterialDistribution(Object2DoubleMap<Material> distribution) {
			this(toParentMaterials(distribution), true);
		}

		private MaterialDistribution(Object2DoubleMap<ParentMaterial> distribution, boolean ignored) {
			this.picker = new WeightedPicker<>(distribution);
		}

		private static Object2DoubleMap<ParentMaterial> toParentMaterials(Object2DoubleMap<Material> distribution) {
			Object2DoubleMap<ParentMaterial> parentDistribution = new Object2DoubleOpenHashMap<>();
			for (Object2DoubleMap.Entry<Material> entry : distribution.object2DoubleEntrySet()) {
				parentDistribution.put(new ParentMaterial(entry.getKey()), entry.getDoubleValue());
			}
			return parentDistribution;
		}

		public GridBlock next() {
			return picker.next().nextGridBlock();
		}

		public Material nextMaterial() {
			return picker.next().material;
		}
	}

	private static final class WeightedPicker<T> {
		private final Object[] values;
		private final double[] probabilities;
		private final int[] alias;
		private final int size;

		private WeightedPicker(Object2DoubleMap<T> distribution) {
			this.size = distribution.size();
			this.values = new Object[size];
			this.probabilities = new double[size];
			this.alias = new int[size];

			double total = 0.0;
			int index = 0;

			Object[] tempValues = new Object[size];
			double[] normalized = new double[size];

			for (Object2DoubleMap.Entry<T> entry : distribution.object2DoubleEntrySet()) {
				total += entry.getDoubleValue();
			}

			for (Object2DoubleMap.Entry<T> entry : distribution.object2DoubleEntrySet()) {
				tempValues[index] = entry.getKey();
				normalized[index] = (entry.getDoubleValue() / total) * size;
				index++;
			}

			IntArrayList small = new IntArrayList();
			IntArrayList large = new IntArrayList();
			for (int i = 0; i < size; i++) {
				if (normalized[i] < 1.0) small.add(i);
				else large.add(i);
			}

			while (!small.isEmpty() && !large.isEmpty()) {
				int smallIndex = small.removeInt(small.size() - 1);
				int largeIndex = large.removeInt(large.size() - 1);
				probabilities[smallIndex] = normalized[smallIndex];
				alias[smallIndex] = largeIndex;
				normalized[largeIndex] = (normalized[largeIndex] + normalized[smallIndex]) - 1.0;
				if (normalized[largeIndex] < 1.0) small.add(largeIndex);
				else large.add(largeIndex);
			}

			while (!small.isEmpty()) {
				probabilities[small.removeInt(small.size() - 1)] = 1.0;
			}
			while (!large.isEmpty()) {
				probabilities[large.removeInt(large.size() - 1)] = 1.0;
			}

			System.arraycopy(tempValues, 0, values, 0, size);
		}

		@SuppressWarnings("unchecked")
		private T next() {
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			int column = rng.nextInt(size);
			return rng.nextDouble() < probabilities[column]
					? (T) values[column]
							: (T) values[alias[column]];
		}
	}
}
