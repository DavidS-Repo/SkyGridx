# Chest Settings
 
## How to Use:
 
The available items can be found here https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
The available biomes can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html

In YAML, identifiers or keys (such as "Chest1" in your example) are subject to certain rules.

- Do's -
Alphanumeric Characters: Identifiers can contain letters (both uppercase and lowercase) and numbers. For example, "Chest1" is a valid identifier.
Underscores: You can use underscores (_) in identifiers. For instance, "my_chest" is a valid identifier.
Hyphens in Quoted Strings: If an identifier contains special characters or spaces, you can enclose it in single or double quotes. For example, "'Special Chest'" or "\"Special Chest\"" are valid.

- Don'ts -
Special Characters: Avoid using special characters like !, @, #, $, %, etc., in identifiers without enclosing them in quotes. For example, "Chest@123" should be written as "'Chest@123'" if you want to use special characters.
Leading Spaces: Identifiers should not have leading spaces. For instance, " MyChest" with a space at the beginning is not a valid identifier.
Trailing Spaces: Similarly, identifiers should not have trailing spaces. "MyChest " with a space at the end is not valid.
Spaces in the Middle: While you can use spaces within identifiers if they are enclosed in quotes, it's generally a good practice to avoid spaces within identifiers for clarity. For example, "My Chest" is valid, but "MyChest" is more commonly used.
Reserved Keywords: Avoid using YAML reserved keywords as identifiers. These keywords have special meaning in YAML and should not be used as keys. For example, "true," "false," and "null" are reserved keywords.
Case Sensitivity: YAML is case-sensitive. "myChest" and "MyChest" would be considered as two different identifiers.

ChestSettings: This is the top-level section of the YAML configuration and acts as the main container for all chest settings.
Chest1: This represents the first chest configuration, which we'll be using as an example. The code will look for a chest configuration with the key "Chest1." The key can be whatever you want; it serves as an identifier.
Items: Under the "Chest1" configuration, there is an "Items" section. The code will process the items listed here to determine what should be placed in the chest's inventory. Formatting: "- ITEM_NAME:PERCENTAGE:MAX_AMOUNT"
Biomes: This section lists the biomes in which the chest's item configuration will apply. Formatting: "- BIOME_NAME"
 
## Example with Expanded Formatting:
 
```YML
ChestSettings:
  Chest1:
    Items:
      - MYCELIUM:30:5
      - RED_MUSHROOM:20:5
      - BROWN_MUSHROOM:20:5
      - RED_MUSHROOM_BLOCK:10:5
      - BROWN_MUSHROOM_BLOCK:10:5
      - MUSHROOM_STEM:10:5
    Biomes:
      - MUSHROOM_FIELDS
```

### The YAML above will be interpreted as follows:
 
- For a chest located in the "MUSHROOM_FIELDS" biome, the code will use the specified item settings to randomly populate the chest's inventory. The percentage chances and amount ranges for each item will be considered when filling the chest.
- MYCELIUM:30:5 - This line specifies an item with the name "MYCELIUM" with a chance of 30% (percentage) of being placed in the chest, and the max amount of this item is 5. The code will randomly select a value between 1 and 5 for the number of MYCELIUM to place in the chest.
 
## Example with Compact Formatting:
 
```YML
ChestSettings:
  Chest1:
    Items: [WOODEN_AXE:20:1, WOODEN_PICKAXE:20:1, CROSSBOW:20:1, ...]
    Biomes: [BADLANDS, BAMBOO_JUNGLE, BIRCH_FOREST, ...]
```
 
### This is a more compact way of formatting the above. Both versions are valid.
 
- How to format item entries: [ITEM_NAME:PERCENTAGE:MAX_AMOUNT]. If there is more than one item, you can do [ITEM_NAME:PERCENTAGE:MAX_AMOUNT, ITEM_NAME:PERCENTAGE:MAX_AMOUNT].
- If you are doing multiple biomes, this is also a more compact way of formatting it.
- How to format biome entries: [BIOME_NAME]. If there is more than one biome, you can do [BIOME_NAME, BIOME_NAME].
 
### Sample of 10 chest from the Overworld, Nether, and End with the included Pre-Configuration.
 
![Sample Chest Generation](https://www.toolsnexus.com/mc/TCO.webp)
 
## SkyGrid World Block Selection
(**ChestSettings.yml** found in the SkygridBlocks folder):
 
```YML
ChestSettings:
  # Example of custom chest items for the biome MUSHROOM_FIELDS
  Chest1:
    Items:
      - MYCELIUM:30:5
      - RED_MUSHROOM:20:5
      - BROWN_MUSHROOM:20:5
      - RED_MUSHROOM_BLOCK:10:5
      - BROWN_MUSHROOM_BLOCK:10:5
      - MUSHROOM_STEM:10:5
    Biomes:
      - MUSHROOM_FIELDS

  # Overworld Biomes
  Chest2:
    Items: [WOODEN_AXE:20:1, WOODEN_PICKAXE:20:1, CROSSBOW:20:1, NAME_TAG:20:1, GOLDEN_APPLE:20:1, ENCHANTED_GOLDEN_APPLE:20:1, APPLE:40:2, OAK_LOG:40:2, DARK_OAK_LOG:40:2, RAIL:40:2, DIAMOND:40:2, STRING:90:5, ARROW:90:5, STICK:90:5, WHEAT:90:5, IRON_INGOT:90:5, TORCH:90:5, ARMADILLO_SCUTE:90:5]
    Biomes: [BADLANDS, BAMBOO_JUNGLE, BIRCH_FOREST, CHERRY_GROVE, DARK_FOREST, DESERT, DRIPSTONE_CAVES, ERODED_BADLANDS, FLOWER_FOREST, FOREST, GROVE, JAGGED_PEAKS, JUNGLE, LUSH_CAVES, MANGROVE_SWAMP, MEADOW, OLD_GROWTH_BIRCH_FOREST, OLD_GROWTH_PINE_TAIGA, OLD_GROWTH_SPRUCE_TAIGA, PLAINS, SAVANNA, SAVANNA_PLATEAU, SPARSE_JUNGLE, STONY_PEAKS, STONY_SHORE, SUNFLOWER_PLAINS, SWAMP, TAIGA, WINDSWEPT_FOREST, WINDSWEPT_GRAVELLY_HILLS, WINDSWEPT_HILLS, WINDSWEPT_SAVANNA, WOODED_BADLANDS, OCEAN, WARM_OCEAN, LUKEWARM_OCEAN, FROZEN_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_OCEAN, DEEP_COLD_OCEAN, COLD_OCEAN, FROZEN_RIVER, RIVER, BEACH, SNOWY_BEACH, SNOWY_PLAINS, SNOWY_SLOPES, SNOWY_TAIGA, FROZEN_PEAKS, ICE_SPIKES, DEEP_DARK]

  # Nether Biomes:
  Chest3:
    Items: [GOLDEN_APPLE:20:1, ENCHANTED_GOLDEN_APPLE:20:1, GOLD_BLOCK:20:1, GOLDEN_AXE:20:1, IRON_BLOCK:20:1, SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE:20:1, NETHERITE_UPGRADE_SMITHING_TEMPLATE:20:1, IRON_SWORD:20:1, GOLDEN_HELMET:20:1, MUSIC_DISC_PIGSTEP:20:1, DIAMOND:40:2, ANCIENT_DEBRIS:40:2, BONE_BLOCK:40:2, GOLDEN_CARROT:40:2, OBSIDIAN:40:2, CRYING_OBSIDIAN:40:2, IRON_INGOT:90:5, GILDED_BLACKSTONE:90:5, CHAIN:90:5, GOLD_INGOT:90:5, MAGMA_CREAM:90:5, GOLD_NUGGET:90:5, SPECTRAL_ARROW:90:5, STRING:90:5, IRON_NUGGET:90:5, ARROW:90:5, COOKED_PORKCHOP:90:5]
    Biomes: [BASALT_DELTAS, NETHER_WASTES, CRIMSON_FOREST, SOUL_SAND_VALLEY, WARPED_FOREST]

  # End Biomes
  Chest4:
    Items: [ELYTRA:20:1, DRAGON_HEAD:20:1, DRAGON_EGG:20:1, END_CRYSTAL:20:1, ENDER_CHEST:20:1, SHULKER_SHELL:40:2, DIAMOND:40:2, END_ROD:90:5, PURPUR_BLOCK:90:5, MAGENTA_STAINED_GLASS:90:5, GOLD_INGOT:90:5, IRON_INGOT:90:5, OBSIDIAN:90:5]
    Biomes: [THE_END, END_BARRENS, END_HIGHLANDS, END_MIDLANDS, SMALL_END_ISLANDS]
```