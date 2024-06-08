 
The SkyGrid Plugin offers access to the latest blocks (1.20.6) and empowers users to fully customize the grids in the Overworld, Nether, and End dimensions. The initial generation process, which typically takes 1-5 minutes, adapts to the server's capabilities. Subsequently, all generation activities occur efficiently in real-time.

This plugin also includes a built-in random teleport command for each dimension, ensuring that players can safely explore their chosen realm. Furthermore, it features an ore generator that can modify stone generation to create various blocks of your choice. While currently configured primarily for ores, the possibilities are virtually endless.

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
   - Fog feature accompanied by a texture pack. Admins can enable it using '/fogon' and disable with '/fogoff'. This feature allows lower render distances without disrupting immersion.
   - Feature is included in the settings file if you would like to enable it by default, it will come as disabled by default.

**Material Distribution:**
   - Define personalized materials for block placement per world.
   - Fine-tune material distribution percentages for a distinct experience.

**Biome-Specific Grids:**
   - Customize grids in Overworld, Nether, and End with unique blocks at the [biome](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/biome.html) level.

**Ore Generation:**
   - Seamlessly integrated into the gameplay, this feature replaced generated stone or cobblestone with any blocks you want.
   - The block types generated, and their percentages can be effortlessly modified to suit your preferences in the ores.yml file inside the OreGenBlock folder, and work on all worlds where stone or cobblestone can be generated.

**Teleportation Command:**
   - Securely teleport to random locations in
     - Overworld (/**tpr overworld**) or (**/tpro**)
     - Nether (**/tpr nether**) or (**/tprn**)
     - End (**/tpr end**) or (**/tpre**).
   - Range can be customized in the settings
   - Settings also include cooldown for per world command reuse
   - All have their own individualize permissions

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
   - Built in async pre-generator feature that allows for efficient world generation. This feature is accessible through the **/pregen** command, with customizable parameters for chunks per cycle and print update delay.
   - Works best on paper servers, on none paper servers the async functionality will not be used. Recommend you go into your paper server paper-global.yml and update these

```yaml
chunk-system:
  gen-parallelism: default
  io-threads: 12
  worker-threads: 12
```
   - Adjust **io-threads** and **worker-threads** to match your CPU’s thread count. Default settings utilize only half.
     - Usage: /pregen chunksPerCycle PrintUpdate(DelayinMinutes)
   - For **chunksPerCycle** It’s recommended to match the number of threads your system has. For example, 12 threads yielded 108-112 chunks per second on a 5600x CPU, depending on server activity and other system tasks.
   - Increasing **chunksPerCycle** if you are not already utilizing 100% CPU can take that further, I was able to bring mine up to 24 **chunksPerCycle** yielded 128-134 chunks per second.

**Demonstrations:**
   - Ore generation examples:
![instrunctions step 1](https://i3.ytimg.com/vi/UrzhCaiLKyI/maxresdefault.jpg)
![instrunctions step 1](https://i3.ytimg.com/vi/NMkvj6UvmLg/maxresdefault.jpg)

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
 - Obtain the latest version of the plugin from [SkyGridX](https://modrinth.com/plugin/skygridx/versions) and place it into the auto-generated "plugins" folder.

![instrunctions step 4](https://www.toolsnexus.com/mc/4.png)
![instrunctions step 5](https://www.toolsnexus.com/mc/5.png)

 - Create a new folder named "world" within the server directory.

![instrunctions step 6](https://www.toolsnexus.com/mc/6.png)
![instrunctions step 7](https://www.toolsnexus.com/mc/7.png)

 - Inside "world," create a folder named "datapacks" and add the datapack downloaded from [Skyblock Void Worldgen](https://modrinth.com/datapack/skyblock-void-worldgen).

![instrunctions step 8](https://www.toolsnexus.com/mc/8.png)
![instrunctions step 9](https://www.toolsnexus.com/mc/9.png)
![instrunctions step 10](https://www.toolsnexus.com/mc/10.png)

> Note: Ensure the datapack is placed here before launching the server to generate the world.

4.**Server Launch & World Generation:**
 - Re-run the **.bat** file to start the server and allow all necessary files to generate.
 - Watch for console messages indicating you to wait.
 
![instrunctions step 11](https://www.toolsnexus.com/mc/pl.png)
 
5.**Enjoy the SkyGrid World:**
 - Connect to the server to verify completion after ([INFO]: Chunks have been loaded. You can now connect!).

![instrunctions step 12](https://www.toolsnexus.com/mc/comp.png)
  
 
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
 
- **/tpr [world]**: Random teleport in the Overworld, Nether, or End. Using `/tpr` with no arguments still defaults to `overworld`.
- **/tpro**: Random teleport in the Overworld.
- **/tprn**: Random teleport in the Nether.
- **/tpre**: Random teleport in the End.
- **/fogon** (turns fog on)
- **/fogoff** (turns fog off)
- **/eclogson** (Enable Event Control logging)
- **/eclogsoff** (Disable Event Control logging)
- **/pregen** (Pregenerates the overworld, nether and end)
- **/pregenoff** (Turns of pregeneration process)
- **/patch** (Patch files to update materials and entities to newest version, if you update the plugin from an old version)

## Permissions:
- `sg.tpr`: Allows teleportation to Overworld, Nether, and End using the `/tpr [world]` command. (Default all)
- `sg.tpr.overworld`: Grants permission to use the `/tpro` command for teleportation in the Overworld. (Default all)
- `sg.tpr.nether`: Grants permission to use the `/tprn` command for teleportation in the Nether. (Default all)
- `sg.tpr.end`: Enables usage of the `/tpre` command for teleportation in the End. (Default all)
- `sg.tpr.*`: Provides access to all teleportation commands. (Default OP)
- `sg.fogon`: Allows enabling fog using the `/fogon` command. (Default OP)
- `sg.fogoff`: Allows disabling fog with the `/fogoff` command. (Default OP)
- `sg.eclogson`: Grants permission to enable Event Control logging using `/eclogson`. (Default OP)
- `sg.eclogsoff`: Grants permission to disable Event Control logging using `/eclogsoff`. (Default OP)
- `sg.pregen`: Grants permission to use the pre-generation command with customizable parameters. (Default OP)
- `sg.pregenoff`: Grants permission to disable pre-generation using the `/pregenoff` command. (Default OP)
- `sg.patch`: Allows patching files to update materials and entities to another version. (Default OP)
- `sg.regen`: Grants permission to regenerate all loaded chunks using the `/regen` command. (Default OP)
- `sg.*`: Provides access to all SkyGrid commands. (Default OP)
 
# Config:

## Plugin Settings
(**settings.yml** found in the Skygrid folder)
 
```YML
# Configuration

# Generator settings
generator:
  # Delay in ticks before processing chunks after chunk load event.
  # A shorter delay increases server load, while a longer delay may affect player immersion.
  processDelay: 10
  
  # Max and Min height for blocks placed in the Grids for all Environments.
  # Keep within increments of 4 starting from the current min or max.
  # i.e. 0, 4, 8, 12, 16 ... or 128, 124, 120, 116, 112 ...
  # Exceeding max heights for worlds will result in errors.
  
  # Nether settings (min 0, max 128)
  nether:
    minY: 0
    maxY: 128

  # End settings (min 0, max 128)
  end:
    minY: 0
    maxY: 128

  # Normal world settings (min -64, max 256)
  normal:
    minY: -64
    maxY: 64

  # Default settings for unspecified environments (min and max depend on your custom environment)
  default:
    minY: 0
    maxY: 128

# TPR Command settings
tprCommand:
  # Randomly teleports the player using /tpr when they first join.
  # Set to true to enable.
  onFirstJoin: false
  
  # The delay in seconds to prevent players from using the /tpr command back-to-back.
  # Players must wait for this duration before they can use the /tpr command again.
  b2bDelay: 10
  
  # Delay in seconds before teleporting in the overworld
  tprDelay: 30
  
  # Delay in seconds before teleporting in the nether
  tprNetherDelay: 30
  
  # Delay in seconds before teleporting in the end
  tprEndDelay: 30
  
  # Maximum X coordinate for random teleport (max 29999983)
  maxX: 29999983
  
  # Maximum Z coordinate for random teleport (max 29999983)
  maxZ: 29999983
  
  # Minimum X coordinate for random teleport (max -29999983)
  minX: -29999983
  
  # Minimum Z coordinate for random teleport (max -29999983)
  minZ: -29999983
  
  # Default Y coordinate for teleport destination (min -64, max 256, try to get it somewhere in between all your environments)
  destinationY: 64
  
  # These are materials that the tpr command considers dangerous and will prevent players from being placed on them when /tpr is used.
  DANGEROUSBLOCKS:
    Materials:
      - AIR
      - ALLIUM
      - AZURE_BLUET
      - BEETROOTS
      - BIG_DRIPLEAF
      - BLUE_ORCHID
      - BROWN_MUSHROOM
      - CACTUS
      - CAMPFIRE
      - CARROTS
      - CAVE_VINES_PLANT
      - COBWEB
      - CORNFLOWER
      - CRIMSON_FUNGUS
      - CRIMSON_ROOTS
      - DANDELION
      - DEAD_BUSH
      - FERN
      - FIRE
      - GLOW_LICHEN
      - HANGING_ROOTS
      - KELP
      - KELP_PLANT
      - LARGE_FERN
      - LAVA
      - LAVA_CAULDRON
      - LILAC
      - LILY_OF_THE_VALLEY
      - MAGMA_BLOCK
      - MELON_STEM
      - NETHER_SPROUTS
      - NETHER_WART
      - ORANGE_TULIP
      - OXEYE_DAISY
      - PEONY
      - PINK_TULIP
      - PITCHER_PLANT
      - POINTED_DRIPSTONE
      - POPPY
      - POTATOES
      - POWDER_SNOW
      - PUMPKIN_STEM
      - RED_MUSHROOM
      - RED_TULIP
      - ROSE_BUSH
      - SCULK_VEIN
      - SEAGRASS
      - SHORT_GRASS
      - SMALL_DRIPLEAF
      - SNOW
      - SOUL_FIRE
      - SPORE_BLOSSOM
      - SUGAR_CANE
      - SUNFLOWER
      - SWEET_BERRY_BUSH
      - TALL_GRASS
      - TALL_SEAGRASS
      - TORCHFLOWER
      - TORCHFLOWER_CROP
      - TWISTING_VINES
      - VINE
      - VOID_AIR
      - WARPED_FUNGUS
      - WARPED_ROOTS
      - WATER
      - WEEPING_VINES
      - WHEAT
      - WHITE_TULIP
      - WITHER_ROSE

# Fog settings
fog:
  # Auto-enable fog setting
  # Set to true to automatically enable fog when the plugin starts.
  autoEnable: false
  
# Event Control settings
# Other events not included here that we still monitor are ;
# onBlockIgnite (Prevents lava from causing fires to near by flammable grid blocks)
# BlockFadeEvent (Prevents ice from melting if it's in the grid and fire from burning out)
# BlockFromToEvent (Prevents lava and water from flowing due to server overload)
EventControl:
  # Used for preventing BlockGrowEvents (blocks that grow from one block to another)
  GROWTH_BLOCKS:
    Materials:
      - KELP_PLANT
      - SUGAR_CANE
      - CACTUS
      - BAMBOO_SAPLING
      - ACACIA_SAPLING
      - CHERRY_SAPLING
      - DARK_OAK_SAPLING
      - JUNGLE_SAPLING
      - OAK_SAPLING
      - SPRUCE_SAPLING
      - BIRCH_SAPLING
      - JUNGLE_SAPLING
      - MANGROVE_PROPAGULE
      - TWISTING_VINES_PLANT

  # This list is for blocks that you want to monitor the following events.
  # BlockFormEvent is used to prevent block updates that mess up the grid in snow biomes(snow forming on top of blocks)
  # EntityChangeBlockEvents prevent gravity-affected blocks from randomly falling due to overload
  GRAVITY_AFFECTED_BLOCKS:
    Materials:
      - SAND
      - RED_SAND
      - GRAVEL
      - KELP_PLANT
      - ANVIL
      - SUGAR_CANE
      - CACTUS
      - SUSPICIOUS_SAND
      - SUSPICIOUS_GRAVEL
      - DRAGON_EGG
      - BLACK_CONCRETE_POWDER
      - BLUE_CONCRETE_POWDER
      - BROWN_CONCRETE_POWDER
      - CYAN_CONCRETE_POWDER
      - GRAY_CONCRETE_POWDER
      - GREEN_CONCRETE_POWDER
      - LIGHT_BLUE_CONCRETE_POWDER
      - LIGHT_GRAY_CONCRETE_POWDER
      - LIME_CONCRETE_POWDER
      - MAGENTA_CONCRETE_POWDER
      - ORANGE_CONCRETE_POWDER
      - PINK_CONCRETE_POWDER
      - PURPLE_CONCRETE_POWDER
      - RED_CONCRETE_POWDER
      - WHITE_CONCRETE_POWDER
      - YELLOW_CONCRETE_POWDER

```

## Ore Generator Settings
(**ores.yml** found in the OreGenBlock folder)
 
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
(**ChestSettings.yml** found in the SkygridBlocks folder):
 
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


# This is a more compact way of formatting the above. Both versions are valid.
# How to format item entries: [ITEM_NAME:PERCENTAGE:MAX_AMOUNT]. If there is more than one item, you can do [ITEM_NAME:PERCENTAGE:MAX_AMOUNT, ITEM_NAME:PERCENTAGE:MAX_AMOUNT].
# If you are doing multiple biomes, this is also a more compact way of formatting it.
# How to format biome entries: [BIOME_NAME]. If there is more than one biome, you can do [BIOME_NAME, BIOME_NAME].

#Overworld Biomes

  Chest2:
    Items: [WOODEN_AXE:20:1, WOODEN_PICKAXE:20:1, CROSSBOW:20:1, NAME_TAG:20:1, GOLDEN_APPLE:20:1, ENCHANTED_GOLDEN_APPLE:20:1, APPLE:40:2, OAK_LOG:40:2, DARK_OAK_LOG:40:2, RAIL:40:2, DIAMOND:40:2, STRING:90:5, ARROW:90:5, STICK:90:5, WHEAT:90:5, IRON_INGOT:90:5, TORCH:90:5, ARMADILLO_SCUTE:90:5]
    Biomes: [BADLANDS, BAMBOO_JUNGLE, BIRCH_FOREST, CHERRY_GROVE, DARK_FOREST, DESERT, DRIPSTONE_CAVES, ERODED_BADLANDS, FLOWER_FOREST, FOREST, GROVE, JAGGED_PEAKS, JUNGLE, LUSH_CAVES, MANGROVE_SWAMP, MEADOW, OLD_GROWTH_BIRCH_FOREST, OLD_GROWTH_PINE_TAIGA, OLD_GROWTH_SPRUCE_TAIGA, PLAINS, SAVANNA, SAVANNA_PLATEAU, SPARSE_JUNGLE, STONY_PEAKS, STONY_SHORE, SUNFLOWER_PLAINS, SWAMP, TAIGA, WINDSWEPT_FOREST, WINDSWEPT_GRAVELLY_HILLS, WINDSWEPT_HILLS, WINDSWEPT_SAVANNA, WOODED_BADLANDS, OCEAN, WARM_OCEAN, LUKEWARM_OCEAN, FROZEN_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_OCEAN, DEEP_COLD_OCEAN, COLD_OCEAN, FROZEN_RIVER, RIVER, BEACH, SNOWY_BEACH, SNOWY_PLAINS, SNOWY_SLOPES, SNOWY_TAIGA, FROZEN_PEAKS, ICE_SPIKES, DEEP_DARK]


# Nether Biomes:

  Chest3:
    Items: [GOLDEN_APPLE:20:1, ENCHANTED_GOLDEN_APPLE:20:1, GOLD_BLOCK:20:1, GOLDEN_AXE:20:1, IRON_BLOCK:20:1, SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE:20:1, NETHERITE_UPGRADE_SMITHING_TEMPLATE:20:1, IRON_SWORD:20:1, GOLDEN_HELMET:20:1, MUSIC_DISC_PIGSTEP:20:1, DIAMOND:40:2, ANCIENT_DEBRIS:40:2, BONE_BLOCK:40:2, GOLDEN_CARROT:40:2, OBSIDIAN:40:2, CRYING_OBSIDIAN:40:2, IRON_INGOT:90:5, GILDED_BLACKSTONE:90:5, CHAIN:90:5, GOLD_INGOT:90:5, MAGMA_CREAM:90:5, GOLD_NUGGET:90:5, SPECTRAL_ARROW:90:5, STRING:90:5, IRON_NUGGET:90:5, ARROW:90:5, COOKED_PORKCHOP:90:5]
    Biomes: [BASALT_DELTAS, NETHER_WASTES, CRIMSON_FOREST, SOUL_SAND_VALLEY, WARPED_FOREST]


#End Biomes

  Chest4:
    Items: [ELYTRA:20:1, DRAGON_HEAD:20:1, DRAGON_EGG:20:1, END_CRYSTAL:20:1, ENDER_CHEST:20:1, SHULKER_SHELL:40:2, DIAMOND:40:2, END_ROD:90:5, PURPUR_BLOCK:90:5, MAGENTA_STAINED_GLASS:90:5, GOLD_INGOT:90:5, IRON_INGOT:90:5, OBSIDIAN:90:5]
    Biomes: [THE_END, END_BARRENS, END_HIGHLANDS, END_MIDLANDS, SMALL_END_ISLANDS]
```

### Spawner Settings:
(**SpawnerSettings.yml** found in the SkygridBlocks folder)
 
```YML
# The available entities for use can be found at https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
# The available biomes can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html


# Entities - Define entities and their spawn percentages. Formatting "- DOLPHIN:33.33"
# Biomes -  List the biomes where this spawner will spawn. Formatting "- BEACH"
# Delay: - Set the spawner's delay. If set to -1, the delay will be randomized (randomly choose a delay between min and max spawn delays).
# MaxNearbyEntities - Set the maximum nearby entities (of the same type) allowed. Default value is 16.
# MaxSpawnDelay - The maximum spawn delay amount (in ticks). This value must be greater than 0 and less than or equal to MaxSpawnDelay. Default value is 800 ticks.
# MinSpawnDelay - The minimum spawn delay amount (in ticks). Default value is 200 ticks.
# PlayerRange - Set the range for player activation (given that there are players online). Default value is 16.
# SpawnCount - Set how many mobs attempt to spawn. Default value is 4.
# SpawnRange - Set the radius around which the spawner will attempt to spawn mobs in. Default value is 4.

SpawnerSettings:
  Spawner1:
    Entities:
      - MUSHROOM_COW:100
    Biomes:
      - MUSHROOM_FIELDS
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4
  
# This is is a more compact way of formatting the above, both versions are valid. 
# How to format entity entries: [ENTITY_NAME:PERCENTAGE] if there is more than one entity you can do [ENTITY_NAME:PERCENTAGE, ENTITY_NAME:PERCENTAGE]
# If you are doing multiple biomes this is also a more compact way of formatting it.
# How to format biome entries: [BIOME_NAME] if there is more than one biome you can do [BIOME_NAME, BIOME_NAME] 

# Nether Biomes:
  
  Spawner2:
    Entities: [PIGLIN:10, PIGLIN_BRUTE:10, BLAZE:10, GHAST:10, HOGLIN:10, WITHER_SKELETON:10, ZOMBIFIED_PIGLIN:10, STRIDER:10, MAGMA_CUBE:10, ZOGLIN:10]
    Biomes: [BASALT_DELTAS, NETHER_WASTES, CRIMSON_FOREST, SOUL_SAND_VALLEY, WARPED_FOREST]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4

#Overworld Biomes

  Spawner3:
    Entities: [ALLAY:2, AXOLOTL:2, BAT:2, BEE:2, CAMEL:2, CAT:2, CAVE_SPIDER:2, CHICKEN:2, COW:2, CREEPER:2, DONKEY:2, EVOKER:2, FOX:2, FROG:2, GOAT:2, HORSE:2, HUSK:2, ILLUSIONER:2, IRON_GOLEM:2, LLAMA:2, MULE:2, OCELOT:2, PANDA:2, PARROT:2, PHANTOM:2, PIG:2, PILLAGER:2, RABBIT:2, RAVAGER:2, SHEEP:2, SILVERFISH:2, SKELETON:2, SKELETON_HORSE:2, SLIME:2, SNIFFER:2, SPIDER:2, STRAY:2, TRADER_LLAMA:2, VEX:2, VILLAGER:2, VINDICATOR:2, WANDERING_TRADER:2, WITCH:2, WITHER:2, WOLF:2, ZOMBIE:2, ZOMBIE_HORSE:2, ZOMBIE_VILLAGER:2, ARMADILLO:2]
    Biomes: [BADLANDS, BAMBOO_JUNGLE, BIRCH_FOREST, CHERRY_GROVE, DARK_FOREST, DESERT, DRIPSTONE_CAVES, ERODED_BADLANDS, FLOWER_FOREST, FOREST, GROVE, JAGGED_PEAKS, JUNGLE, LUSH_CAVES, MANGROVE_SWAMP, MEADOW, OLD_GROWTH_BIRCH_FOREST, OLD_GROWTH_PINE_TAIGA, OLD_GROWTH_SPRUCE_TAIGA, PLAINS, SAVANNA, SAVANNA_PLATEAU, SPARSE_JUNGLE, STONY_PEAKS, STONY_SHORE, SUNFLOWER_PLAINS, SWAMP, TAIGA, WINDSWEPT_FOREST, WINDSWEPT_GRAVELLY_HILLS, WINDSWEPT_HILLS, WINDSWEPT_SAVANNA, WOODED_BADLANDS]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4

  Spawner4:
    Entities: [COD:10, DOLPHIN:10, DROWNED:10, ELDER_GUARDIAN:10, GUARDIAN:10, SALMON:10, SQUID:10, TADPOLE:10, TROPICAL_FISH:10, PUFFERFISH:10]
    Biomes: [OCEAN, WARM_OCEAN, LUKEWARM_OCEAN, FROZEN_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_OCEAN, DEEP_COLD_OCEAN, COLD_OCEAN, FROZEN_RIVER, RIVER]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4

  Spawner5:
    Entities: [TURTLE:100]
    Biomes: [BEACH, SNOWY_BEACH]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4

  Spawner6:
    Entities: [SNOWMAN:50, POLAR_BEAR:50]
    Biomes: [SNOWY_PLAINS, SNOWY_SLOPES, SNOWY_TAIGA, FROZEN_PEAKS, ICE_SPIKES]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4

  Spawner7:
    Entities: [WARDEN:100]
    Biomes: [DEEP_DARK]
    Delay: -1
    MaxNearbyEntities: 1
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 1
    SpawnRange: 8

#End Biomes

  Spawner8:
    Entities: [ENDERMAN:40, ENDERMITE:10, SHULKER:40]
    Biomes: [END_BARRENS, END_MIDLANDS, THE_END, SMALL_END_ISLANDS, END_HIGHLANDS]
    Delay: -1
    MaxNearbyEntities: 16
    MaxSpawnDelay: 800
    MinSpawnDelay: 200
    PlayerRange: 16
    SpawnCount: 4
    SpawnRange: 4
```
 
### Overworld Grid Settings:
(**world.txt** found in the SkygridBlocks folder):
  
```
# SkyGrid World Block Selection Guide:

# Each line in the materials file corresponds to a block and its chance of appearing.
# Format: BlockID:Percentage (use block names or IDs).
# Example: "STONE:50" means a 50% chance of Stone appearing.
# You can include as many blocks as you want, and their percentages can be anything.
# The total percentage doesn't need to add up to 100%.
# You can use any of the materials in the Bukkit Material page here: [https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html]

# Block Distribution Rules:

# 1. No Percentage Specified for a Block:
#	- If a percentage is not specified for a block, the code assigns it a default percentage of 1.0. This ensures that all blocks are accounted for in the distribution process.
#	- For example:
#		STONE:50
#		DIRT:49
#		SAND:
#	Explanation:
#	-The code will interpret SAND as having a default percentage of 1.0 since no percentage is specified. After allocating 50% for STONE and 49% for DIRT, the remaining 1% will be distributed to SAND.

# 2. Total Percentage Exceeds 100%:
#	- When the total percentage exceeds 100%, the code scales down the percentages proportionally to ensure they sum up to 100%. This prevents over-allocation of blocks.
#	- For example:
#		STONE:80
#		DIRT:60
#		SAND:
#		-RIVER-
#		AIR:120
#		WATER:
#	Explanation:
#	- The initial total percentage for each list is calculated. To scale down the percentages, the scaling factor is calculated by dividing 100 by the sum of the initial percentages. Each percentage is multiplied by its respective scaling factor to adjust it accordingly.

# 3. Biome-Dependent Allocation:
#	- Blocks can be listed under specific biome headers, allowing for biome-dependent allocation.
#	- If the percentages within a biome list do not add up to 100%, the remaining percentage is redistributed among the blocks within that specific biome's list.
#	- For example:
#		STONE:50
#		DIRT:30
#		SAND:
#		AIR:
#		-OCEAN-
#		WATER:30
#		SAND:20
#		SANDSTONE:
#	Explanation:
#	- Each biome header gets its own redistribution percentage. For each biome list, percentages are calculated separately. Here, the remaining percentage is redistributed evenly among the blocks within each biome list.
#	- When the block percentages in a biome's list don't add up to 100%, the leftover percentage is redistributed among the blocks within that specific biome's list. This ensures that the total percentage for that biome accurately represents the distribution of blocks in the world.

# Biome-Specific Block Generation:
#	- Customize the block generation in your world based on different biomes.
#	- Specify unique block materials and percentages for each biome in the world.
#	- Biomes are signified by the -BIOME_NAME- header, and everything under the biome header will generate only in that biome unless another biome is specified.
#	- Multiple biomes in the header of the material files are supported. You can now specify multiple biomes separated by commas.
#	- Supported biomes can be found here: [https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html]

# How to Use/Configure Biome-Specific Blocks:
#	- Specify block materials and their respective percentages for each biome in your world.
#	- Example:
#		DIRT:20
#		STONE:20
#		GRASS_BLOCK:20
#		-OCEAN,COLD_OCEAN,DEEP_COLD_OCEAN,DEEP_FROZEN_OCEAN,DEEP_LUKEWARM_OCEAN,DEEP_OCEAN,FROZEN_OCEAN,LUKEWARM_OCEAN-
#		SAND:80
#		SANDSTONE:9.0
#		SEA_PICKLE:1.0
#		TUBE_CORAL:1.0
#		TUBE_CORAL_FAN:1.0
#		HORN_CORAL:1.0
#		HORN_CORAL_FAN:1.0
#		FIRE_CORAL:1.0
#		FIRE_CORAL_FAN:1.0
#		BUBBLE_CORAL:1.0
#		BUBBLE_CORAL_FAN:1.0
#		BRAIN_CORAL:1.0
#		BRAIN_CORAL_FAN:1.0
#		-WARM_OCEAN-
#		SAND:80
#		SANDSTONE:20
#
# Material loader version differences:
#	Basic Material Loader:
#		- Optimized for loading materials with no biome headers i.e. -BIOME_NAME-
#		- Materials distribution is enforced much more accurately, with the trade in performance reduction.
#		- Since there is no biome headers used the materials will be applied to all biomes equally.
#	Advanced material loader:
#		- Optimized for loading materials with biome headers, only enables when biomes headers are detected in any of the files.
#		- Materials distribution is enforced loosely through a weigh percentage system.
#		- All biomes must be included in the text file, omitting any biomes will result in blank chunks where the biomes are located
#		- Here are all the current biomes for the Overworld:
#
#-FROZEN_RIVER,FROZEN_OCEAN,DEEP_FROZEN_OCEAN,DEEP_LUKEWARM_OCEAN,LUKEWARM_OCEAN,COLD_OCEAN,DEEP_COLD_OCEAN,OCEAN,DEEP_OCEAN,RIVER,WARM_OCEAN,SWAMP,MANGROVE_SWAMP,DESERT,DARK_FOREST,OLD_GROWTH_PINE_TAIGA,OLD_GROWTH_SPRUCE_TAIGA,BEACH,SNOWY_BEACH,STONY_SHORE,JUNGLE,SPARSE_JUNGLE,BAMBOO_JUNGLE,JAGGED_PEAKS,FROZEN_PEAKS,ICE_SPIKES,STONY_PEAKS,FOREST,FLOWER_FOREST,BIRCH_FOREST,OLD_GROWTH_BIRCH_FOREST,TAIGA,SNOWY_TAIGA,SNOWY_PLAINS,GROVE,SNOWY_SLOPES,PLAINS,SUNFLOWER_PLAINS,MEADOW,MUSHROOM_FIELDS,CHERRY_GROVE,SAVANNA,SAVANNA_PLATEAU,WINDSWEPT_SAVANNA,WINDSWEPT_FOREST,WINDSWEPT_HILLS,WINDSWEPT_GRAVELLY_HILLS,DEEP_DARK,LUSH_CAVES,DRIPSTONE_CAVES,BADLANDS,ERODED_BADLANDS,WOODED_BADLANDS-

ACACIA_LEAVES:10
ACACIA_LOG:20
ALLIUM:2
AMETHYST_BLOCK:2
AMETHYST_CLUSTER:1.5
ANDESITE:10
AZALEA:1.5
AZALEA_LEAVES:1.5
AZURE_BLUET:2
BAMBOO:2
BEE_NEST:2
BEETROOTS:3
BELL:1.5
BIRCH_LEAVES:10
BIRCH_LOG:20
BLUE_ORCHID:2
BONE_BLOCK:3
BOOKSHELF:3
BRAIN_CORAL_BLOCK:1.5
BROWN_MUSHROOM:2
BUBBLE_CORAL_BLOCK:1.5
BUDDING_AMETHYST:1.5
CACTUS:4
CALCITE:2
CARROTS:3
CAVE_VINES:2
CHERRY_LEAVES:2
CHEST:0.0001
CHISELED_STONE_BRICKS:2
CLAY:20
COAL_ORE:15
COARSE_DIRT:5
COBBLESTONE:12
COBWEB:10
COPPER_ORE:10
CORNFLOWER:2
CRYING_OBSIDIAN:3
DANDELION:2
DARK_OAK_LEAVES:10
DARK_OAK_LOG:20
DARK_PRISMARINE:2
DEAD_BUSH:2
DEEPSLATE:70
DEEPSLATE_COAL_ORE:10
DEEPSLATE_COPPER_ORE:5
DEEPSLATE_DIAMOND_ORE:1
DEEPSLATE_GOLD_ORE:2.5
DEEPSLATE_IRON_ORE:10
DEEPSLATE_LAPIS_ORE:2.5
DEEPSLATE_REDSTONE_ORE:4
DIAMOND_ORE:1
DIORITE:10
DIRT:40
DRIPSTONE_BLOCK:3
FERN:2
FIRE_CORAL_BLOCK:1.5
FLOWERING_AZALEA:1.5
FLOWERING_AZALEA_LEAVES:1.5
GLASS:2
GLOW_LICHEN:2
GOLD_BLOCK:1.1
GOLD_ORE:5
GRANITE:10
GRASS:30
GRASS_BLOCK:90
GRAVEL:10
HANGING_ROOT:1.5
HORN_CORAL_BLOCK:1.5
ICE:1
INFESTED_DEEPSLATE:13
IRON_ORE:20
JUNGLE_LEAVES:10
JUNGLE_LOG:20
KELP_PLANT:1
LAPIS_ORE:5
LARGE_FERN:2
LAVA:10
LILAC:2
LILY_OF_THE_VALLEY:1.5
LILY_PAD:2
MANGROVE_LEAVES:10
MANGROVE_LOG:20
MANGROVE_ROOTS:1
MANGROVE_WOOD:1
MELON:5
MOSS_BLOCK:3
MOSSY_COBBLESTONE:5
MOSSY_STONE_BRICKS:2
MUD:5
MUDDY_MANGROVE_ROOTS:4
MYCELIUM:15
OAK_LEAVES:10
OAK_LOG:20
OBSIDIAN:5
ORANGE_TULIP:2
OXEYE_DAISY:2
PACKED_ICE:2
PEONY:2
PINK_PETALS:2
PINK_TULIP:2
PISTON:1.7
PODZOL:5
POINTED_DRIPSTONE:2
POPPY:2
POTATOES:2
PRISMARINE:2
PUMPKIN:5
RAW_COPPER_BLOCK:1.2
RAW_IRON_BLOCK:1.2
RED_MUSHROOM:2
RED_SAND:5
RED_SANDSTONE:3
RED_TULIP:2
REDSTONE_ORE:8
ROOTED_DIRT:4
ROSE_BUSH:2
SAND:20
SANDSTONE:10
SCULK:5
SCULK_CATALYST:2
SCULK_SENSOR:2
SCULK_SHRIKER:0.8
SCULK_VEIN:3
SEA_LANTERN:3
SEA_PICKLE:2
SEAGRASS:2
SMALL_DRIPLEAF:2
SMOOTH_BASALT:2
SNOW_BLOCK:8
SPAWNER:0.00001
SPONGE:2
SPORE_BLOSSOM:2
SPRUCE_LEAVES:10
SPRUCE_LOG:20
STICKY_PISTON:2
STONE:140
STONE_BRICKS:5
SUGAR_CANE:5
SUNFLOWER:2
SUSPICIOUS:SAND:3
SUSPICIOUS_GRAVEL:2
SWEET_BERRY_BUSH:2
TALL_GRASS:2
TARGET:2
TNT:2
TUBE_CORAL_BLOCK:1.5
TUFF:5
WATER:10
WHEAT:2
WHITE_CONCRETE_POWDER:5
WHITE_TULIP:2
WHITE_WOOL:25
WITHER_ROSE:1.5
```
 
### Nether Grid Settings:
(**world_nether.txt** found in the SkygridBlocks folder):
 
```
ANCIENT_DEBRIS:1
BASALT:20
BLACKSTONE:60
BONE_BLOCK:5
CHEST:.01
CRIMSON_FUNGUS:6
CRIMSON_HYPHAE:6
CRIMSON_NYLIUM:60
CRIMSON_ROOTS:6
CRIMSON_STEM:10
CRYING_OBSIDIAN:2
GILDED_BLACKSTONE:4
GLOWSTONE:3
GRAVEL:30
LAVA:50
MAGMA_BLOCK:50
NETHER_BRICKS:30
NETHER_GOLD_ORE:6
NETHER_QUARTZ_ORE:15
NETHER_SPROUTS:7
NETHER_WART:30
NETHER_WART_BLOCK:6
NETHERRACK:600
OBSIDIAN:5
OCHRE_FROGLIGHT:4
PEARLESCENT_FROGLIGHT:4
POLISHED_BLACKSTONE:6
POLISHED_BLACKSTONE_BRICKS:4
SHROOMLIGHT:5
SOUL_FIRE:5
SOUL_SAND:100
SOUL_SOIL:10
SPAWNER:0.001
TWISTING_VINES:5
VERDANT_FROGLIGHT:4
WARPED_FUNGUS:6
WARPED_HYPHAE:6
WARPED_NYLIUM:60
WARPED_ROOTS:6
WARPED_STEM:10
WARPED_WART_BLOCK:10
```
 
### End Grid Settings:
(**world_the_end.txt** found in the SkygridBlocks folder):
 
```
BREWING_STAND:.01
CHEST:0.00001
CHORUS_FLOWER:5
END_ROD:10
END_STONE:60
END_STONE_BRICKS:5
ENDER_CHEST:.01
OBSIDIAN: 10
PURPLE_STAINED_GLASS:5
PURPUR_BLOCK:5
PURPUR_PILLAR:5
PURPUR_STAIRS:5
SPAWNER:0.000001
```
