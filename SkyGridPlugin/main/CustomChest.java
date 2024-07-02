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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CustomChest {

	private static CustomChest instance;
	private static JavaPlugin plugin;
	private final Int2ObjectMap<Object2ReferenceMap<Integer, ItemSettings>> chestItemsMap;
	private final Int2ObjectMap<IntSet> chestBiomesMap;
	private final Logger logger;
	private final ExecutorService executorService;

	private CustomChest(JavaPlugin plugin) {
		CustomChest.plugin = plugin;
		this.chestItemsMap = new Int2ObjectOpenHashMap<>();
		this.chestBiomesMap = new Int2ObjectOpenHashMap<>();
		this.logger = plugin.getLogger();
		this.executorService = Executors.newCachedThreadPool();;
		loadChestSettingsAsync();
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

			CompletableFuture.runAsync(() -> {
				IntSet chestKeys = chestBiomesMap.get(biome.ordinal());
				if (chestKeys != null) {
					for (int chestKey : chestKeys) {
						Object2ReferenceMap<Integer, ItemSettings> itemsMap = chestItemsMap.get(chestKey);
						calculateItemsToPlace(itemsMap, chestInventory)
						.thenAccept(itemsToPlace -> plugin.getServer().getScheduler().runTask(plugin,
								() -> placeItemsInChest(chestInventory, itemsToPlace)));
						return;
					}
				} else {
					logger.warning("Biome-specific chest settings not found for biome: " + biome.name());
				}
			}, executorService);
		}
	}

	private CompletableFuture<ItemStack[]> calculateItemsToPlace(Object2ReferenceMap<Integer, ItemSettings> itemsMap, Inventory chestInventory) {
		ItemStack[] itemsToPlace = new ItemStack[chestInventory.getSize()];
		Object2IntMap<Integer> currentItemCounts = getCurrentItemCounts(chestInventory);
		IntSet emptySlots = getEmptySlots(chestInventory);

		List<CompletableFuture<Void>> futures = emptySlots.intStream()
				.mapToObj(slot -> CompletableFuture.runAsync(() -> {
					ItemSettings itemSettings = chooseRandomItemSettingsWithWeights(itemsMap);
					if (itemSettings != null) {
						int materialOrdinal = itemSettings.materialOrdinal;
						int currentAmount = currentItemCounts.getOrDefault((Integer) materialOrdinal, 0);
						int maxAddable = itemSettings.maxAmount - currentAmount;
						if (maxAddable > 0) {
							int amount = ThreadLocalRandom.current().nextInt(1, maxAddable + 1);
							itemsToPlace[slot] = new ItemStack(Material.values()[materialOrdinal], amount);
							currentItemCounts.mergeInt(materialOrdinal, amount, Integer::sum);
						}
					}
				}, executorService))
				.collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> itemsToPlace);
	}

	private void placeItemsInChest(Inventory chestInventory, ItemStack[] itemsToPlace) {
		ItemStack[] finalContents = chestInventory.getContents();
		for (int i = 0; i < itemsToPlace.length; i++) {
			ItemStack itemStack = itemsToPlace[i];
			if (itemStack != null) {
				finalContents[i] = itemStack;
			}
		}
		chestInventory.setContents(finalContents);
	}

	private IntOpenHashSet getEmptySlots(Inventory inventory) {
		IntOpenHashSet emptySlots = new IntOpenHashSet();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) {
				emptySlots.add(i);
			}
		}
		return emptySlots;
	}

	private Object2IntMap<Integer> getCurrentItemCounts(Inventory inventory) {
		Object2IntMap<Integer> currentItemCounts = new Object2IntOpenHashMap<>();
		for (ItemStack item : inventory.getContents()) {
			if (item != null) {
				currentItemCounts.mergeInt(item.getType().ordinal(), item.getAmount(), Integer::sum);
			}
		}
		return currentItemCounts;
	}

	private ItemSettings chooseRandomItemSettingsWithWeights(Object2ReferenceMap<Integer, ItemSettings> itemsMap) {
		if (itemsMap.isEmpty()) {
			return null;
		}
		double totalWeight = itemsMap.values().parallelStream().mapToDouble(item -> item.weight).sum();
		double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
		for (ItemSettings itemSettings : itemsMap.values()) {
			randomValue -= itemSettings.weight;
			if (randomValue <= 0.0) {
				return itemSettings;
			}
		}
		return null;
	}

	private void loadChestSettingsAsync() {
		CompletableFuture.runAsync(() -> {
			File chestSettingsFile = new File(plugin.getDataFolder(), "SkygridBlocks/ChestSettings.yml");
			if (!chestSettingsFile.exists()) {
				logger.warning("ChestSettings.yml not found.");
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
						try {
							Biome biome = Biome.valueOf(biomeName);
							chestBiomesMap.computeIfAbsent(biome.ordinal(), k -> new IntOpenHashSet()).add(chestKey);
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
									itemsMap.put(material.ordinal(), new ItemSettings(material.ordinal(), weight, maxAmount));
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
		}, executorService);
	}

	static class ChestKey {
		private final int key;

		ChestKey(int key) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ChestKey chestKey = (ChestKey) o;
			return key == chestKey.key;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key);
		}

		@Override
		public String toString() {
			return Integer.toString(key);
		}
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
}