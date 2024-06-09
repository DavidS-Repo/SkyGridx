# Mob Spawner Settings

## How to Use:
 
The available entities can be found here https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
The available biomes can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html
 
Entities - Define entities and their chances of being chosen for the spawner. Formatting `- DOLPHIN:33.33`
Biomes -  List the biomes where this spawner will spawn. Formatting "- BEACH"
Delay: - Set the spawner's delay. If set to -1, the delay will be randomized (randomly choose a delay between min and max spawn delays).
MaxNearbyEntities - Set the maximum nearby entities (of the same type) allowed. Default value is `16`.
MaxSpawnDelay - The maximum spawn delay amount (in ticks). This value must be greater than `0` and less than or equal to MaxSpawnDelay. Default value is `800` ticks.
MinSpawnDelay - The minimum spawn delay amount (in ticks). Default value is `200` ticks.
PlayerRange - Set the range for player activation (given that there are players online). Default value is `16`.
SpawnCount - Set how many mobs attempt to spawn. Default value is `4`.
 
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
 
In the example above `Spawner1` can spawn both `SKELETON` and `ZOMBIE` entities with equal chances.
 
**Random Entity Selection**: Create a list of entities with associated spawn weights to add unpredictability to your spawners.
 
```YML
Entities: [SKELETON:10, ZOMBIE:10, CREEPER:5]
```
 
In this example, `SKELETON` and `ZOMBIE` have higher chances of being chosen for the spawner than `CREEPER`.
 
## Spawner Settings:
(**SpawnerSettings.yml** found in the SkygridBlocks folder)
 
```YML
SpawnerSettings:
  # Example of custom spawner entity for the biome MUSHROOM_FIELDS
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
 
  # Overworld Biomes
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