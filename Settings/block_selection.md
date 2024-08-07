
# SkyGrid World Block Selection Guide:

- Each line in the materials file corresponds to a block and its chance of appearing.
- Format: `BlockID:Percentage` (use block IDs).
- Example: `STONE:50` means a 50% chance of Stone appearing.
- You can include as many blocks as you want, and their percentages can be anything.
- The total percentage doesn't need to add up to 100%.
- You can use any of the materials in the Bukkit Material page here: 
[https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html]

## Block Distribution Rules:

### 1. No Percentage Specified for a Block:
	
If a percentage is not specified for a block, the code assigns it a default percentage of 1.0. This ensures that all blocks are accounted for in the distribution process.
	
**For example**:
```
STONE:50
DIRT:49
SAND:
```
**Explanation**:
	
The code will interpret SAND as having a default percentage of 1.0 since no percentage is specified. After allocating 50% for STONE and 49% for DIRT, the remaining 1% will be distributed to SAND.

### 2. Total Percentage Exceeds 100%:
	
When the total percentage exceeds 100%, the code scales down the percentages proportionally to ensure they sum up to 100%. This prevents over-allocation of blocks.
	
**For example**:
```
STONE:80
DIRT:60
SAND:
-RIVER-
AIR:120
WATER:
```
**Explanation**:

The initial total percentage for each list is calculated. To scale down the percentages, the scaling factor is calculated by dividing 100 by the sum of the initial percentages. Each percentage is multiplied by its respective scaling factor to adjust it accordingly.

### 3. Biome-Dependent Allocation:

Blocks can be listed under specific biome headers, allowing for biome-dependent allocation.

If the percentages within a biome list do not add up to 100%, the remaining percentage is redistributed among the blocks within that specific biome's list.

**For example**:
```
STONE:50
DIRT:30
SAND:
AIR:
-OCEAN-
WATER:30
SAND:20
SANDSTONE:
```
**Explanation**:
	
Each biome header gets its own redistribution percentage. For each biome list, percentages are calculated separately. Here, the remaining percentage is redistributed evenly among the blocks within each biome list.
	
When the block percentages in a biome's list don't add up to 100%, the leftover percentage is redistributed among the blocks within that specific biome's list. This ensures that the total percentage for that biome accurately represents the distribution of blocks in the world.


## Material loader version differences:

### 1.	Basic Material Loader:
- Optimized for loading materials with no biome headers i.e. `-BIOME_NAME-`
- Materials distribution is enforced much more accurately, with the trade in performance reduction.
- Since there is no biome headers used the materials will be applied to all biomes equally.

### 2.	Advanced material loader:
- Optimized for loading materials with biome headers, only enables when biomes headers are detected in any of the files.
- Materials distribution is enforced loosely through a weigh percentage system.
- All biomes must be included in the text file, omitting any biomes will result in blank chunks where the biomes are located

**Additional Mechanisms:**

- [Biome-Specific Block Generation Guide](biome_specific.md)

## Grid Settings:

### Overworld Grid Settings:
(**world.txt** found in the SkygridBlocks folder):

```
# Overworld Biomes
# -FROZEN_RIVER,FROZEN_OCEAN,DEEP_FROZEN_OCEAN,DEEP_LUKEWARM_OCEAN,LUKEWARM_OCEAN,COLD_OCEAN,DEEP_COLD_OCEAN,OCEAN,DEEP_OCEAN,RIVER,WARM_OCEAN,SWAMP,MANGROVE_SWAMP,DESERT,DARK_FOREST,OLD_GROWTH_PINE_TAIGA,OLD_GROWTH_SPRUCE_TAIGA,BEACH,SNOWY_BEACH,STONY_SHORE,JUNGLE,SPARSE_JUNGLE,BAMBOO_JUNGLE,JAGGED_PEAKS,FROZEN_PEAKS,ICE_SPIKES,STONY_PEAKS,FOREST,FLOWER_FOREST,BIRCH_FOREST,OLD_GROWTH_BIRCH_FOREST,TAIGA,SNOWY_TAIGA,SNOWY_PLAINS,GROVE,SNOWY_SLOPES,PLAINS,SUNFLOWER_PLAINS,MEADOW,MUSHROOM_FIELDS,CHERRY_GROVE,SAVANNA,SAVANNA_PLATEAU,WINDSWEPT_SAVANNA,WINDSWEPT_FOREST,WINDSWEPT_HILLS,WINDSWEPT_GRAVELLY_HILLS,DEEP_DARK,LUSH_CAVES,DRIPSTONE_CAVES,BADLANDS,ERODED_BADLANDS,WOODED_BADLANDS-

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
BEE_NEST:.001
BEETROOTS:3
BELL:0.01
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
CHEST:0.1
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
SPAWNER:0.0001
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
# Nether Biomes
# -BASALT_DELTAS,CRIMSON_FOREST,NETHER_WASTES,SOUL_SAND_VALLEY,WARPED_FOREST-

ANCIENT_DEBRIS:0.1
BASALT:20
BLACKSTONE:60
BONE_BLOCK:5
CHEST:0.2
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
SPAWNER:0.01
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
# End Biomes
# -THE_END,END_BARRENS,END_HIGHLANDS,END_MIDLANDS,SMALL_END_ISLANDS-

BREWING_STAND:.1
CHEST:0.001
CHORUS_FLOWER:5
END_ROD:10
END_STONE:60
END_STONE_BRICKS:5
ENDER_CHEST:.1
OBSIDIAN: 10
PURPLE_STAINED_GLASS:5
PURPUR_BLOCK:5
PURPUR_PILLAR:5
PURPUR_STAIRS:5
SPAWNER:0.0001
```
