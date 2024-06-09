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
EventControl:
  # (true to enable event check, false to disable)
  # BlockIgniteEvent - Prevents lava from causing fires to near by flammable grid blocks
  BlockIgniteEvent: true
  
  # BlockFadeEvent - Prevents ice from melting if it's in the grid and fire from burning out
  BlockFadeEvent: true
  
  # BlockFromToEvent - Prevents lava and water from flowing due to server overload
  BlockFromToEvent: true
  
  # StructureGrowEvent - Prevents saplings in floating in grid from growing
  StructureGrowEvent: true
  
  # BlockSpreadEvent - Prevents bamboo sapling from growing since it does not trigger with StructureGrowEvent
  BlockSpreadEvent: true
  
  # BlockGrowEvent is used for KELP_PLANT, SUGAR_CANE, CACTUS, and TWISTING_VINES_PLANT (not logged in /eclogson, to much spam)
  BlockGrowEvent: true
  
  # BlockFormEvent - Prevents block updates that mess up the grid in snow biomes, snow forming on top of blocks
  BlockFormEvent: true
  
  # EntityChangeBlock - Prevents gravity-affected blocks from randomly falling due to overload
  EntityChangeBlockEvent: true
  
  # This list is for blocks that you want to monitor the BlockFormEvent and EntityChangeBlock events.
  GRAVITY_AFFECTED_BLOCKS:
    Materials:
    - SAND
    - RED_SAND
    - GRAVEL
    - KELP_PLANT
    - ANVIL
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