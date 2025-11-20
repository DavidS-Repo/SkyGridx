package main;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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

/**
 * Handles custom chest configs and filling chest contents.
 */
public class CustomChest {
	private static CustomChest instance;
	private static JavaPlugin plugin;
	private static int maxItemsPerSlot;
	private static final Material[] MATERIALS = Material.values();

	public static NamespacedKey UNPOPULATED_KEY;
	private static NamespacedKey CUSTOM_REF_KEY;

	private final Int2ObjectMap<ChestConfig> chestConfigsByHash = new Int2ObjectOpenHashMap<>();
	private final Map<String, ChestConfig> chestConfigsByName = new HashMap<>();
	private final Map<String, IntSet> biomeToChestHashes = new HashMap<>();

	private CustomChest(JavaPlugin plugin) {
		CustomChest.plugin = plugin;
		UNPOPULATED_KEY = new NamespacedKey(plugin, "custom_loot_unpopulated");
		CUSTOM_REF_KEY = new NamespacedKey(plugin, "custom_loot_pointer");
		loadChestSettingsAsync();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static CustomChest getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new CustomChest(plugin);
		}
		return instance;
	}

	/**
	 * Registers a chest from a block and writes it to the DB in a batch.
	 */
	public void loadChest(Block block) {
		List<ChestRegionData.ChestRecord> records = new ArrayList<>(1);
		loadChest(block, records);
		if (!records.isEmpty()) {
			ChestRegionData.getInstance(plugin).registerChests(records);
		}
	}

	/**
	 * Registers a chest from a block and adds it to the given record list.
	 */
	public void loadChest(Block block, List<ChestRegionData.ChestRecord> records) {
		if (!(block.getState() instanceof Chest chest)) return;

		String biomeRaw = block.getBiome().getKey().getKey().toUpperCase();

		IntSet possible = biomeToChestHashes.getOrDefault(biomeRaw, new IntOpenHashSet());
		if (possible.isEmpty()) {
			plugin.getLogger().warning("No chest config for biome: " + biomeRaw + " at " + chest.getLocation());
			return;
		}

		ChestConfig cfg = chestConfigsByHash.get(possible.iterator().nextInt());
		if (cfg == null) {
			plugin.getLogger().warning("Chest config is null for biome: " + biomeRaw);
			return;
		}

		if (cfg.hasLootPool()) {
			PoolEntry pick = cfg.selectRandomLootPool();
			if (pick == null) return;

			if (pick.vanillaTable != null) {
				records.add(new ChestRegionData.ChestRecord(
						chest.getLocation(),
						ChestRegionData.ChestType.VANILLA,
						pick.vanillaTable.getKey().toString()
						));
				return;
			}

			if (pick.customRefName != null) {
				ChestConfig ref = chestConfigsByName.get(pick.customRefName);
				if (ref != null && ref.itemsData != null) {
					records.add(new ChestRegionData.ChestRecord(
							chest.getLocation(),
							ChestRegionData.ChestType.CUSTOM,
							pick.customRefName
							));
				}
			}
			return;
		}

		if (cfg.itemsData != null) {
			records.add(new ChestRegionData.ChestRecord(
					chest.getLocation(),
					ChestRegionData.ChestType.CUSTOM,
					null
					));
		}
	}

	/**
	 * Fills a chest on open based on stored info or biome.
	 */
	public void populateChestOnOpen(Chest chest, ChestRegionData.ChestInfo info) {
		ChestConfig cfg = null;

		if (info != null && info.lootTableKey != null) {
			cfg = chestConfigsByName.get(info.lootTableKey);
		}

		if (cfg == null) {
			String biomeKey = chest.getBlock().getBiome().getKey().getKey().toUpperCase();
			IntSet set = biomeToChestHashes.getOrDefault(biomeKey, new IntOpenHashSet());
			if (set.isEmpty()) {
				plugin.getLogger().warning("No chest config for biome: " + biomeKey + " when populating chest");
				return;
			}
			cfg = chestConfigsByHash.get(set.iterator().nextInt());
		}

		if (cfg == null || cfg.itemsData == null) {
			return;
		}

		calculateItemsToPlace(cfg.itemsData, chest.getInventory())
		.thenAccept(stacks ->
		new BukkitRunnable() {
			@Override
			public void run() {
				placeItemsInChest(chest.getInventory(), stacks);
			}
		}.runTask(plugin)
				);
	}

	/**
	 * Fills a legacy NBT based chest on open.
	 */
	public void populateLegacyChestOnOpen(Chest chest) {
		String refName = chest.getPersistentDataContainer().get(CUSTOM_REF_KEY, PersistentDataType.STRING);
		ChestConfig cfg;
		if (refName != null) {
			cfg = chestConfigsByName.get(refName);
		} else {
			String biomeKey = chest.getBlock().getBiome().getKey().getKey().toUpperCase();
			IntSet set = biomeToChestHashes.getOrDefault(biomeKey, new IntOpenHashSet());
			if (set.isEmpty()) return;
			cfg = chestConfigsByHash.get(set.iterator().nextInt());
		}

		if (cfg == null || cfg.itemsData == null) return;

		calculateItemsToPlace(cfg.itemsData, chest.getInventory())
		.thenAccept(stacks ->
		new BukkitRunnable() {
			@Override
			public void run() {
				placeItemsInChest(chest.getInventory(), stacks);
			}
		}.runTask(plugin)
				);
	}

	private CompletableFuture<ItemStack[]> calculateItemsToPlace(ItemsData data, Inventory inv) {
		return CompletableFuture.supplyAsync(() -> {
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			ItemStack[] out = new ItemStack[inv.getSize()];

			int[] counts = getCurrentItemCounts(inv);
			List<Integer> free = getEmptySlotList(inv);

			for (ItemSettings chance : data.chanceItems) {
				if (rng.nextDouble(100) >= chance.chancePerChestPercent) continue;
				if (free.isEmpty()) break;

				int slot = free.remove(rng.nextInt(free.size()));
				int left = chance.maxAmount - counts[chance.materialOrdinal];
				if (left <= 0) continue;

				int amt = rng.nextInt(chance.minAmount, Math.min(left, maxItemsPerSlot) + 1);
				if (amt <= 0) continue;

				out[slot] = buildStack(chance, amt, rng);
				counts[chance.materialOrdinal] += amt;
			}

			int[] slots = free.stream().mapToInt(i -> i).toArray();
			for (int s : slots) {
				ItemSettings picked = data.getRandomWeightedItem();
				if (picked == null) continue;

				int left = picked.maxAmount - counts[picked.materialOrdinal];
				if (left <= 0) continue;

				int cap = Math.min(left, maxItemsPerSlot);
				if (cap < picked.minAmount) continue;

				int amt = rng.nextInt(picked.minAmount, cap + 1);
				if (amt <= 0) continue;

				out[s] = buildStack(picked, amt, rng);
				counts[picked.materialOrdinal] += amt;
			}

			return out;
		});
	}

	@SuppressWarnings("deprecation")
	private ItemStack buildStack(ItemSettings settings, int amount, ThreadLocalRandom rng) {
		Material mat = MATERIALS[settings.materialOrdinal];
		ItemStack stack = new ItemStack(mat, amount);

		if (settings.customName != null && !settings.customName.isEmpty()) {
			ItemMeta nm = stack.getItemMeta();
			nm.setDisplayName(settings.customName);
			stack.setItemMeta(nm);
		}

		if (!settings.enchantments.isEmpty()) {
			if (mat == Material.ENCHANTED_BOOK) {
				EnchantmentStorageMeta storage = (EnchantmentStorageMeta) stack.getItemMeta();
				for (EnchantmentSetting es : settings.enchantments) {
					if (rng.nextDouble(100) < es.weight) {
						int lvl = rng.nextInt(es.minLevel, es.maxLevel + 1);
						if (lvl > 0) {
							Enchantment ench = Enchantment.getByName(es.enchantmentName.toUpperCase());
							if (ench != null) {
								storage.addStoredEnchant(ench, lvl, true);
							}
						}
					}
				}
				stack.setItemMeta(storage);
			} else {
				ItemMeta meta = stack.getItemMeta();
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				List<String> lore = meta.hasLore()
						? new ArrayList<>(meta.getLore())
								: new ArrayList<>();
				String lt = "standard".equalsIgnoreCase(settings.levelType) ? "Standard" : "Roman";

				for (EnchantmentSetting es : settings.enchantments) {
					if (rng.nextDouble(100) < es.weight) {
						int lvl = rng.nextInt(es.minLevel, es.maxLevel + 1);
						if (lvl > 0) {
							Enchantment ench = Enchantment.getByName(es.enchantmentName.toUpperCase());
							if (ench != null) {
								meta.addEnchant(ench, lvl, true);
								String lvlText = lt.equals("Standard")
										? "lvl_" + lvl
												: EnchantmentNameSupport.toRoman(lvl);
								String color = EnchantmentNameSupport.getColor(es.loreColor);
								String name = EnchantmentNameSupport.getFriendlyName(ench);
								lore.add(color + name + " " + lvlText);
							}
						}
					}
				}
				meta.setLore(lore);
				stack.setItemMeta(meta);
			}
		}

		if (mat == Material.TIPPED_ARROW) {
			TippedArrowRandomizer randomizer = new TippedArrowRandomizer();
			randomizer.randomizeTippedArrow(stack);
		}

		return stack;
	}

	private List<Integer> getEmptySlotList(Inventory inv) {
		List<Integer> list = new ArrayList<>();
		ItemStack[] cont = inv.getContents();
		for (int i = 0; i < cont.length; i++) {
			if (cont[i] == null) list.add(i);
		}
		return list;
	}

	private int[] getCurrentItemCounts(Inventory inv) {
		int[] arr = new int[MATERIALS.length];
		for (ItemStack it : inv.getContents()) {
			if (it != null) arr[it.getType().ordinal()] += it.getAmount();
		}
		return arr;
	}

	private void placeItemsInChest(Inventory inv, ItemStack[] items) {
		ItemStack[] base = inv.getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) base[i] = items[i];
		}
		inv.setContents(base);
	}

	private void loadChestSettingsAsync() {
		CompletableFuture.runAsync(() -> {
			File f = new File(plugin.getDataFolder(), "SkygridBlocks/ChestSettings.yml");
			if (!f.exists()) {
				plugin.getLogger().severe("ChestSettings.yml not found.");
				return;
			}
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
			maxItemsPerSlot = cfg.getInt("MaxItemsPerSlot", 2);

			chestConfigsByHash.clear();
			chestConfigsByName.clear();
			biomeToChestHashes.clear();

			if (!cfg.isConfigurationSection("ChestSettings")) {
				plugin.getLogger().severe("Missing ChestSettings section");
				return;
			}

			ConfigurationSection root = cfg.getConfigurationSection("ChestSettings");
			for (String key : root.getKeys(false)) {
				ChestConfig c = parseSingleChest(key, root.getConfigurationSection(key));
				chestConfigsByHash.put(key.hashCode(), c);
				chestConfigsByName.put(key, c);
				for (String b : root.getStringList(key + ".Biomes")) {
					biomeToChestHashes
					.computeIfAbsent(b.toUpperCase(), __ -> new IntOpenHashSet())
					.add(key.hashCode());
				}
			}
		});
	}

	private ChestConfig parseSingleChest(String key, ConfigurationSection sec) {
		ChestConfig cfg = new ChestConfig();

		if (sec.contains("LootTables")) {
			for (Object o : sec.getList("LootTables")) {
				if (!(o instanceof Map<?, ?> map) || map.size() != 1) continue;

				Map.Entry<?, ?> e = map.entrySet().iterator().next();
				String name = e.getKey().toString();
				double w = 1;

				if (e.getValue() instanceof List<?> props) {
					for (Object p : props) {
						if (p instanceof Map<?, ?> pm && pm.containsKey("Weight")) {
							w = tryParseDouble(pm.get("Weight"), 1);
						}
					}
				}

				PoolEntry pe = new PoolEntry();
				pe.weight = w;
				try {
					pe.vanillaTable = LootTables.valueOf(name.toUpperCase()).getLootTable();
				} catch (IllegalArgumentException ex) {
					pe.customRefName = name;
				}
				cfg.lootPool.add(pe);
			}
		}

		if (sec.contains("Items")) {
			Object raw = sec.get("Items");
			Object2ReferenceMap<Integer, ItemSettings> map = new Object2ReferenceOpenHashMap<>();
			if (raw instanceof List<?> list) {
				for (Object o : list) {
					if (o instanceof String s) {
						parseCompactItemString(s, map);
					} else if (o instanceof Map<?, ?> m) {
						parseExpandedItemNode(m, map);
					}
				}
			}
			cfg.itemsData = new ItemsData(map);
		}

		return cfg;
	}

	private void parseCompactItemString(String s, Object2ReferenceMap<Integer, ItemSettings> map) {
		String[] p = s.split(":");
		if (p.length < 3) return;

		Material mat = Material.matchMaterial(p[0]);
		if (mat == null) {
			plugin.getLogger().warning("Bad material: " + s);
			return;
		}

		boolean pct = p[1].endsWith("p") || p[1].endsWith("P");
		double num = tryParseDouble(p[1].replace("p", "").replace("P", ""), 1);
		int min = 1, max;

		if (p[2].contains("-")) {
			String[] r = p[2].split("-");
			min = (int) tryParseDouble(r[0], 1);
			max = (int) tryParseDouble(r[1], 1);
		} else {
			max = (int) tryParseDouble(p[2], 1);
		}

		ItemSettings it = new ItemSettings(mat.ordinal(), pct ? 1 : num, min, max);
		if (pct) it.chancePerChestPercent = num;
		map.put(mat.ordinal(), it);
	}

	private void parseExpandedItemNode(Map<?, ?> node, Object2ReferenceMap<Integer, ItemSettings> map) {
		if (node.size() != 1) return;

		Map.Entry<?, ?> e = node.entrySet().iterator().next();
		Material mat = Material.matchMaterial(e.getKey().toString());
		if (mat == null) return;

		double weight = 1, chance = -1;
		int min = 1, max = 1;
		String cname = null, ltype = null;
		List<EnchantmentSetting> ench = new ArrayList<>();

		if (e.getValue() instanceof List<?> list) {
			for (Object o : list) {
				if (!(o instanceof Map<?, ?> m)) continue;
				for (Map.Entry<?, ?> sub : m.entrySet()) {
					String k = sub.getKey().toString();
					switch (k) {
					case "Weight" -> weight = tryParseDouble(sub.getValue(), weight);
					case "ChancePerChest" -> chance = tryParseDouble(sub.getValue(), chance);
					case "MinAmount" -> min = (int) tryParseDouble(sub.getValue(), min);
					case "MaxAmount" -> max = (int) tryParseDouble(sub.getValue(), max);
					case "CustomName" -> cname = sub.getValue().toString();
					case "LevelType" -> ltype = sub.getValue().toString();
					case "Enchantments" -> buildEnchantList(sub.getValue(), ench);
					}
				}
			}
		}

		ItemSettings it = new ItemSettings(mat.ordinal(), weight, min, max, cname, ench, ltype);
		if (chance >= 0) it.chancePerChestPercent = chance;
		map.put(mat.ordinal(), it);
	}

	private void buildEnchantList(Object raw, List<EnchantmentSetting> out) {
		if (!(raw instanceof List<?> list)) return;

		for (Object o : list) {
			if (!(o instanceof Map<?, ?> m) || m.size() != 1) continue;
			Map.Entry<?, ?> e = m.entrySet().iterator().next();
			String name = e.getKey().toString();

			double w = 0;
			int min = 0, max = 0;
			String col = null;

			if (e.getValue() instanceof List<?> props) {
				for (Object p : props) {
					if (!(p instanceof Map<?, ?> pm)) continue;
					for (Map.Entry<?, ?> en : pm.entrySet()) {
						String kk = en.getKey().toString();
						switch (kk) {
						case "Weight" -> w = tryParseDouble(en.getValue(), w);
						case "MinLevel" -> min = (int) tryParseDouble(en.getValue(), min);
						case "MaxLevel" -> max = (int) tryParseDouble(en.getValue(), max);
						case "LoreColor" -> col = en.getValue().toString();
						}
					}
				}
			}

			out.add(new EnchantmentSetting(name, w, min, max, col));
		}
	}

	private double tryParseDouble(Object v, double def) {
		try {
			return Double.parseDouble(v.toString());
		} catch (Exception ex) {
			return def;
		}
	}

	static class ChestConfig {
		final List<PoolEntry> lootPool = new ArrayList<>();
		ItemsData itemsData;

		boolean hasLootPool() {
			return !lootPool.isEmpty();
		}

		PoolEntry selectRandomLootPool() {
			double total = 0;
			for (PoolEntry e : lootPool) total += e.weight;
			if (total <= 0) return null;

			double r = ThreadLocalRandom.current().nextDouble(total);
			double acc = 0;

			for (PoolEntry e : lootPool) {
				acc += e.weight;
				if (r < acc) return e;
			}
			return lootPool.get(0);
		}
	}

	static class PoolEntry {
		LootTable vanillaTable;
		String customRefName;
		double weight;
	}

	static class ItemSettings {
		int materialOrdinal;
		double weight;
		int minAmount;
		int maxAmount;
		String customName;
		List<EnchantmentSetting> enchantments;
		String levelType;
		double chancePerChestPercent = -1;

		ItemSettings(int ord, double w, int min, int max) {
			this(ord, w, min, max, null, new ArrayList<>(), null);
		}

		ItemSettings(int ord, double w, int min, int max,
				String cn, List<EnchantmentSetting> el, String lt) {
			materialOrdinal = ord;
			weight = w;
			minAmount = min;
			maxAmount = max;
			customName = cn;
			enchantments = el;
			levelType = lt;
		}

		boolean usesChanceMode() {
			return chancePerChestPercent >= 0;
		}
	}

	static class EnchantmentSetting {
		String enchantmentName;
		double weight;
		int minLevel;
		int maxLevel;
		String loreColor;

		EnchantmentSetting(String n, double w, int min, int max, String c) {
			enchantmentName = n;
			weight = w;
			minLevel = min;
			maxLevel = max;
			loreColor = c;
		}
	}

	static class ItemsData {
		final List<ItemSettings> chanceItems = new ArrayList<>();
		final ItemSettings[] weightArr;
		final double[] cum;
		final double total;

		ItemsData(Object2ReferenceMap<Integer, ItemSettings> src) {
			List<ItemSettings> wl = new ArrayList<>();
			for (ItemSettings s : src.values()) {
				if (s.usesChanceMode()) chanceItems.add(s);
				else wl.add(s);
			}
			weightArr = wl.toArray(new ItemSettings[0]);
			cum = new double[weightArr.length];
			double a = 0;
			for (int i = 0; i < weightArr.length; i++) {
				a += weightArr[i].weight;
				cum[i] = a;
			}
			total = a;
		}

		ItemSettings getRandomWeightedItem() {
			if (total <= 0) return null;
			double r = ThreadLocalRandom.current().nextDouble(total);
			int idx = Arrays.binarySearch(cum, r);
			if (idx < 0) idx = -idx - 1;
			return weightArr[idx];
		}
	}
}
