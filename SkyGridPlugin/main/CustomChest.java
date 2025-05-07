package main;

import main.EnchantmentNameSupport.ChosenEnchantment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
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

	// Max items per slot from config
	private static int maxPerSlot;

	// Marker key for unpopulated custom chests
	public static NamespacedKey UNPOPULATED_KEY;

	private static final Material[] MATERIALS = Material.values();

	private final Int2ObjectMap<ChestConfig> chestConfigs = new Int2ObjectOpenHashMap<>();
	private final Map<String, IntSet> chestBiomesMap = new HashMap<>();

	private CustomChest(JavaPlugin plugin) {
		CustomChest.plugin = plugin;
		UNPOPULATED_KEY = new NamespacedKey(plugin, "custom_loot_unpopulated");
		loadChestSettingsAsync();
	}

	public static CustomChest getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new CustomChest(plugin);
		}
		return instance;
	}

	/**
	 * Called during world/chunk gen for each chest block.
	 */
	public void loadChest(Block block) {
		if (!(block.getState() instanceof Chest chest)) return;

		String biome = block.getBiome().toString().toUpperCase();
		IntSet keys = chestBiomesMap.getOrDefault(biome, new IntOpenHashSet());
		if (keys.isEmpty()) return;

		ChestConfig cfg = chestConfigs.get(keys.iterator().nextInt());
		if (cfg == null) return;

		if (cfg.lootTablesConfig != null) {
			LootTable lt = LootTableSelector.chooseWeightedLootTable(cfg.lootTablesConfig);
			if (lt != null) {
				chest.setLootTable(lt);
				chest.update();
			}
		} else if (cfg.itemsData != null) {
			chest.getPersistentDataContainer().set(UNPOPULATED_KEY, PersistentDataType.BYTE, (byte) 1);
			chest.getInventory().clear();
			chest.update();
		}
	}

	/**
	 * Populate on first open for custom loots.
	 */
	public void populateChestOnOpen(Chest chest) {
		String biome = chest.getBlock().getBiome().toString().toUpperCase();
		IntSet keys = chestBiomesMap.getOrDefault(biome, new IntOpenHashSet());
		if (keys.isEmpty()) return;

		ChestConfig cfg = chestConfigs.get(keys.iterator().nextInt());
		if (cfg == null || cfg.itemsData == null) return;

		calculateItemsToPlace(cfg.itemsData, chest.getInventory())
		.thenAccept(items -> plugin.getServer().getScheduler().runTask(plugin, () ->
		placeItemsInChest(chest.getInventory(), items)
				));
	}

	@SuppressWarnings("deprecation")
	private CompletableFuture<ItemStack[]> calculateItemsToPlace(ItemsData data, Inventory inv) {
		return CompletableFuture.supplyAsync(() -> {
			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			ItemStack[] toPlace = new ItemStack[inv.getSize()];
			int[] counts = getCurrentItemCounts(inv);
			int[] emptySlots = getEmptySlots(inv);

			for (int slot : emptySlots) {
				ItemSettings s = data.getRandomItemSettings();
				if (s == null) continue;

				int have = counts[s.materialOrdinal];
				int chestLeft = s.maxAmount - have;
				if (chestLeft <= 0) continue;

				int slotMax = Math.min(chestLeft, maxPerSlot);
				if (slotMax >= s.minAmount) {
					int amt = rnd.nextInt(s.minAmount, slotMax + 1);
					if (amt > 0) {
						ItemStack stack = new ItemStack(MATERIALS[s.materialOrdinal], amt);
						ItemMeta meta = stack.getItemMeta();
						if (s.customName != null && !s.customName.isEmpty()) {
							meta.setDisplayName(s.customName);
						}
						stack.setItemMeta(meta);

						List<ChosenEnchantment> chosen = new ArrayList<>();
						if (!s.enchantments.isEmpty()) {
							String lt = "standard".equalsIgnoreCase(s.levelType) ? "Standard" : "Roman";
							for (EnchantmentSetting es : s.enchantments) {
								if (rnd.nextDouble(100) < es.weight) {
									int lvl = rnd.nextInt(es.minLevel, es.maxLevel + 1);
									if (lvl > 0) {
										Enchantment ench = Enchantment.getByName(es.enchantmentName.toUpperCase());
										if (ench != null) {
											String color = (es.loreColor == null || es.loreColor.isBlank())
													? "GRAY" : es.loreColor;
											chosen.add(new ChosenEnchantment(ench, lvl, lt, color));
										}
									}
								}
							}
						}
						EnchantmentNameSupport.applyCustomEnchantDisplay(stack, chosen);

						toPlace[slot] = stack;
						counts[s.materialOrdinal] += amt;
					}
				}
			}
			return toPlace;
		});
	}

	private void placeItemsInChest(Inventory inv, ItemStack[] items) {
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) contents[i] = items[i];
		}
		inv.setContents(contents);
	}

	private int[] getEmptySlots(Inventory inv) {
		List<Integer> empty = new ArrayList<>();
		ItemStack[] cont = inv.getContents();
		for (int i = 0; i < cont.length; i++) {
			if (cont[i] == null) empty.add(i);
		}
		return empty.stream().mapToInt(Integer::intValue).toArray();
	}

	private int[] getCurrentItemCounts(Inventory inv) {
		int[] counts = new int[MATERIALS.length];
		for (ItemStack it : inv.getContents()) {
			if (it != null) counts[it.getType().ordinal()] += it.getAmount();
		}
		return counts;
	}

	private void loadChestSettingsAsync() {
		CompletableFuture.runAsync(() -> {
			File f = new File(plugin.getDataFolder(), "SkygridBlocks/ChestSettings.yml");
			if (!f.exists()) {
				plugin.getLogger().severe("ChestSettings.yml not found.");
				return;
			}
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
			maxPerSlot = cfg.getInt("MaxItemsPerSlot", 2);

			chestConfigs.clear();
			chestBiomesMap.clear();

			if (!cfg.isConfigurationSection("ChestSettings")) {
				plugin.getLogger().severe("Invalid ChestSettings.yml format.");
				return;
			}
			ConfigurationSection root = cfg.getConfigurationSection("ChestSettings");
			for (String key : root.getKeys(false)) {
				int hash = key.hashCode();
				ChestConfig cc = new ChestConfig();

				if (root.contains(key + ".LootTables")) {
					cc.lootTablesConfig = root.getMapList(key + ".LootTables");
				} else if (root.contains(key + ".Items")) {
					Object itemsObj = root.get(key + ".Items");
					Object2ReferenceMap<Integer, ItemSettings> map = new Object2ReferenceOpenHashMap<>();

					if (itemsObj instanceof List<?> list) {
						for (Object o : list) {
							if (o instanceof String s) {
								// Compact: MATERIAL:WEIGHT:MAX or MATERIAL:WEIGHT:MIN-MAX
								String[] p = s.split(":");
								if (p.length == 3) {
									Material mat = Material.matchMaterial(p[0]);
									if (mat != null) {
										try {
											double w = Double.parseDouble(p[1]);
											String tok = p[2];
											int min = 0, max;
											if (tok.contains("-")) {
												String[] r = tok.split("-");
												min = Integer.parseInt(r[0]);
												max = Integer.parseInt(r[1]);
											} else {
												max = Integer.parseInt(tok);
											}
											map.put(mat.ordinal(), new ItemSettings(mat.ordinal(), w, min, max));
										} catch (NumberFormatException ex) {
											plugin.getLogger().severe("Invalid number in: " + s);
										}
									} else {
										plugin.getLogger().severe("Bad material: " + p[0]);
									}
								}

							} else if (o instanceof Map<?, ?> m) {
								// Expanded format
								if (m.size() != 1) continue;
								Map.Entry<?, ?> e = m.entrySet().iterator().next();
								String matName = e.getKey().toString();
								Material mat = Material.matchMaterial(matName);
								if (mat == null) {
									plugin.getLogger().severe("Bad material: " + matName);
									continue;
								}
								double w = 1;
								int minA = 0, maxA = 1;
								String custom = null, lvlType = null;
								List<EnchantmentSetting> enchList = new ArrayList<>();
								Object props = e.getValue();
								if (props instanceof List<?> propsList) {
									for (Object po : propsList) {
										if (!(po instanceof Map<?, ?> pm)) continue;
										for (Map.Entry<?, ?> pe : pm.entrySet()) {
											String k = pe.getKey().toString();
											String v = pe.getValue().toString();
											switch (k) {
											case "Weight" -> {
												try { w = Double.parseDouble(v); }
												catch (NumberFormatException ex) {
													plugin.getLogger().severe("Bad Weight for " + matName);
												}
											}
											case "MinAmount" -> {
												try { minA = Integer.parseInt(v); }
												catch (NumberFormatException ex) {
													plugin.getLogger().severe("Bad MinAmount for " + matName);
												}
											}
											case "MaxAmount" -> {
												try { maxA = Integer.parseInt(v); }
												catch (NumberFormatException ex) {
													plugin.getLogger().severe("Bad MaxAmount for " + matName);
												}
											}
											case "CustomName" -> custom = v;
											case "LevelType" -> lvlType = v;
											case "Enchantments" -> {
												if (pe.getValue() instanceof List<?> elist) {
													for (Object eo : elist) {
														if (!(eo instanceof Map<?, ?> emap) || emap.size() != 1) continue;
														Map.Entry<?, ?> ent = emap.entrySet().iterator().next();
														String en = ent.getKey().toString();
														double ew = 0;
														int minL = 0, maxL = 0;
														String color = null;
														if (ent.getValue() instanceof List<?> eprops) {
															for (Object epr : eprops) {
																if (!(epr instanceof Map<?, ?> epm)) continue;
																for (Map.Entry<?, ?> ep : epm.entrySet()) {
																	switch (ep.getKey().toString()) {
																	case "Weight" -> {
																		try {
																			ew = Double.parseDouble(ep.getValue().toString());
																		} catch (NumberFormatException ignored) {}
																	}
																	case "MinLevel" -> {
																		try {
																			minL = Integer.parseInt(ep.getValue().toString());
																		} catch (NumberFormatException ignored) {}
																	}
																	case "MaxLevel" -> {
																		try {
																			maxL = Integer.parseInt(ep.getValue().toString());
																		} catch (NumberFormatException ignored) {}
																	}
																	case "LoreColor" -> color = ep.getValue().toString();
																	}
																}
															}
														}
														enchList.add(new EnchantmentSetting(en, ew, minL, maxL, color));
													}
												}
											}
											default -> plugin.getLogger().warning("Unknown property " + k + " for " + matName);
											}
										}
									}
								}
								map.put(mat.ordinal(), new ItemSettings(mat.ordinal(), w, minA, maxA, custom, enchList, lvlType));
							}
						}
					}
					cc.itemsData = new ItemsData(map);
				}

				chestConfigs.put(hash, cc);

				List<String> biomes = root.getStringList(key + ".Biomes");
				for (String b : biomes) {
					chestBiomesMap.computeIfAbsent(b.toUpperCase(), __ -> new IntOpenHashSet()).add(hash);
				}
			}
		});
	}

	/* --- Data Classes --- */

	static class ChestConfig {
		List<Map<?, ?>> lootTablesConfig;
		ItemsData itemsData;
	}

	static class ItemSettings {
		int materialOrdinal;
		double weight;
		int minAmount;
		int maxAmount;
		String customName;
		List<EnchantmentSetting> enchantments;
		String levelType;

		ItemSettings(int materialOrdinal, double weight, int maxAmount) {
			this(materialOrdinal, weight, 0, maxAmount, null, new ArrayList<>(), null);
		}

		ItemSettings(int materialOrdinal, double weight, int minAmount, int maxAmount) {
			this(materialOrdinal, weight, minAmount, maxAmount, null, new ArrayList<>(), null);
		}

		ItemSettings(int materialOrdinal, double weight, int minAmount, int maxAmount,
				String customName, List<EnchantmentSetting> enchantments, String levelType) {
			this.materialOrdinal = materialOrdinal;
			this.weight = weight;
			this.minAmount = minAmount;
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
			Collection<ItemSettings> vals = itemsMap.values();
			itemSettingsArray = vals.toArray(new ItemSettings[0]);
			cumulativeWeights = new double[itemSettingsArray.length];
			double cum = 0;
			for (int i = 0; i < itemSettingsArray.length; i++) {
				cum += itemSettingsArray[i].weight;
				cumulativeWeights[i] = cum;
			}
			totalWeight = cum;
		}

		public ItemSettings getRandomItemSettings() {
			if (totalWeight <= 0) return null;
			double r = ThreadLocalRandom.current().nextDouble(totalWeight);
			int idx = Arrays.binarySearch(cumulativeWeights, r);
			return idx < 0 ? itemSettingsArray[-idx - 1] : itemSettingsArray[idx];
		}
	}
}