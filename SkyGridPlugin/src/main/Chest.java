package main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Chest {

	private static Chest instance;
	private static JavaPlugin plugin;
	private FileConfiguration chestSettings;
	private Random random;
	private Map<String, List<ItemSettings>> itemSettingsMap;

	private Chest(JavaPlugin plugin) {
		Chest.plugin = plugin;
		this.random = new Random();
		this.itemSettingsMap = new HashMap<>();
		loadChestSettings();
	}

	public static Chest getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new Chest(plugin);
		}
		return instance;
	}

	public void loadChest(Block block) {
		if (block != null) {
			Inventory chestInventory = ((org.bukkit.block.Chest) block.getState()).getInventory();
			String biomeName = block.getBiome().name().toUpperCase();
			String worldName = block.getWorld().getName();

			if (chestSettings != null && chestSettings.isConfigurationSection("ChestSettings")) {
				ConfigurationSection chestSettingsSection = chestSettings.getConfigurationSection("ChestSettings");

				for (String chestKey : chestSettingsSection.getKeys(false)) {
					ConfigurationSection chestSection = chestSettingsSection.getConfigurationSection(chestKey);
					Set<String> biomes = new HashSet<>(chestSection.getStringList("Biomes"));

					if (biomes.contains(biomeName)) {
						List<ItemSettings> itemSettingsList = getItemSettingsList(chestKey);
						loadChestWithItems(itemSettingsList, chestInventory);
						return;
					}
				}
			}

			loadDefaultChestContents(chestInventory, worldName, biomeName);
		}
	}

	private void loadChestWithItems(List<ItemSettings> itemSettingsList, Inventory chestInventory) {
		Set<String> uniqueItems = new HashSet<>();
		int inventorySize = chestInventory.getSize();
		List<Integer> emptySlots = getEmptySlots(chestInventory);

		Collections.shuffle(emptySlots, random);

		for (int i = 0; i < inventorySize && !emptySlots.isEmpty(); i++) {
			ItemSettings itemSettings = chooseRandomItemSettingsWithWeights(itemSettingsList);

			if (itemSettings != null) {
				String itemName = itemSettings.itemName;

				if (uniqueItems.add(itemName)) {
					int amount = getRandomAmount(itemSettings);
					Material material = Material.matchMaterial(itemName);

					if (material != null) {
						if (amount > 2) {
							int splitAmount = getRandomSplitAmount(amount);
							distributeRandomly(chestInventory, material, splitAmount, emptySlots);
						} else {
							int slot = emptySlots.remove(0);
							chestInventory.setItem(slot, new ItemStack(material, amount));
						}
					} else {
						Bukkit.getLogger().warning("Invalid material name: " + itemName);
					}
				}
			}
		}
	}

	private List<Integer> getEmptySlots(Inventory inventory) {
		List<Integer> emptySlots = new ArrayList<>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) == null) {
				emptySlots.add(i);
			}
		}
		return emptySlots;
	}

	private int getRandomSplitAmount(int totalAmount) {
		return Math.max(1, totalAmount / 2 + random.nextInt(totalAmount / 2));
	}

	private void distributeRandomly(Inventory chestInventory, Material material, int totalAmount, List<Integer> emptySlots) {
		while (totalAmount > 0 && !emptySlots.isEmpty()) {
			int slot = emptySlots.remove(0);
			int amount = Math.min(totalAmount, random.nextInt(totalAmount) + 1);
			chestInventory.setItem(slot, new ItemStack(material, amount));
			totalAmount -= amount;
		}
	}

	private int getRandomAmount(ItemSettings itemSettings) {
		return random.nextInt(itemSettings.maxAmount + 1);
	}

	private boolean loadDefaultChestContents(Inventory chestInventory, String worldName, String biomeName) {
		List<Material> tier1Items = Arrays.asList(Material.WOODEN_AXE, Material.WOODEN_PICKAXE, Material.CROSSBOW, Material.NAME_TAG, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.GOLD_BLOCK, Material.GOLDEN_AXE, Material.IRON_BLOCK, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Material.IRON_SWORD, Material.GOLDEN_HELMET, Material.MUSIC_DISC_PIGSTEP, Material.ELYTRA, Material.DRAGON_HEAD, Material.DRAGON_EGG, Material.END_CRYSTAL, Material.ENDER_CHEST);
		List<Material> tier2Items = Arrays.asList(Material.APPLE, Material.OAK_LOG, Material.DARK_OAK_LOG, Material.RAIL, Material.DIAMOND, Material.IRON_INGOT, Material.GILDED_BLACKSTONE, Material.ANCIENT_DEBRIS, Material.BONE_BLOCK, Material.GOLDEN_CARROT, Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.SHULKER_SHELL);
		List<Material> tier3Items = Arrays.asList(Material.CHAIN, Material.GOLD_INGOT, Material.MAGMA_CREAM, Material.GOLD_NUGGET, Material.SPECTRAL_ARROW, Material.STRING, Material.IRON_NUGGET, Material.ARROW, Material.COOKED_PORKCHOP, Material.STICK, Material.WHEAT, Material.END_STONE, Material.END_ROD, Material.PURPUR_BLOCK, Material.MAGENTA_STAINED_GLASS);

		List<Material> items = getItemsForWorld(worldName);

		if (biomeName != null && !biomeName.isEmpty() && items.isEmpty()) {
			return false;
		}

		Collections.shuffle(items, random);

		for (Material item : items) {
			int baseAmount = random.nextInt(3) + 1;
			int num = random.nextInt(10) + 1;

			if (tier1Items.contains(item) && num <= 2) {
				baseAmount = 1;
			} else if (tier2Items.contains(item) && num <= 4) {
				baseAmount = random.nextInt(2) + 1;
			} else if (tier3Items.contains(item) && num <= 9) {
				baseAmount = random.nextInt(5) + 1;
			} else {
				baseAmount = 0;
			}

			int totalAmount = (baseAmount > 1) ? random.nextInt(baseAmount) + 1 : baseAmount;

			while (totalAmount > 0) {
				int amount = Math.min(totalAmount, random.nextInt(totalAmount) + 1);
				int slot = getRandomEmptySlot(chestInventory);
				if (slot != -1) {
					chestInventory.setItem(slot, new ItemStack(item, amount));
					totalAmount -= amount;
				}
			}
		}
		return true;
	}

	private int getRandomEmptySlot(Inventory inventory) {
		List<Integer> emptySlots = getEmptySlots(inventory);
		return emptySlots.isEmpty() ? -1 : emptySlots.get(random.nextInt(emptySlots.size()));
	}

	private List<Material> getItemsForWorld(String worldName) {
		switch (worldName) {
		case "world":
			return Arrays.asList(
					Material.WOODEN_AXE, Material.WOODEN_PICKAXE, Material.STICK, Material.APPLE,
					Material.OAK_LOG, Material.DARK_OAK_LOG, Material.WHEAT, Material.ARROW,
					Material.STRING, Material.CROSSBOW, Material.RAIL, Material.GOLD_INGOT,
					Material.NAME_TAG, Material.DIAMOND, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE
					);
		case "world_nether":
			return Arrays.asList(
					Material.CHAIN, Material.GOLD_INGOT, Material.MAGMA_CREAM, Material.GOLD_NUGGET,
					Material.SPECTRAL_ARROW, Material.IRON_INGOT, Material.STRING, Material.GILDED_BLACKSTONE,
					Material.IRON_SWORD, Material.GOLDEN_AXE, Material.IRON_NUGGET, Material.ANCIENT_DEBRIS,
					Material.BONE_BLOCK, Material.ARROW, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
					Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Material.GOLDEN_APPLE, Material.GOLDEN_HELMET,
					Material.GOLDEN_CARROT, Material.OBSIDIAN, Material.IRON_BLOCK, Material.CRYING_OBSIDIAN,
					Material.MUSIC_DISC_PIGSTEP, Material.GOLD_BLOCK, Material.COOKED_PORKCHOP
					);
		case "world_the_end":
			return Arrays.asList(
					Material.SHULKER_SHELL, Material.END_STONE, Material.ELYTRA, Material.DRAGON_HEAD,
					Material.DRAGON_EGG, Material.END_CRYSTAL, Material.END_ROD, Material.ENDER_CHEST,
					Material.OBSIDIAN, Material.PURPUR_BLOCK, Material.MAGENTA_STAINED_GLASS
					);
		default:
			return Collections.emptyList();
		}
	}

	private ItemSettings chooseRandomItemSettingsWithWeights(List<ItemSettings> itemSettingsList) {
		if (itemSettingsList.isEmpty()) {
			return null;
		}

		double totalWeight = itemSettingsList.stream().mapToDouble(item -> item.weight).sum();
		double randomValue = random.nextDouble() * totalWeight;

		for (ItemSettings itemSettings : itemSettingsList) {
			randomValue -= itemSettings.weight;
			if (randomValue <= 0.0) {
				return itemSettings;
			}
		}

		return null;
	}

	private List<ItemSettings> getItemSettingsList(String chestKey) {
		return itemSettingsMap.getOrDefault(chestKey, Collections.emptyList());
	}

	private void loadChestSettings() {
		File chestSettingsFile = new File(plugin.getDataFolder(), "SkygridBlocks/ChestSettings.yml");

		if (!chestSettingsFile.exists()) {
			createDefaultChestSettings(chestSettingsFile);
		}

		chestSettings = YamlConfiguration.loadConfiguration(chestSettingsFile);

		if (chestSettings != null && chestSettings.isConfigurationSection("ChestSettings")) {
			ConfigurationSection chestSettingsSection = chestSettings.getConfigurationSection("ChestSettings");

			for (String chestKey : chestSettingsSection.getKeys(false)) {
				List<ItemSettings> itemSettingsList = new ArrayList<>();
				List<String> itemSettingsStrings = chestSettingsSection.getStringList(chestKey + ".Items");

				for (String itemSettingsString : itemSettingsStrings) {
					String[] parts = itemSettingsString.split(":");
					if (parts.length == 3) {
						String itemName = parts[0];
						double weight = Double.parseDouble(parts[1]);
						int maxAmount = Integer.parseInt(parts[2]);

						itemSettingsList.add(new ItemSettings(itemName, weight, maxAmount));
					}
				}
				itemSettingsMap.put(chestKey, itemSettingsList);
			}
		}

		plugin.getLogger().info("ChestSettings Loaded");
	}

	private void createDefaultChestSettings(File chestSettingsFile) {
		InputStream inputStream = plugin.getResource("ChestSettings.yml");

		if (inputStream != null) {
			try (InputStreamReader reader = new InputStreamReader(inputStream)) {
				char[] buffer = new char[1024];
				StringBuilder builder = new StringBuilder();
				int bytesRead;
				while ((bytesRead = reader.read(buffer)) != -1) {
					builder.append(buffer, 0, bytesRead);
				}
				String yamlContent = builder.toString();
				Files.write(chestSettingsFile.toPath(), yamlContent.getBytes());

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			plugin.getLogger().warning("ChestSettings.yml not found in the JAR.");
		}
	}

	private static class ItemSettings {
		String itemName;
		double weight;
		int maxAmount;

		ItemSettings(String itemName, double weight, int maxAmount) {
			this.itemName = itemName;
			this.weight = weight;
			this.maxAmount = maxAmount;
		}
	}
}
