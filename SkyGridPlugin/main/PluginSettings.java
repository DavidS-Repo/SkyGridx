package main;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class PluginSettings {
	private final JavaPlugin plugin;
	private final CustomConfig customConfig;
	private static YamlConfiguration config;

	public PluginSettings(JavaPlugin plugin) {
		this.plugin = plugin;
		this.customConfig = new CustomConfig(plugin, "settings.yml");
		loadConfig();
	}

	private void loadConfig() {
		String configContent = customConfig.loadConfig();
		config = YamlConfiguration.loadConfiguration(new StringReader(configContent));
		loadDefaults();
	}

	private void loadDefaults() {
		// Generator Settings
		config.addDefault("generator.processDelay", 10);
		config.addDefault("generator.nether.minY", 0);
		config.addDefault("generator.nether.maxY", 128);
		config.addDefault("generator.end.minY", 0);
		config.addDefault("generator.end.maxY", 128);
		config.addDefault("generator.normal.minY", -64);
		config.addDefault("generator.normal.maxY", 64);
		config.addDefault("generator.default.minY", 0);
		config.addDefault("generator.default.maxY", 128);

		//TPR settings
		config.addDefault("tprCommand.onFirstJoin", false);
		config.addDefault("tprCommand.b2bDelay", 10);
		config.addDefault("tprCommand.tprDelay", 30);
		config.addDefault("tprCommand.tprNetherDelay", 30);
		config.addDefault("tprCommand.tprEndDelay", 30);
		config.addDefault("tprCommand.maxX", 29999983);
		config.addDefault("tprCommand.maxZ", 29999983);
		config.addDefault("tprCommand.minX", -29999983);
		config.addDefault("tprCommand.minZ", -29999983);
		config.addDefault("tprCommand.destinationY", 64);
		config.addDefault("tprCommand.DANGEROUSBLOCKS.Materials", List.of
				("AIR", "ALLIUM", "AZURE_BLUET", "BEETROOTS", "BIG_DRIPLEAF", "BLUE_ORCHID", "BROWN_MUSHROOM", "CACTUS", "CAMPFIRE", "CARROTS", 
						"CAVE_VINES_PLANT", "COBWEB", "CORNFLOWER", "CRIMSON_FUNGUS", "CRIMSON_ROOTS", "DANDELION", "DEAD_BUSH", "FERN", "FIRE", 
						"GLOW_LICHEN", "GRASS", "HANGING_ROOTS", "KELP", "KELP_PLANT", "LARGE_FERN", "LAVA", "LAVA_CAULDRON", "LILAC", "LILY_OF_THE_VALLEY", 
						"MAGMA_BLOCK", "MELON_STEM", "NETHER_SPROUTS", "NETHER_WART", "ORANGE_TULIP", "OXEYE_DAISY", "PEONY", "PINK_TULIP", "PITCHER_PLANT", 
						"POINTED_DRIPSTONE", "POPPY", "POTATOES", "POWDER_SNOW", "PUMPKIN_STEM", "RED_MUSHROOM", "RED_TULIP", "ROSE_BUSH", "SCULK_VEIN", 
						"SEAGRASS", "SMALL_DRIPLEAF", "SNOW", "SOUL_FIRE", "SPORE_BLOSSOM", "SUGAR_CANE", "SUNFLOWER", "SWEET_BERRY_BUSH", "TALL_GRASS", 
						"TALL_SEAGRASS", "TORCHFLOWER", "TORCHFLOWER_CROP", "TWISTING_VINES", "VINE", "VOID_AIR", "WARPED_FUNGUS", "WARPED_ROOTS", "WATER", 
						"WEEPING_VINES", "WHEAT", "WHITE_TULIP", "WITHER_ROSE"));

		//Fog setting
		config.addDefault("fog.autoEnable", false);

		//Event Control Settings
		config.addDefault("EventControl.GRAVITY_AFFECTED_BLOCKS.Materials", List.of
				("SAND", "RED_SAND", "GRAVEL", "KELP_PLANT", "ANVIL", "SUSPICIOUS_SAND", "SUSPICIOUS_GRAVEL", "DRAGON_EGG", 
						"BLACK_CONCRETE_POWDER", "BLUE_CONCRETE_POWDER", "BROWN_CONCRETE_POWDER", "CYAN_CONCRETE_POWDER", "GRAY_CONCRETE_POWDER", 
						"GREEN_CONCRETE_POWDER", "LIGHT_BLUE_CONCRETE_POWDER", "LIGHT_GRAY_CONCRETE_POWDER", "LIME_CONCRETE_POWDER", "MAGENTA_CONCRETE_POWDER", 
						"ORANGE_CONCRETE_POWDER", "PINK_CONCRETE_POWDER", "PURPLE_CONCRETE_POWDER", "RED_CONCRETE_POWDER", "WHITE_CONCRETE_POWDER", "YELLOW_CONCRETE_POWDER"));

		//Fog setting
		config.addDefault("EventControl.BlockIgniteEvent", true);
		config.addDefault("EventControl.BlockFadeEvent", true);
		config.addDefault("EventControl.BlockFromToEvent", true);
		config.addDefault("EventControl.StructureGrowEvent", true);
		config.addDefault("EventControl.BlockSpreadEvent", true);
		config.addDefault("EventControl.BlockGrowEvent", true);
		config.addDefault("EventControl.BlockFormEvent", true);
		config.addDefault("EventControl.EntityChangeBlockEvent", true);

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

	private static EnumSet<Material> loadMaterialSet(String path) {
		List<String> materialNames = config.getStringList(path);
		return materialNames.stream()
				.map(Material::valueOf)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
	}
	
	public final static int THREADS() {
		return Runtime.getRuntime().availableProcessors();
	}

	public static EnumSet<Material> getDangerousBlocks() {
		return loadMaterialSet("tprCommand.DANGEROUSBLOCKS.Materials");
	}

	public static int getProcessDelay() {
		return config.getInt("generator.processDelay");
	}

	public static int netherMinY() {
		return config.getInt("generator.nether.minY");
	}

	public static int netherMaxY() {
		return config.getInt("generator.nether.maxY");
	}

	public static int endMaxY() {
		return config.getInt("generator.end.maxY");
	}

	public static int endMinY() {
		return config.getInt("generator.end.minY");
	}

	public static int normalMaxY() {
		return config.getInt("generator.normal.maxY");
	}

	public static int normalMinY() {
		return config.getInt("generator.normal.minY");
	}

	public static int defaultMaxY() {
		return config.getInt("generator.default.maxY");
	}

	public static int defaultMinY() {
		return config.getInt("generator.default.minY");
	}

	public static boolean isOnFistJoinEnabled() {
		return config.getBoolean("tprCommand.onFistJoin");
	}

	public static int getb2bDelay() {
		return config.getInt("tprCommand.b2bDelay");
	}

	public static int getTprDelay() {
		return config.getInt("tprCommand.tprDelay");
	}

	public static int getTprNetherDelay() {
		return config.getInt("tprCommand.tprNetherDelay");
	}

	public static int getTprEndDelay() {
		return config.getInt("tprCommand.tprEndDelay");
	}

	public static int getMaxX() {
		return config.getInt("tprCommand.maxX");
	}

	public static int getMaxZ() {
		return config.getInt("tprCommand.maxZ");
	}

	public static int getMinX() {
		return config.getInt("tprCommand.minX");
	}

	public static int getMinZ() {
		return config.getInt("tprCommand.minZ");
	}

	public static int getDestinationY() {
		return config.getInt("tprCommand.destinationY");
	}

	public static boolean isFogAutoEnabled() {
		return config.getBoolean("fog.autoEnable");
	}

	public static EnumSet<Material> getGravityAffectedBlocks() {
		return loadMaterialSet("EventControl.GRAVITY_AFFECTED_BLOCKS.Materials");
	}

	public static boolean isBlockIgniteEvent() {
		return config.getBoolean("EventControl.BlockIgniteEvent");
	}
	
	public static boolean isBlockFadeEvent() {
		return config.getBoolean("EventControl.BlockFadeEvent");
	}
	
	public static boolean isBlockFromToEvent() {
		return config.getBoolean("EventControl.BlockFromToEvent");
	}
	
	public static boolean isStructureGrowEvent() {
		return config.getBoolean("EventControl.StructureGrowEvent");
	}
	
	public static boolean isBlockSpreadEvent() {
		return config.getBoolean("EventControl.BlockSpreadEvent");
	}
	
	public static boolean isBlockGrowEvent() {
		return config.getBoolean("EventControl.BlockGrowEvent");
	}
	
	public static boolean isBlockFormEvent() {
		return config.getBoolean("EventControl.BlockFormEvent");
	}
	
	public static boolean isEntityChangeBlockEvent() {
		return config.getBoolean("EventControl.EntityChangeBlockEvent");
	}
}