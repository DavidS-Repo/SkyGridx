
The SkyGrid Plugin offers access to the latest blocks (1.21) and empowers users to fully customize the grids in the Overworld, Nether, and End dimensions. The initial generation process, which typically takes 1-5 minutes, adapts to the server's capabilities. Subsequently, all generation activities occur efficiently in real-time.

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
   - Built in async pre-generator feature that allows for efficient world generation. This feature is accessible through the **/pregen** command, with customizable parameters for Parallel Tasks Multiplier, print update delay and world.
   - Works best on paper servers, on none paper servers the async functionality will not be used. Recommend you go into your paper server paper-global.yml and update these

```yaml
chunk-system:
  gen-parallelism: default
  io-threads: 12
  worker-threads: 12
```
   - Adjust `io-threads` and `worker-threads` to match your CPU’s thread count. Default settings utilize only half.
   - Usage: /pregen <ParallelTasksMultiplier> <PrintUpdateDelay(inSeconds/Minutes/Hours)> <world>
     - Example: `/pregen 6 5s world`
       - Pre-Generate the `overworld` at a max rate of (threads_available * 6 parallel_tasks), prints logs every 5 seconds
     - Example: `/pregen 2 2m world_nether`
       - Pre-Generate the `nether` at a max rate of (threads_available * 2 parallel_tasks), prints logs every 2 minutes
     - Example: `/pregen 1 12h world_the_end`
       - Pre-Generate the `the end` at a max rate of (threads_available * 1 parallel_tasks), prints logs every 12 hours
   - For `ParallelTasksMultiplier`, it is recommended to stay below your thread count.
   - `ParallelTasksMultiplier` limits the number of parallel chunk load tasks. It is multiplied by the number of threads available at server initialization. For instance, if your server starts with 12 threads, the maximum number of parallel tasks allowed when `ParallelTasksMultiplier` is set to 6 will be 72.
   - A `ParallelTasksMultiplier` of 6 yielded ~150-200 chunks per second on a 5600x CPU, depending on server activity and other system tasks.
   - Increasing `ParallelTasksMultiplier` beyond current CPU utilization can further enhance performance. For example, setting it to 12 yielded ~190-250 chunks per second.
   - In summary, `ParallelTasksMultiplier` determines the load on your server. A smaller number results in a lower load and fewer chunks per second, while a larger number increases the server load but improves chunk processing speed.

---

**Ore generation examples:**

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
- `/pregen`: Pregenerate any of the dimensions; overworld, nether, and end.
- `/pregenoff`: Turn off pregeneration process.
- `/patch`: Patch files to update materials and entities to the newest version.

## Permissions:

(**Default all**)
- `sg.tpr`: Allows teleportation to Overworld, Nether, and End using the `/tpr [world]` command.
- `sg.tpr.overworld`: Grants permission to use the `/tpro` command for teleportation in the Overworld.
- `sg.tpr.nether`: Grants permission to use the `/tprn` command for teleportation in the Nether.
- `sg.tpr.end`: Enables usage of the `/tpre` command for teleportation in the End.

(**Default OP**)
- `sg.tpr.*`: Provides access to all teleportation commands.
- `sg.fogon`: Allows enabling fog using the `/fogon` command.
- `sg.fogoff`: Allows disabling fog with the `/fogoff` command.
- `sg.eclogson`: Grants permission to enable Event Control logging using `/eclogson`.
- `sg.eclogsoff`: Grants permission to disable Event Control logging using `/eclogsoff`.
- `sg.pregen`: Grants permission to use the pre-generation command with customizable parameters.
- `sg.pregenoff`: Grants permission to disable pre-generation using the `/pregenoff` command.
- `sg.patch`: Allows patching files to update materials and entities to another version.
- `sg.regen`: Grants permission to regenerate all loaded chunks using the `/regen` command.
- `sg.*`: Provides access to all SkyGrid commands.

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
