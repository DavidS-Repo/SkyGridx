
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

## Commands:

- `/tpr [world]`: Random teleport in the Overworld, Nether, or End.
- `/tpro`: Random teleport in the Overworld.
- `/tprn`: Random teleport in the Nether.
- `/tpre`: Random teleport in the End.
- `/fogon`: Enable fog.
- `/fogoff`: Disable fog.
- `/eclogson`: Enable Event Control logging.
- `/eclogsoff`: Disable Event Control logging.
- `/pregen`: Pregenerate the overworld, nether, and end.
- `/pregenoff`: Turn off pregeneration process.
- `/patch`: Patch files to update materials and entities to the newest version.

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

## Installation Instructions

For detailed installation instructions, [click here](Settings/Installation.md).

## Configuration Files
- [Plugin Settings](Settings/settings.md)
- [Ore Generator Settings](Settings/ores.md)
- [Mob Spawner Settings](Settings/spawner_settings.md)
- [Chest Settings](Settings/chest_settings.md)
- [SkyGrid World Block Selection Guide](Settings/block_selection.md)
---

**Additional Mechanisms:**
- [Biome-Specific Block Generation Guide](Settings/biome_specific.md)
---
