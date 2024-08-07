# Ore Generator Settings

This file is used to specify the chances of different ores being chosen for replacement

When lava or water generates stone blocks in the world. The stone generate in the world can be replaced by any block you want as long as its in the bukkit materials list

https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html

Format: `Material_Name:Percentage`

The percentage represents the chance of the ore being chosen for replacement out of the total replacements when lava or water generates stone or cobblestone.

Each line represents one ore with its corresponding replacement chance.

```
DIAMOND_ORE:5
```

This means that there is a 5% chance of a diamond ore block being chosen when lava or water generates a stone block.

The total sum of percentages should be less than or equal to 100.
If the sum exceeds 100, the probabilities will be scaled down proportionally to fit within the 0-100 range.

For example, if the total sum of percentages exceeds 100 as follows:

```
DIAMOND_ORE:40
COAL_ORE:60
IRON_ORE:30
GOLD_ORE:20
```

The total sum is `40 + 60 + 30 + 20` = `150`, which exceeds `100`.
To fit within the `0-100` range, the probabilities will be scaled down proportionally:

`DIAMOND_ORE`: 40 * (100 / 150) = 26 (approximately 27)
`COAL_ORE`: 60 * (100 / 150) = 40
`IRON_ORE`: 30 * (100 / 150) = 20
`GOLD_ORE`: 20 * (100 / 150) = 13 (approximately 13)

The adjusted probabilities now add up to `26 + 40 + 20 + 13 = 99`, which is within the `0-100` range.

## Ore Generator Settings
(**ores.yml** found in the OreGenBlock folder)

```YML
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

## Ore generation examples

![instrunctions step 1](https://i3.ytimg.com/vi/UrzhCaiLKyI/maxresdefault.jpg)
![instrunctions step 1](https://i3.ytimg.com/vi/NMkvj6UvmLg/maxresdefault.jpg)