 
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

**Material Distribution:**
   - Define personalized materials for block placement per world.
   - Fine-tune material distribution percentages for a distinct experience.

**Biome-Specific Grids:**
   - Customize grids in Overworld, Nether, and End with unique blocks for each supported [biome](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/biome.html).

**Ore Generation:**
   - Seamlessly integrated into the gameplay, this feature spawns ores when the player generates stone. 
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
 
# Installation Instructions:
 
- Download the SkyGrid plugin from the provided source.
 
- Place the plugin JAR file into the "plugins" folder of your Minecraft server directory.
 
- Restart the server to load the plugin and ensure its successful integration.
 
- Customize the world materials and ore generation settings by editing the corresponding text files located in the plugin's data folder.
 
- Activate the SkyGrid plugin by entering the "**/sgon**" command to enable real-time SkyGrid generation. And wait for completion messages of initial generation (should take about 1-5 minutes depending on your server)
 
![Comman console output](https://www.toolsnexus.com/mc/com.jpg)


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
 
## Here is another example,
```
-OCEAN-
SAND:80
SANDSTONE:9
SEA_PICKLE:1
TUBE_CORAL:1
TUBE_CORAL_FAN:1
HORN_CORAL:1
HORN_CORAL_FAN:1
FIRE_CORAL:1
FIRE_CORAL_FAN:1
BUBBLE_CORAL:1
BUBBLE_CORAL_FAN:1
BRAIN_CORAL:1
BRAIN_CORAL_FAN:1
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
 
- For a seamless SkyGrid generation experience, it is recommended to utilize a void world where unnecessary block removal is not required. This approach significantly reduces the server's load during real-time generation.
 
- To create a simple void world, follow these steps:
    1. Open Minecraft and navigate to the "**Singleplayer**" menu.
    2. Click "**Create New World**".
    3. Change the world name to "**World**".
    4. In the top menu, click on "**World**".
    5. Select "**World Type**" and change it to "**Superflat**".
    6. Click "**Customize**" and remove all layers by selecting and deleting them.
    7. Turn off "**Generated Structures**".
    8. Finally, click "**Create New World**".​       
- Once created, you can save and exit the game. To access the world folder directory, return to the "**Singleplayer**" menu and click "**Edit**," followed by "**Open World Folder**". From there, you can easily locate the world folder, copy it, and paste it into your server directory. Make sure to remove any existing worlds to maintain a clean and dedicated SkyGrid experience.
 
- Alternatively, if you would like more than one biome for the overworld you can use a data pack like [this one](https://modrinth.com/datapack/skyblock-void-worldgen) that generates a void world on all dimensions while keeping the biome and bounding box data intact. This should work on any seed and will also help reduce the load of block clearing done in the other two dimensions.
 
# Commands:
 
- **/sgon** (turns on the SkyGrid generation)
- **/tpr** (random teleport in the Overworld)
- **/tpr nether** (random teleport in the Nether)
- **/tpr end** (random teleport in the End)
 
