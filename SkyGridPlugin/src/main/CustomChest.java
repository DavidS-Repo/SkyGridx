package main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class CustomChest {

	private static CustomChest instance;
	private static JavaPlugin plugin;
	private final Map<ChestKey, Map<Material, ItemSettings>> chestItemsMap;
	private final Map<Biome, Set<ChestKey>> chestBiomesMap;
	private final Logger logger;

	private CustomChest(JavaPlugin plugin) {
		CustomChest.plugin = plugin;
		this.chestItemsMap = new HashMap<>();
		this.chestBiomesMap = new HashMap<>();
		this.logger = plugin.getLogger();
		loadChestSettings();
	}

	public static CustomChest getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new CustomChest(plugin);
		}
		return instance;
	}

	public void loadChest(Block block) {
		if (block != null && block.getState() instanceof Chest) {
			Chest chest = (Chest) block.getState();
			Inventory chestInventory = chest.getInventory();
			Biome biome = block.getBiome();

			Set<ChestKey> chestKeys = chestBiomesMap.get(biome);
			if (chestKeys != null) {
				for (ChestKey chestKey : chestKeys) {
					Map<Material, ItemSettings> itemsMap = chestItemsMap.get(chestKey);
					loadChestWithItems(itemsMap, chestInventory);
					return;
				}
			}
			logger.warning("Biome-specific chest settings not found for biome: " + biome.name());
		}
	}

	private void loadChestWithItems(Map<Material, ItemSettings> itemsMap, Inventory chestInventory) {
		Set<Integer> emptySlots = getEmptySlots(chestInventory);
		Map<Material, Integer> currentItemCounts = getCurrentItemCounts(chestInventory);

		for (Integer slot : emptySlots) {
			ItemSettings itemSettings = chooseRandomItemSettingsWithWeights(itemsMap);

			if (itemSettings != null) {
				Material material = itemSettings.material;

				int currentAmount = currentItemCounts.getOrDefault(material, 0);
				int amount = getRandomAmount(itemSettings, currentAmount);

				if (amount > 0) {
					chestInventory.setItem(slot, new ItemStack(material, amount));
					currentItemCounts.put(material, currentAmount + amount);
				}
			}
		}
	}

	private Set<Integer> getEmptySlots(Inventory inventory) {
		Set<Integer> emptySlots = new HashSet<>();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) {
				emptySlots.add(i);
			}
		}
		return emptySlots;
	}

	private Map<Material, Integer> getCurrentItemCounts(Inventory inventory) {
		Map<Material, Integer> currentItemCounts = new HashMap<>();
		for (ItemStack item : inventory.getContents()) {
			if (item != null) {
				currentItemCounts.merge(item.getType(), item.getAmount(), Integer::sum);
			}
		}
		return currentItemCounts;
	}

	private int getRandomAmount(ItemSettings itemSettings, int currentAmount) {
		int maxAddable = itemSettings.maxAmount - currentAmount;
		return maxAddable <= 0 ? 0 : ThreadLocalRandom.current().nextInt(1, maxAddable + 1);
	}

	private ItemSettings chooseRandomItemSettingsWithWeights(Map<Material, ItemSettings> itemsMap) {
		if (itemsMap.isEmpty()) {
			return null;
		}

		double totalWeight = itemsMap.values().stream().mapToDouble(item -> item.weight).sum();
		double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;

		for (ItemSettings itemSettings : itemsMap.values()) {
			randomValue -= itemSettings.weight;
			if (randomValue <= 0.0) {
				return itemSettings;
			}
		}

		return null;
	}

	private void loadChestSettings() {
		File chestSettingsFile = new File(plugin.getDataFolder(), "SkygridBlocks/ChestSettings.yml");

		if (!chestSettingsFile.exists()) {
			logger.warning("ChestSettings.yml not found.");
			return;
		}

		FileConfiguration chestSettings = YamlConfiguration.loadConfiguration(chestSettingsFile);

		if (chestSettings.isConfigurationSection("ChestSettings")) {
			ConfigurationSection chestSettingsSection = chestSettings.getConfigurationSection("ChestSettings");

			for (String chestKeyString : chestSettingsSection.getKeys(false)) {
				ChestKey chestKey = new ChestKey(chestKeyString);
				Map<Material, ItemSettings> itemsMap = new HashMap<>();
				List<String> biomeNames = chestSettingsSection.getStringList(chestKeyString + ".Biomes");

				for (String biomeName : biomeNames) {
					try {
						Biome biome = Biome.valueOf(biomeName);
						chestBiomesMap.computeIfAbsent(biome, k -> new HashSet<>()).add(chestKey);
					} catch (IllegalArgumentException e) {
						logger.warning("Invalid biome name: " + biomeName);
					}
				}

				List<String> itemSettingsStrings = chestSettingsSection.getStringList(chestKeyString + ".Items");
				for (String itemSettingsString : itemSettingsStrings) {
					String[] parts = itemSettingsString.split(":");
					if (parts.length == 3) {
						Material material = Material.matchMaterial(parts[0]);
						if (material != null) {
							try {
								double weight = Double.parseDouble(parts[1]);
								int maxAmount = Integer.parseInt(parts[2]);
								itemsMap.put(material, new ItemSettings(material, weight, maxAmount));
							} catch (NumberFormatException e) {
								logger.warning("Invalid number format in item settings: " + itemSettingsString);
							}
						} else {
							logger.warning("Invalid material name: " + parts[0]);
						}
					}
				}

				chestItemsMap.put(chestKey, itemsMap);
			}
		}

		logger.info("ChestSettings Loaded");
	}
}
