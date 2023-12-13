package main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Biome;
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

			if (chestSettings != null && chestSettings.isConfigurationSection("ChestSettings")) {
				ConfigurationSection chestSettingsSection = chestSettings.getConfigurationSection("ChestSettings");

				String biomeName = getBiomeNameFromBlock(block);

				for (String chestKey : chestSettingsSection.getKeys(false)) {
					ConfigurationSection chestSection = chestSettingsSection.getConfigurationSection(chestKey);
					List<String> biomes = chestSection.getStringList("Biomes");

					if (biomes.contains(biomeName)) {
						List<ItemSettings> itemSettingsList = getItemSettingsList(chestKey);
						loadChestWithItems(itemSettingsList, chestInventory);
						return;
					}
				}
			}

			String worldName = block.getWorld().getName();
			loadDefaultChestContents(chestInventory, worldName, getBiomeNameFromBlock(block));
		}
	}

	private void loadChestWithItems(List<ItemSettings> itemSettingsList, Inventory chestInventory) {
	    Set<String> uniqueItems = new HashSet<>();
	    List<Integer> undistributedSlots = new ArrayList<>();

	    for (int i = 0; i < chestInventory.getSize(); i++) {
	        undistributedSlots.add(i);
	    }

	    for (int i = 0; i < chestInventory.getSize(); i++) {
	        ItemSettings itemSettings = chooseRandomItemSettingsWithWeights(itemSettingsList);

	        if (itemSettings != null) {
	            String itemName = itemSettings.itemName;

	            if (!uniqueItems.contains(itemName)) {
	                int amount = getRandomAmount(itemSettings);
	                Material material = Material.matchMaterial(itemName);

	                if (material != null) {
	                    // Check if there are undistributed slots
	                    if (!undistributedSlots.isEmpty()) {
	                        // Distribute in random quantities if the amount in the chest slot is greater than 2
	                        if (amount > 2) {
	                            int undistributedAmount = getRandomUndistributedAmount(amount, undistributedSlots.size());
	                            distributeRandomly(chestInventory, material, undistributedAmount, undistributedSlots);
	                        } else {
	                            // Place the item in the first undistributed slot
	                            int undistributedSlot = undistributedSlots.get(0);
	                            chestInventory.setItem(undistributedSlot, new ItemStack(material, amount));
	                            undistributedSlots.remove(Integer.valueOf(undistributedSlot));
	                        }
	                        uniqueItems.add(itemName);
	                    }
	                } else {
	                    Bukkit.getLogger().warning("Invalid material name: " + itemName);
	                }
	            }
	        }
	    }
	}

	private int getRandomUndistributedAmount(int totalAmount, int numUndistributedSlots) {
	    return Math.min(totalAmount, random.nextInt(numUndistributedSlots) + 1);
	}

	private void distributeRandomly(Inventory chestInventory, Material material, int totalAmount, List<Integer> undistributedSlots) {
	    // Shuffle the undistributed slots for random distribution
	    Collections.shuffle(undistributedSlots);

	    while (totalAmount > 0 && !undistributedSlots.isEmpty()) {
	        int undistributedSlot = undistributedSlots.remove(0);
	        int amount = Math.min(totalAmount, random.nextInt(totalAmount) + 1);
	        chestInventory.setItem(undistributedSlot, new ItemStack(material, amount));
	        totalAmount -= amount;
	    }
	}

	private int getRandomAmount(ItemSettings itemSettings) {
		int minAmount = 0;
		int maxAmount = itemSettings.maxAmount;
		return random.nextInt(maxAmount - minAmount + 1) + minAmount;
	}

	private boolean loadDefaultChestContents(Inventory chestInventory, String worldName, String biomeName) {
	    List<Material> tier1Items = Arrays.asList(Material.WOODEN_AXE, Material.WOODEN_PICKAXE, Material.CROSSBOW, Material.NAME_TAG, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.GOLD_BLOCK, Material.GOLDEN_AXE, Material.IRON_BLOCK, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Material.IRON_SWORD, Material.GOLDEN_HELMET, Material.MUSIC_DISC_PIGSTEP, Material.ELYTRA, Material.DRAGON_HEAD, Material.DRAGON_EGG, Material.END_CRYSTAL, Material.ENDER_CHEST);

	    List<Material> tier2Items = Arrays.asList(Material.APPLE, Material.OAK_LOG, Material.DARK_OAK_LOG, Material.RAIL, Material.DIAMOND, Material.IRON_INGOT, Material.GILDED_BLACKSTONE, Material.ANCIENT_DEBRIS, Material.BONE_BLOCK, Material.GOLDEN_CARROT, Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.SHULKER_SHELL);

	    List<Material> tier3Items = Arrays.asList(Material.CHAIN, Material.GOLD_INGOT, Material.MAGMA_CREAM, Material.GOLD_NUGGET, Material.SPECTRAL_ARROW, Material.STRING, Material.IRON_NUGGET, Material.ARROW, Material.COOKED_PORKCHOP, Material.STICK, Material.WHEAT, Material.END_STONE, Material.END_ROD, Material.PURPUR_BLOCK, Material.MAGENTA_STAINED_GLASS);

	    List<Material> items = getItemsForWorld(worldName);

	    if (biomeName != null && !biomeName.isEmpty() && items.isEmpty()) {
	        return false;
	    }

	    // Shuffle the list of items to distribute randomly
	    Collections.shuffle(items, random);

	    for (Material item : items) {
	        int baseAmount = random.nextInt(3) + 1;
	        int num = random.nextInt(10) + 1; // For tier selection probability

	        // Determine tier and adjust baseAmount accordingly
	        if (tier1Items.contains(item) && num <= 2) {
	            baseAmount = 1;
	        } else if (tier2Items.contains(item) && num <= 4) {
	            baseAmount = random.nextInt(2) + 1;
	        } else if (tier3Items.contains(item) && num <= 9) {
	            baseAmount = random.nextInt(5) + 1;
	        } else {
	            baseAmount = 0; // Not in any tier
	        }

	        // Split the base amount into multiple random amounts
	        int totalAmount = (baseAmount > 1) ? random.nextInt(baseAmount) + 1 : baseAmount;

	        // Distribute the total amount randomly
	        while (totalAmount > 0) {
	            int amount = Math.min(totalAmount, random.nextInt(totalAmount) + 1);

	            // Find a random empty slot to place the item
	            int slot = getRandomEmptySlot(chestInventory);
	            if (slot != -1) {
	                chestInventory.setItem(slot, new ItemStack(item, amount));
	                totalAmount -= amount;
	            }
	        }
	    }
	    return true;
	}

	// Helper method to get a random empty slot in the inventory
	private int getRandomEmptySlot(Inventory inventory) {
	    int size = inventory.getSize();
	    List<Integer> emptySlots = new ArrayList<>();

	    for (int i = 0; i < size; i++) {
	        if (inventory.getItem(i) == null) {
	            emptySlots.add(i);
	        }
	    }

	    if (emptySlots.isEmpty()) {
	        return -1; // No empty slots available
	    }

	    // Choose a random empty slot
	    return emptySlots.get(random.nextInt(emptySlots.size()));
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

	private String getBiomeNameFromBlock(Block block) {
		if (block != null) {
			Biome biome = block.getBiome();
			return biome.name().toUpperCase();
		}
		return null;
	}

	private ItemSettings chooseRandomItemSettingsWithWeights(List<ItemSettings> itemSettingsList) {
		if (itemSettingsList.isEmpty()) {
			return null;
		}

		List<ItemSettings> weightedItemSettings = new ArrayList<>();
		double totalWeight = 0.0;

		for (ItemSettings itemSettings : itemSettingsList) {
			totalWeight += itemSettings.weight;
			weightedItemSettings.add(new ItemSettings(itemSettings.itemName, totalWeight, itemSettings.maxAmount));
		}

		double randomValue = random.nextDouble() * totalWeight;
		for (ItemSettings itemSettings : weightedItemSettings) {
			if (randomValue < itemSettings.weight) {
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