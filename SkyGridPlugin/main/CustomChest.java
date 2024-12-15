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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class CustomChest {

	private static CustomChest instance;
	private static JavaPlugin plugin;
	private final Int2ObjectMap<ItemsData> chestItemsMap;
	private final Map<String, IntSet> chestBiomesMap;

	private CustomChest(JavaPlugin plugin) {
		CustomChest.plugin = plugin;
		this.chestItemsMap = new Int2ObjectOpenHashMap<>();
		this.chestBiomesMap = new HashMap<>();
		loadChestSettingsAsync();
	}

	public static CustomChest getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new CustomChest(plugin);
		}
		return instance;
	}

	public void loadChest(Block block) {
		if (block != null && block.getState() instanceof Chest chest) {
			Inventory chestInventory = chest.getInventory();
			Biome biome = block.getBiome();
			String biomeName = biome.toString().toUpperCase();
			IntSet chestKeys = chestBiomesMap.getOrDefault(biomeName, IntSet.of());
			if (!chestKeys.isEmpty()) {
				int chestKey = chestKeys.iterator().nextInt();
				ItemsData itemsData = chestItemsMap.get(chestKey);
				if (itemsData != null) {
					calculateItemsToPlace(itemsData, chestInventory)
					.thenAccept(itemsToPlace -> plugin.getServer().getScheduler().runTask(plugin,
							() -> placeItemsInChest(chestInventory, itemsToPlace)));
				} else {
					Cc.logS(Cc.GOLD, "No items data found for key: " + chestKey);
				}
			} else {
				Cc.logS(Cc.GOLD, "Biome-specific chest settings not found for biome: " + biomeName);
			}
		}
	}

	private CompletableFuture<ItemStack[]> calculateItemsToPlace(ItemsData itemsData, Inventory chestInventory) {
		return CompletableFuture.supplyAsync(() -> {
			ItemStack[] itemsToPlace = new ItemStack[chestInventory.getSize()];
			int[] currentItemCounts = getCurrentItemCounts(chestInventory);
			int[] emptySlots = getEmptySlots(chestInventory);
			for (int slot : emptySlots) {
				ItemSettings itemSettings = itemsData.getRandomItemSettings();
				if (itemSettings != null) {
					int materialOrdinal = itemSettings.materialOrdinal;
					int currentAmount = currentItemCounts[materialOrdinal];
					int maxAddable = itemSettings.maxAmount - currentAmount;
					if (maxAddable > 0) {
						int amount = ThreadLocalRandom.current().nextInt(1, maxAddable + 1);
						itemsToPlace[slot] = new ItemStack(Material.values()[materialOrdinal], amount);
						currentItemCounts[materialOrdinal] += amount;
					}
				}
			}
			return itemsToPlace;
		}, ExecutorServiceProvider.getExecutorService());
	}

	private void placeItemsInChest(Inventory chestInventory, ItemStack[] itemsToPlace) {
		ItemStack[] finalContents = chestInventory.getContents();
		for (int i = 0; i < itemsToPlace.length; i++) {
			if (itemsToPlace[i] != null) {
				finalContents[i] = itemsToPlace[i];
			}
		}
		chestInventory.setContents(finalContents);
	}

	private int[] getEmptySlots(Inventory inventory) {
		List<Integer> emptySlotList = new ArrayList<>();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) {
				emptySlotList.add(i);
			}
		}
		return emptySlotList.stream().mapToInt(Integer::intValue).toArray();
	}

	private int[] getCurrentItemCounts(Inventory inventory) {
		int[] currentItemCounts = new int[Material.values().length];
		for (ItemStack item : inventory.getContents()) {
			if (item != null) {
				currentItemCounts[item.getType().ordinal()] += item.getAmount();
			}
		}
		return currentItemCounts;
	}

	private void loadChestSettingsAsync() {
		CompletableFuture.runAsync(() -> {
			File chestSettingsFile = new File(plugin.getDataFolder(), "SkygridBlocks/ChestSettings.yml");
			if (!chestSettingsFile.exists()) {
				Cc.logS(Cc.RED, "ChestSettings.yml not found.");
				return;
			}
			FileConfiguration chestSettings = YamlConfiguration.loadConfiguration(chestSettingsFile);
			if (chestSettings.isConfigurationSection("ChestSettings")) {
				ConfigurationSection chestSettingsSection = chestSettings.getConfigurationSection("ChestSettings");
				for (String chestKeyString : chestSettingsSection.getKeys(false)) {
					int chestKey = chestKeyString.hashCode();
					Object2ReferenceMap<Integer, ItemSettings> itemsMap = new Object2ReferenceOpenHashMap<>();
					List<String> biomeNames = chestSettingsSection.getStringList(chestKeyString + ".Biomes");
					for (String biomeName : biomeNames) {
						chestBiomesMap.computeIfAbsent(biomeName.toUpperCase(), k -> new IntOpenHashSet()).add(chestKey);
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
									itemsMap.put(material.ordinal(), new ItemSettings(material.ordinal(), weight, maxAmount));
								} catch (NumberFormatException e) {
									Cc.logS(Cc.RED, "Invalid number format in item settings: " + itemSettingsString);
								}
							} else {
								Cc.logS(Cc.RED, "Invalid material name: " + parts[0]);
							}
						}
					}
					ItemsData itemsData = new ItemsData(itemsMap);
					chestItemsMap.put(chestKey, itemsData);
				}
				Cc.logSB("ChestSettings Loaded");
			} else {
				Cc.logS(Cc.RED, "Invalid ChestSettings.yml format.");
			}
		}, ExecutorServiceProvider.getExecutorService());
	}

	static class ItemSettings {
		int materialOrdinal;
		double weight;
		int maxAmount;
		ItemSettings(int materialOrdinal, double weight, int maxAmount) {
			this.materialOrdinal = materialOrdinal;
			this.weight = weight;
			this.maxAmount = maxAmount;
		}
	}

	static class ItemsData {
		private final ItemSettings[] itemSettingsArray;
		private final double[] cumulativeWeights;
		private final double totalWeight;
		ItemsData(Object2ReferenceMap<Integer, ItemSettings> itemsMap) {
			itemSettingsArray = itemsMap.values().toArray(new ItemSettings[0]);
			cumulativeWeights = new double[itemSettingsArray.length];
			double cumulative = 0.0;
			for (int i = 0; i < itemSettingsArray.length; i++) {
				cumulative += itemSettingsArray[i].weight;
				cumulativeWeights[i] = cumulative;
			}
			totalWeight = cumulative;
		}
		public ItemSettings getRandomItemSettings() {
			double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
			int index = Arrays.binarySearch(cumulativeWeights, randomValue);
			return index < 0 ? itemSettingsArray[-index - 1] : itemSettingsArray[index];
		}
	}
}
