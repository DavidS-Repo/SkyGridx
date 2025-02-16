package main;

import main.EnchantmentNameSupport.ChosenEnchantment;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
	private static int maxPerSlot;
	private static final Material[] MATERIALS = Material.values();
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

			IntSet chestKeys = chestBiomesMap.getOrDefault(biomeName, new IntOpenHashSet());
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

	@SuppressWarnings("deprecation")
	private CompletableFuture<ItemStack[]> calculateItemsToPlace(ItemsData itemsData, Inventory chestInventory) {
		return CompletableFuture.supplyAsync(() -> {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			ItemStack[] itemsToPlace = new ItemStack[chestInventory.getSize()];
			int[] currentItemCounts = getCurrentItemCounts(chestInventory);
			int[] emptySlots = getEmptySlots(chestInventory);
			for (int slot : emptySlots) {
				ItemSettings itemSettings = itemsData.getRandomItemSettings();
				if (itemSettings != null) {
					int matOrdinal = itemSettings.materialOrdinal;
					int currentAmount = currentItemCounts[matOrdinal];
					int maxAddable = itemSettings.maxAmount - currentAmount;
					if (maxAddable > 0) {
						int maxSlotAdd = Math.min(maxAddable, maxPerSlot);
						int amount = random.nextInt(1, maxSlotAdd + 1);
						ItemStack stack = new ItemStack(MATERIALS[matOrdinal], amount);
						ItemMeta meta = stack.getItemMeta();
						if (itemSettings.customName != null && !itemSettings.customName.isEmpty()) {
							meta.setDisplayName(itemSettings.customName);
						}
						stack.setItemMeta(meta);
						List<ChosenEnchantment> chosenEnchants = new ArrayList<>();
						if (itemSettings.enchantments != null && !itemSettings.enchantments.isEmpty()) {
							String rawLevelType = (itemSettings.levelType != null) ? itemSettings.levelType.trim() : "";
							String itemLevelType = rawLevelType.equalsIgnoreCase("standard") ? "Standard" : "Roman";
							for (EnchantmentSetting enchSetting : itemSettings.enchantments) {
								if (random.nextDouble(100.0) < enchSetting.weight) {
									int level = random.nextInt(enchSetting.minLevel, enchSetting.maxLevel + 1);
									if (level > 0) {
										Enchantment ench = Enchantment.getByName(enchSetting.enchantmentName.toUpperCase());
										if (ench != null) {
											String rawLoreColor = (enchSetting.loreColor != null) ? enchSetting.loreColor.trim() : "";
											String loreColor = rawLoreColor.isEmpty() ? "GRAY" : rawLoreColor;
											chosenEnchants.add(new ChosenEnchantment(ench, level, itemLevelType, loreColor));
										}
									}
								}
							}
						}
						EnchantmentNameSupport.applyCustomEnchantDisplay(stack, chosenEnchants);
						itemsToPlace[slot] = stack;
						currentItemCounts[matOrdinal] += amount;
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
		int[] currentItemCounts = new int[MATERIALS.length];
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
			if (chestSettings.contains("MaxItemsPerSlot")) {
				maxPerSlot = chestSettings.getInt("MaxItemsPerSlot", 2);
			}
			if (chestSettings.isConfigurationSection("ChestSettings")) {
				ConfigurationSection chestSettingsSection = chestSettings.getConfigurationSection("ChestSettings");
				for (String chestKeyString : chestSettingsSection.getKeys(false)) {
					int chestKey = chestKeyString.hashCode();
					Object2ReferenceMap<Integer, ItemSettings> itemsMap = new Object2ReferenceOpenHashMap<>();
					List<String> biomeNames = chestSettingsSection.getStringList(chestKeyString + ".Biomes");
					for (String biomeName : biomeNames) {
						chestBiomesMap.computeIfAbsent(biomeName.toUpperCase(), k -> new IntOpenHashSet()).add(chestKey);
					}
					Object itemsObj = chestSettingsSection.get(chestKeyString + ".Items");
					if (itemsObj instanceof List<?> itemsList) {
						for (Object itemObj : itemsList) {
							// Old format: "MATERIAL:WEIGHT:MAXAMOUNT"
							if (itemObj instanceof String itemString) {
								String[] parts = itemString.split(":");
								if (parts.length == 3) {
									Material material = Material.matchMaterial(parts[0]);
									if (material != null) {
										try {
											double weight = Double.parseDouble(parts[1]);
											int maxAmount = Integer.parseInt(parts[2]);
											itemsMap.put(material.ordinal(), new ItemSettings(material.ordinal(), weight, maxAmount));
										} catch (NumberFormatException e) {
											Cc.logS(Cc.RED, "Invalid number format in item settings: " + itemString);
										}
									} else {
										Cc.logS(Cc.RED, "Invalid material name: " + parts[0]);
									}
								}
							}
							// New format: map structure with custom name, item-level LevelType, and enchantments
							else if (itemObj instanceof Map<?, ?> itemMap) {
								if (itemMap.size() == 1) {
									Map.Entry<?, ?> entry = itemMap.entrySet().iterator().next();
									String materialName = entry.getKey().toString();
									Material material = Material.matchMaterial(materialName);
									if (material == null) {
										Cc.logS(Cc.RED, "Invalid material name: " + materialName);
										continue;
									}
									double weight = 1.0;
									int maxAmount = 1;
									String customName = null;
									String levelType = null;
									List<EnchantmentSetting> enchantments = new ArrayList<>();
									Object propertiesObj = entry.getValue();
									if (propertiesObj instanceof List<?> propList) {
										for (Object propObj : propList) {
											if (propObj instanceof Map<?, ?> propMap) {
												for (Map.Entry<?, ?> propEntry : propMap.entrySet()) {
													String key = propEntry.getKey().toString();
													Object value = propEntry.getValue();
													switch (key) {
													case "Weight" -> {
														try {
															weight = Double.parseDouble(value.toString());
														} catch (NumberFormatException e) {
															Cc.logS(Cc.RED, "Invalid Weight for " + materialName);
														}
													}
													case "MaxAmount" -> {
														try {
															maxAmount = Integer.parseInt(value.toString());
														} catch (NumberFormatException e) {
															Cc.logS(Cc.RED, "Invalid MaxAmount for " + materialName);
														}
													}
													case "CustomName" -> customName = value.toString();
													case "LevelType" -> levelType = value.toString();
													case "Enchantments" -> {
														if (value instanceof List<?> enchList) {
															for (Object enchObj : enchList) {
																if (enchObj instanceof Map<?, ?> enchMap) {
																	if (enchMap.size() == 1) {
																		Map.Entry<?, ?> enchEntry = enchMap.entrySet().iterator().next();
																		String enchName = enchEntry.getKey().toString();
																		double enchWeight = 0.0;
																		int minLevel = 0;
																		int maxLevel = 0;
																		String enchLoreColor = null;
																		Object enchPropsObj = enchEntry.getValue();
																		if (enchPropsObj instanceof List<?> enchPropsList) {
																			for (Object enchPropObj : enchPropsList) {
																				if (enchPropObj instanceof Map<?, ?> enchPropMap) {
																					for (Map.Entry<?, ?> eProp : enchPropMap.entrySet()) {
																						String propKey = eProp.getKey().toString();
																						Object propValue = eProp.getValue();
																						switch (propKey) {
																						case "Weight" -> {
																							try {
																								enchWeight = Double.parseDouble(propValue.toString());
																							} catch (NumberFormatException ex) {
																								Cc.logS(Cc.RED, "Invalid enchantment Weight for " + enchName);
																							}
																						}
																						case "MinLevel" -> {
																							try {
																								minLevel = Integer.parseInt(propValue.toString());
																							} catch (NumberFormatException ex) {
																								Cc.logS(Cc.RED, "Invalid enchantment MinLevel for " + enchName);
																							}
																						}
																						case "MaxLevel" -> {
																							try {
																								maxLevel = Integer.parseInt(propValue.toString());
																							} catch (NumberFormatException ex) {
																								Cc.logS(Cc.RED, "Invalid enchantment MaxLevel for " + enchName);
																							}
																						}
																						case "LoreColor" -> enchLoreColor = propValue.toString();
																						default -> { }
																						}
																					}
																				}
																			}
																		}
																		enchantments.add(new EnchantmentSetting(enchName, enchWeight, minLevel, maxLevel, enchLoreColor));
																	}
																}
															}
														}
													}
													default -> Cc.logS(Cc.GOLD, "Unknown property: " + key + " for " + materialName);
													}
												}
											}
										}
									}
									ItemSettings settings = new ItemSettings(material.ordinal(), weight, maxAmount, customName, enchantments, levelType);
									itemsMap.put(material.ordinal(), settings);
								}
							} else {
								Cc.logS(Cc.RED, "Unknown item format in ChestSettings for chest: " + chestKeyString);
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
		String customName;
		List<EnchantmentSetting> enchantments;
		String levelType;

		ItemSettings(int materialOrdinal, double weight, int maxAmount) {
			this(materialOrdinal, weight, maxAmount, null, new ArrayList<>(), null);
		}

		ItemSettings(int materialOrdinal, double weight, int maxAmount, String customName, List<EnchantmentSetting> enchantments, String levelType) {
			this.materialOrdinal = materialOrdinal;
			this.weight = weight;
			this.maxAmount = maxAmount;
			this.customName = customName;
			this.enchantments = enchantments;
			this.levelType = levelType;
		}
	}

	static class EnchantmentSetting {
		String enchantmentName;
		double weight;
		int minLevel;
		int maxLevel;
		String loreColor;

		EnchantmentSetting(String enchantmentName, double weight, int minLevel, int maxLevel, String loreColor) {
			this.enchantmentName = enchantmentName;
			this.weight = weight;
			this.minLevel = minLevel;
			this.maxLevel = maxLevel;
			this.loreColor = loreColor;
		}
	}

	static class ItemsData {
		private final ItemSettings[] itemSettingsArray;
		private final double[] cumulativeWeights;
		private final double totalWeight;

		ItemsData(Object2ReferenceMap<Integer, ItemSettings> itemsMap) {
			Collection<ItemSettings> values = itemsMap.values();
			itemSettingsArray = values.toArray(new ItemSettings[0]);
			cumulativeWeights = new double[itemSettingsArray.length];

			double cumulative = 0.0;
			for (int i = 0; i < itemSettingsArray.length; i++) {
				cumulative += itemSettingsArray[i].weight;
				cumulativeWeights[i] = cumulative;
			}
			totalWeight = cumulative;
		}

		public ItemSettings getRandomItemSettings() {
			if (totalWeight <= 0.0) {
				return null;
			}
			double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
			int index = Arrays.binarySearch(cumulativeWeights, randomValue);
			return index < 0 ? itemSettingsArray[-index - 1] : itemSettingsArray[index];
		}
	}
}