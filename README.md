 
The SkyGrid Plugin offers access to the latest blocks (1.20.4) and empowers users to fully customize the grids in the Overworld, Nether, and End dimensions. The initial generation process, which typically takes 1-5 minutes, adapts to the server's capabilities. Subsequently, all generation activities occur efficiently in real-time.

This plugin also includes a built-in random teleport command for each dimension, ensuring that players can safely explore their chosen realm. Furthermore, it features an ore generator that can modify stone generation to create various blocks of your choice. While currently configured primarily for ores, the possibilities are virtually endless.

**Important Note**: The '**/reload**' command isn't fully supported at the moment. If you opt to use '**/reload**,' please ensure to re-enable the Skygrid generation afterward by using '**/sgon**.' This step is necessary to maintain seamless functionality.

**Plugin Test Server:** - **IP**: skygrid.live
This server serves as an exclusive testing environment for the latest SkyGrid plugin features. Please note that frequent world resets occur during plugin updates or modifications, and the server is not moderated. Players are encouraged to explore commands and test functionalities rather than building, as the world is periodically cleared. It's important to note that the plugin version on the test server is the latest, featuring some functionalities not yet available in the publicly released version.

![Skygrid](https://www.toolsnexus.com/mc/sg3.jpg)

 
# **Key Features:**

**Block Selection:**
   - Create a unique SkyGrid with a variety of blocks from the [Bukkit Material page](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/material.html).
   - Customize block types and percentages effortlessly.

**Real-time Generation:**
   - SkyGrid generates in real-time as you explore.

**Built-in Fog Feature:**
   - Added a new fog feature accompanied by a texture pack. Admins can enable it using **/fogon** and disable with **/fogoff**. This feature allows lower render distances without disrupting immersion.

**Material Distribution:**
   - Define personalized materials for block placement per world.
   - Fine-tune material distribution percentages for a distinct experience.

**Biome-Specific Grids:**
   - Customize grids in Overworld, Nether, and End with unique blocks for each supported [biome](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/biome.html).

**Ore Generation:**
   - Seamlessly integrated into the gameplay, this feature spawns ores when the player generates stone or cobblestone. 
   - The block types generated, and their percentages can be effortlessly modified to suit your preferences, and work on all worlds where stone can be generated.

**Teleportation Command:**
   - Use **/tpr** for thrilling journeys.
   - Securely teleport to random locations in Overworld, Nether (**/tpr nether**), and End (**/tpr end**).

**Spawner Settings:**
   - The available entities for use can be found at [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html)
   - Fine-tune spawner settings for entities, delay, max nearby entities, and more.
   - Random entity selection based on spawn weights for added unpredictability.
   - Customize spawners for specific biomes at the block level, adding a new layer of customization. For example, if you specify the biome as "DEEP_DARK," you can configure spawners to spawn WARDEN entities exclusively in that biome.

**Chest Loot Customization:**
   - Tailor chest loot settings with precision, specifying items, percentages, and amounts.
   - Tiered loot distribution for added variety, each tier with its own set of items and probabilities.
   - Customize chest loot for specific biomes, allowing for a more immersive and thematic distribution of items based on the environment.

**Pre-Generator Feature:**
   - Built in async pre-generator feature that allows for efficient world generation. This feature is accessible through the **/pregen** command, with customizable parameters for chunks per cycle, cycle delay, and print update delay.
   - Works best on paper servers, on none paper servers the async functionality will not be used. Recommend you go into your paper server paper-global.yml and update these

```yaml
chunk-system:
  gen-parallelism: 'true'
  io-threads: 12
  worker-threads: 12
```
   - io-threads and worker-threads should match the number of threads for you CPU, if left at default only half will be used
   - Usage: /pregen chunksPerCycle PrintUpdate(DelayinMinutes)

**Demonstrations:**
   - Ore generation examples:

---
 
# Installation Instructions:
 
1.**Prepare Server Launch:**
 - Run the following commands within a **.bat** file to initialize the server:

```
@echo off
java -Xms1G -Xmx4G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -XX:+UseNUMA -XX:ParallelGCThreads=6 -XX:ConcGCThreads=6 -XX:MaxGCPauseMillis=200 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar NameOfJar.jar --nogui
pause
```
 - Remember to adjust memory settings to suit your server and replace **NameOfJar.jar** with your server's actual jar file name.

![instrunctions step 1](https://www.toolsnexus.com/mc/1.png)
![instrunctions step 2](https://www.toolsnexus.com/mc/2.png)

2.**Sign the EULA:**
 - Once the EULA is signed, refrain from starting the server yet.

![instrunctions step 3](https://www.toolsnexus.com/mc/3.png)

3.**Download Plugin and Datapack:**
 - Obtain the latest version of the plugin from [SkyGridX](https://cdn.modrinth.com/data/ihjAiP7L/versions/zJuOwswe/SkyGrid.jar) and place it into the auto-generated "plugins" folder.

![instrunctions step 4](https://www.toolsnexus.com/mc/4.png)
![instrunctions step 5](https://www.toolsnexus.com/mc/5.png)

 - Create a new folder named "world" within the server directory.

![instrunctions step 6](https://www.toolsnexus.com/mc/6.png)
![instrunctions step 7](https://www.toolsnexus.com/mc/7.png)

 - Inside "world," create a folder named "datapacks" and add the datapack downloaded from [Skyblock Void Worldgen](https://cdn.modrinth.com/data/x7OPbYhr/versions/B1s7cIeC/skyvoid_worldgen_v1_0_4-MC_1_20.zip).

![instrunctions step 8](https://www.toolsnexus.com/mc/8.png)
![instrunctions step 9](https://www.toolsnexus.com/mc/9.png)
![instrunctions step 10](https://www.toolsnexus.com/mc/10.png)

> Note: Ensure the datapack is placed here before launching the server to generate the world.

4.**Server Launch & World Generation:**
 - Re-run the **.bat** file to start the server and allow all necessary files to generate.
 - Watch for console messages indicating completion:

```
[INFO]: Done (14.909s)! For help, type "help"
[INFO]: Timings Reset
```
 
 - **Do not shut down the server yet.**
 
5.**Spawn Chunk Configuration:**
 - Choose between two options:
  - For clear spawn chunks without Skygrid, perform a **save-all** command, then reboot the server. Enable Skygrid generation **sgon** afterward.
  - To generate Skygrid in spawn chunks, enable the plugin **sgon** right after the console messages appear. Connect to the server to verify completion ([INFO]: First boot generation complete).
 
![instrunctions step 11](https://www.toolsnexus.com/mc/11.png)
 
6.**Enjoy the SkyGrid World:**
 - Once the spawn chunk configuration is set as desired, players can join the server and delve into the adventurous SkyGrid world.
  
 
## Biome-Specific Block Generation

## How to Use:
1. Update your plugin to the latest version.
2. Configure Biome-Specific Blocks:
3. Open the text files for each world dimension (e.g., world.txt, world_nether.txt, world_the_end.txt) located in the plugin's folder.
 4. Specify block materials and their respective percentages for each biome in your world.

## For example:
```
DIRT:20 
STONE:20 
GRASS_BLOCK:20 
-OCEAN- 
SAND:50 
WATER:50 
-DESERT- 
SAND:100​ 
```
 
- In this example, when generating chunks in the "**OCEAN**" biome, there's a 50% chance of generating "SAND" blocks and a 50% chance of generating "WATER" blocks. In the "**DESERT**" biome, only "SAND" blocks will be generated. For the blocks above they will be generated on all biomes not specified in the file for that dimension. So, if you only want to have 1 custom biome with the rest having another variety of blocks you can do that.
- Biomes are signified by the -**BIOME_NAME**-, everything under the biome header will generate only in that biome, unless there is another biome under it like in the example above.
 
## Here is an example with multiple biomes,
```
-OCEAN,COLD_OCEAN,DEEP_COLD_OCEAN,DEEP_FROZEN_OCEAN,DEEP_LUKEWARM_OCEAN,DEEP_OCEAN,FROZEN_OCEAN,LUKEWARM_OCEAN-
SAND:80
SANDSTONE:9.0
SEA_PICKLE:1.0
TUBE_CORAL:1.0
TUBE_CORAL_FAN:1.0
HORN_CORAL:1.0
HORN_CORAL_FAN:1.0
FIRE_CORAL:1.0
FIRE_CORAL_FAN:1.0
BUBBLE_CORAL:1.0
BUBBLE_CORAL_FAN:1.0
BRAIN_CORAL:1.0
BRAIN_CORAL_FAN:1.0
-WARM_OCEAN-
SAND:80
SANDSTONE:20
```

- Supported biomes can be found here: [Biomes](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html)

![zoomed out biome support](https://www.toolsnexus.com/mc/out.jpg)
 
 
![zoomed in biome support](https://www.toolsnexus.com/mc/in.jpg)
 
 
## Mob Spawner Settings

## How to Use:
1. Update your plugin to the latest version.
2. Configure Spawners: Open the SpawnerSettings.yml file located in the plugin's folder.
3. Customize Settings: Define the settings for each spawner in the configuration file, specifying the entity, delay, max nearby entities, and more. You can use either of the following YML formatting styles, and they will both be read the same by the YML parser:

```YML
# The available entities for use can be found at https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
# The available biomes can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html
 
# Entities - Define entities and their chances of being chosen for the spawner. Formatting "- DOLPHIN:33.33"
# Biomes -  List the biomes where this spawner will spawn. Formatting "- BEACH"
# Delay: - Set the spawner's delay. If set to -1, the delay will be randomized (randomly choose a delay between min and max spawn delays).
# MaxNearbyEntities - Set the maximum nearby entities (of the same type) allowed. Default value is 16.
# MaxSpawnDelay - The maximum spawn delay amount (in ticks). This value must be greater than 0 and less than or equal to MaxSpawnDelay. Default value is 800 ticks.
# MinSpawnDelay - The minimum spawn delay amount (in ticks). Default value is 200 ticks.
# PlayerRange - Set the range for player activation (given that there are players online). Default value is 16.
# SpawnCount - Set how many mobs attempt to spawn. Default value is 4.
# SpawnRange - Set the radius around which the spawner will attempt to spawn mobs in. Default value is 4.
```

## Example with Expanded Formatting:

```YML
SpawnerSettings:
  Spawner1:
    Entities:
      - SKELETON:100
      - ZOMBIE:100
    Biomes:
      - FOREST
      - PLAINS
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4
```
 
## Example with Compact Formatting:

```YML
SpawnerSettings:
  Spawner1:
    Entities: [SKELETON:100, ZOMBIE:100]
    Biomes: [FOREST, PLAINS]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4
```
  
In these examples, "Spawner1" can spawn both SKELETON and ZOMBIE entities with equal chances.
  
**Random Entity Selection**: Create a list of entities with associated spawn weights to add unpredictability to your spawners.
  
```YML
Entities: [SKELETON:10, ZOMBIE:10, CREEPER:5]
```
 
In this example, "SKELETON" and "ZOMBIE" have higher chances of being chosen for the spawner than "CREEPER."

 
  
## Chest Settings
  
 
## How to Use:
1. Update your plugin to the latest version.
2. **Configure Chests**: Open the ChestSettings.yml file located in the plugin's SkygridBlocks folder.
3. **Customize Loot Settings**: Define the loot settings for each chest in the configuration file. Use the provided examples as a guide.

```YML
# The available items for use can be found at https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
# The available biomes can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html

# In YAML, identifiers or keys (such as "Chest1" in your example) are subject to certain rules.

# - Do's -
# Alphanumeric Characters: Identifiers can contain letters (both uppercase and lowercase) and numbers. For example, "Chest1" is a valid identifier.
# Underscores: You can use underscores (_) in identifiers. For instance, "my_chest" is a valid identifier.
# Hyphens in Quoted Strings: If an identifier contains special characters or spaces, you can enclose it in single or double quotes. For example, "'Special Chest'" or "\"Special Chest\"" are valid.

# - Don'ts -
# Special Characters: Avoid using special characters like !, @, #, $, %, etc., in identifiers without enclosing them in quotes. For example, "Chest@123" should be written as "'Chest@123'" if you want to use special characters.
# Leading Spaces: Identifiers should not have leading spaces. For instance, " MyChest" with a space at the beginning is not a valid identifier.
# Trailing Spaces: Similarly, identifiers should not have trailing spaces. "MyChest " with a space at the end is not valid.
# Spaces in the Middle: While you can use spaces within identifiers if they are enclosed in quotes, it's generally a good practice to avoid spaces within identifiers for clarity. For example, "My Chest" is valid, but "MyChest" is more commonly used.
# Reserved Keywords: Avoid using YAML reserved keywords as identifiers. These keywords have special meaning in YAML and should not be used as keys. For example, "true," "false," and "null" are reserved keywords.
# Case Sensitivity: YAML is case-sensitive. "myChest" and "MyChest" would be considered as two different identifiers.

# ChestSettings: This is the top-level section of the YAML configuration and acts as the main container for all chest settings.
# Chest1: This represents the first chest configuration, which we'll be using as an example. The code will look for a chest configuration with the key "Chest1." The key can be whatever you want; it serves as an identifier.
# Items: Under the "Chest1" configuration, there is an "Items" section. The code will process the items listed here to determine what should be placed in the chest's inventory. Formatting: "- ITEM_NAME:PERCENTAGE:MAX_AMOUNT"
# Biomes: This section lists the biomes in which the chest's item configuration will apply. Formatting: "- BIOME_NAME"
```

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

# The YAML above will be interpreted as follows:

# For a chest located in the "MUSHROOM_FIELDS" biome, the code will use the specified item settings to randomly populate the chest's inventory. The percentage chances and amount ranges for each item will be considered when filling the chest.
# MYCELIUM:30:5 - This line specifies an item with the name "MYCELIUM" with a chance of 30% (percentage) of being placed in the chest, and the max amount of this item is 5. The code will randomly select a value between 1 and 5 for the number of MYCELIUM to place in the chest.
```
 
## Example with Compact Formatting:

```YML
ChestSettings:
  Chest1:
    Items: [WOODEN_AXE:20:1, WOODEN_PICKAXE:20:1, CROSSBOW:20:1, ...]
    Biomes: [BADLANDS, BAMBOO_JUNGLE, BIRCH_FOREST, ...]

# This is a more compact way of formatting the above. Both versions are valid.
# How to format item entries: [ITEM_NAME:PERCENTAGE:MAX_AMOUNT]. If there is more than one item, you can do [ITEM_NAME:PERCENTAGE:MAX_AMOUNT, ITEM_NAME:PERCENTAGE:MAX_AMOUNT].
# If you are doing multiple biomes, this is also a more compact way of formatting it.
# How to format biome entries: [BIOME_NAME]. If there is more than one biome, you can do [BIOME_NAME, BIOME_NAME].
```
  
Sample of 10 chest from the Overworld, Nether, and End with the included Pre-Configuration.
  
![Sample Chest Generation](https://www.toolsnexus.com/mc/TCO.webp)

 
# A few additional notes to consider:
 
- The SkyGrid plugin requires a Bukkit or Spigot server for optimal performance and functionality. Before adding the plugin to your server, ensure that you have the latest version of Bukkit/Spigot/Paper installed.
 
- For a seamless SkyGrid generation experience, it is recommended to follow the steps in the Installation Instructions.
 
 
# Commands:
 
- **/sgon** (turns on the SkyGrid generation)
- **/tpr** (random teleport in the Overworld)
- **/tpr nether** (random teleport in the Nether)
- **/tpr end** (random teleport in the End)
- **/fogon** (turns fog on)
- **/fogoff** (turns fog off)
- **/gclogson** (turns on growth control logs)
- **/gclogsoff** (turns off growth control logs)
 
# Config:
 
## Ore Generator Settings
(**ores.txt** found in the OreGenBlock folder)
 
```YML
# Ore Replacement Chances

# This file is used to specify the chances of different ores being chosen for replacement
# when lava or water generates stone blocks in the world. The stone generate in the world can be replaced by any block you want as long as its in the bukkit materials list https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html

# Format: Material_Name:Percentage
# The percentage represents the chance (in whole numbers) of the ore being chosen
# for replacement out of the total replacements when lava or water generates stone.

# Each line represents one ore with its corresponding replacement chance.
# Lines starting with '#' are considered comments and will be ignored by the code.

# Example:
# DIAMOND_ORE:5
# This means that there is a 5% chance of a diamond ore block being chosen
# when lava or water generates a stone block.

# The total sum of percentages should be less than or equal to 100.
# If the sum exceeds 100, the probabilities will be scaled down proportionally
# to fit within the 0-100 range.

# For example, if the total sum of percentages exceeds 100 as follows:
# DIAMOND_ORE:40
# COAL_ORE:60
# IRON_ORE:30
# GOLD_ORE:20

# The total sum is 40 + 60 + 30 + 20 = 150, which exceeds 100.
# To fit within the 0-100 range, the probabilities will be scaled down proportionally:
# DIAMOND_ORE: 40 * (100 / 150) = 26 (approximately 27)
# COAL_ORE: 60 * (100 / 150) = 40
# IRON_ORE: 30 * (100 / 150) = 20
# GOLD_ORE: 20 * (100 / 150) = 13 (approximately 13)

# The adjusted probabilities now add up to 26 + 40 + 20 + 13 = 99, which is within the 0-100 range.


#For if the block generated though water and lava is stone
STONE:
  STONE: 98
  COAL_ORE: 0.3
  COPPER_ORE: 0.4
  DIAMOND_ORE: 0.1
  EMERALD_ORE: 0.1
  GOLD_ORE: 0.2
  IRON_ORE: 0.4
  LAPIS_ORE: 0.2
  REDSTONE_ORE: 0.3

#For if the block generated though water and lava is cobblestone
COBBLESTONE:
  DEEPSLATE: 98
  DEEPSLATE_COAL_ORE: 0.3
  DEEPSLATE_COPPER_ORE: 0.4
  DEEPSLATE_DIAMOND_ORE: 0.1
  DEEPSLATE_EMERALD_ORE: 0.1
  DEEPSLATE_GOLD_ORE: 0.2
  DEEPSLATE_IRON_ORE: 0.4
  DEEPSLATE_LAPIS_ORE: 0.2
  DEEPSLATE_REDSTONE_ORE: 0.3
```
 
## SkyGrid World Block Selection
(found in the SkygridBlocks folder):
 
### Overworld (**world.txt**):
 
```
#SkyGrid World Block Selection Guide:
#
#- Each line in the materials file corresponds to a block and its chance of appearing.
#- Format: BlockID:Percentage (use block names or IDs).
#- Example: "STONE:50" means a 50% chance of Stone appearing with in the 32 chunk distribution.
#- You can include as many blocks as you want, and their percentages can be anything.
#- The total percentage doesn't need to add up to 100%.
#- If no percentage is specified for any of the blocks the plugin will distribute the percentage among all the blocks missing a percentage
#- If you specify the percentage but for only some of the blocks and it doesn't add up to 100% the plugin will distribute the remainder to the rest of the blocks missing a percentage.
#- These percentages should be interpreted as a probability distribution for selecting the blocks. So, for the END_STONE:80, on average, you'd expect END_STONE to be selected 80% of the time in your block distribution.
#- You can use any of the materials in the bukkit Material page here https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
#
#Example (world.txt):
# 50% chance of Stone
#STONE:50
# 25% chance of Grass
#GRASS:25
# 24.999% chance of Iron Ore
#IRON_ORE:24.999
# 0.001% chance of Diamond Ore
#DIAMOND_ORE:0.001
#
#
ACACIA_LEAVES:0.972
ACACIA_LOG:0.972
AMETHYST_BLOCK:0.972
ANDESITE:0.972
AZALEA:0.972
AZALEA_LEAVES:0.972
BAMBOO:0.972
BEE_NEST:0.972
BIRCH_LEAVES:0.972
BIRCH_LOG:0.972
BLUE_ICE:0.972
BONE_BLOCK:0.972
BOOKSHELF:0.972
BRAIN_CORAL_BLOCK:0.972
BROWN_MUSHROOM_BLOCK:0.972
BUBBLE_CORAL_BLOCK:0.972
BUDDING_AMETHYST:0.972
CACTUS:0.972
CALCITE:0.972
CHERRY_LEAVES:0.972
CHERRY_LOG:0.972
CLAY:0.972
COAL_ORE:2.972
COARSE_DIRT:0.972
COBBLED_DEEPSLATE:0.972
COBBLESTONE:0.972
COPPER_ORE:0.972
CRYING_OBSIDIAN:0.972
CUT_SANDSTONE:0.972
DARK_OAK_LEAVES:0.972
DARK_OAK_LOG:0.972
DARK_PRISMARINE:0.972
DEEPSLATE:6.722
DIAMOND_ORE:0.001
DIORITE:0.972
DIRT:0.972
DRIPSTONE_BLOCK:0.972
EMERALD_ORE:0.001
FIRE_CORAL_BLOCK:0.972
GLASS:0.972
GOLD_ORE:0.972
GRANITE:0.972
GRASS_BLOCK:7.972
GRAVEL:0.972
HAY_BLOCK:0.972
HORN_CORAL_BLOCK:0.972
IRON_ORE:3
JUNGLE_LEAVES:0.972
JUNGLE_LOG:0.972
LAPIS_ORE:0.972
LARGE_AMETHYST_BUD:0.972
LAVA_CAULDRON:0.972
MANGROVE_LEAVES:0.972
MANGROVE_LOG:0.972
MELON:0.972
MOSSY_COBBLESTONE:0.972
MOSS_BLOCK:0.972
MUD:0.972
MUDDY_MANGROVE_ROOTS:0.972
MYCELIUM:0.972
OAK_LEAVES:0.972
OAK_LOG:0.972
OBSIDIAN:0.972
PACKED_ICE:0.972
PACKED_MUD:0.972
PODZOL:0.972
POINTED_DRIPSTONE:0.972
PRISMARINE:0.972
PUMPKIN:0.972
RAW_IRON_BLOCK:1.943
REDSTONE_ORE:1.943
RED_MUSHROOM_BLOCK:0.972
RED_SAND:0.972
ROOTED_DIRT:0.972
SAND:6.722
SANDSTONE:0.972
SEA_LANTERN:0.972
SEA_PICKLE:0.972
SNOW_BLOCK:0.972
STONE:6.5
SUGAR_CANE:0.972
SUSPICIOUS_GRAVEL:0.972
SUSPICIOUS_SAND:0.972
TERRACOTTA:0.972
TUBE_CORAL_BLOCK:0.972
TUFF:0.972
WET_SPONGE:0.972
```
 
### Nether (**world_nether.txt**):
 
```
#SkyGrid World Block Selection Guide:
#
#- Each line in the materials file corresponds to a block and its chance of appearing.
#- Format: BlockID:Percentage (use block names or IDs).
#- Example: "STONE:50" means a 50% chance of Stone appearing with in the 32 chunk distribution.
#- You can include as many blocks as you want, and their percentages can be anything.
#- The total percentage doesn't need to add up to 100%.
#- If no percentage is specified for any of the blocks the plugin will distribute the percentage among all the blocks missing a percentage
#- If you specify the percentage but for only some of the blocks and it doesn't add up to 100% the plugin will distribute the remainder to the rest of the blocks missing a percentage.
#- These percentages should be interpreted as a probability distribution for selecting the blocks. So, for the END_STONE:80, on average, you'd expect END_STONE to be selected 80% of the time in your block distribution.
#- You can use any of the materials in the bukkit Material page here https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
#
#Example (world.txt):
# 50% chance of Stone
#STONE:50
# 25% chance of Grass
#GRASS:25
# 24.999% chance of Iron Ore
#IRON_ORE:24.999
# 0.001% chance of Diamond Ore
#DIAMOND_ORE:0.001
#
#
NETHERRACK:13
SOUL_SAND:3
NETHER_QUARTZ_ORE:3
GLOWSTONE:5
NETHER_BRICKS:2
BASALT:13
BLACKSTONE:13
MAGMA_BLOCK:2
CRIMSON_NYLIUM:3
CRIMSON_STEM:3
NETHER_WART_BLOCK:3
SHROOMLIGHT:2
NETHER_GOLD_ORE:2
ANCIENT_DEBRIS:0.001
GRAVEL:2
SOUL_SOIL:2
BONE_BLOCK:2
WARPED_NYLIUM:3
WARPED_STEM:3
WARPED_WART_BLOCK:3
LAVA_CAULDRON:3
OCHRE_FROGLIGHT:2
PEARLESCENT_FROGLIGHT:2
VERDANT_FROGLIGHT:1
```
 
### End (**world_the_end.txt**):
 
```
#SkyGrid World Block Selection Guide:
#
#- Each line in the materials file corresponds to a block and its chance of appearing.
#- Format: BlockID:Percentage (use block names or IDs).
#- Example: "STONE:50" means a 50% chance of Stone appearing with in the 32 chunk distribution.
#- You can include as many blocks as you want, and their percentages can be anything.
#- The total percentage doesn't need to add up to 100%.
#- If no percentage is specified for any of the blocks the plugin will distribute the percentage among all the blocks missing a percentage
#- If you specify the percentage but for only some of the blocks and it doesn't add up to 100% the plugin will distribute the remainder to the rest of the blocks missing a percentage.
#- These percentages should be interpreted as a probability distribution for selecting the blocks. So, for the END_STONE:80, on average, you'd expect END_STONE to be selected 80% of the time in your block distribution.
#- You can use any of the materials in the bukkit Material page here https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
#
#Example (world.txt):
# 50% chance of Stone
#STONE:50
# 25% chance of Grass
#GRASS:25
# 24.999% chance of Iron Ore
#IRON_ORE:24.999
# 0.001% chance of Diamond Ore
#DIAMOND_ORE:0.001
#
#
END_STONE:40
PURPUR_BLOCK:25
END_STONE_BRICKS:15
OBSIDIAN:12
CHORUS_FLOWER:2
END_ROD:1
```
 
