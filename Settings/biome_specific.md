# Biome-Specific Block Generation
 
## How to Use:
 
### Biome-Specific Block Generation:
 
- Customize the block generation in your world based on different biomes.
- Specify unique block materials and percentages for each biome in the world.
- Biomes are signified by the `-BIOME_NAME-` header, and everything under the biome header will generate only in that biome unless another biome is specified after it.
- Multiple biomes in the header of the material files are supported. You can now specify multiple biomes separated by commas.
  - For example: `-BIOME1,BIOME2-`
- Supported biomes can be found here: [https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html]
 
### Example:
 
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
 
- In this example, when generating chunks in the `OCEAN` biome, there's a 50% chance of generating `SAND` blocks and a 50% chance of generating "WATER" blocks. In the `DESERT` biome, only `SAND` blocks will be generated. For the blocks above they will be generated on all biomes not specified in the file for that dimension. So, if you only want to have `1` custom biome with the rest having another variety of blocks you can do that.
 
### Here is an example with multiple biomes,
 
```
-DESERT-
SAND:100
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
 
![zoomed out biome support](https://www.toolsnexus.com/mc/out.jpg)
 
![zoomed in biome support](https://www.toolsnexus.com/mc/in.jpg)
