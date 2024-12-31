# Chest Settings

Below is an overview of how the **ChestSettings.yml** works with the current plugin code. The code uses a **weight-based** system to pick items for each empty slot in a chest.  

## How It Works

1. **ChestSettings.yml**  
   - This file is located in `SkygridBlocks/ChestSettings.yml` (by default) and contains all the configurations for how each “chest type” will be filled, as well as which biomes use those settings.

2. **Top-Level Key: `ChestSettings`**  
   - Under this key, you create one or more “chest configurations,” each identified by a custom key (e.g., `Chest1`, `MushroomChest`, etc.).  

3. **Chest Configuration Format**  
   - **Items**: A list of items that can appear in the chest. Each item is defined as `MATERIAL_NAME:weight:maxAmount`.  
     - `MATERIAL_NAME` must match a valid [Bukkit Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html).  
     - `weight` is a `double` (or integer) that indicates how likely that item is to be chosen **relative to other items** in the same chest.  
       - The plugin adds all item weights and picks an item in proportion to those weights.  
       - **Example**: If four items each have a weight of `5`, the total weight is `20`. Each has a 5/20 = 25% chance per slot selection.  
     - `maxAmount` is the maximum stack size of that item that can appear **in total** (not per slot). Once the plugin places that many of an item, it won’t place any more of it in that chest.
   - **Biomes**: A list of valid [Bukkit Biome names](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html).  
     - Whenever a chest is placed in one of these biomes, **this** configuration is used.

4. **How the Plugin Fills the Chest**  
   1. **Determines the Biome** of the block where the chest is located.  
   2. **Finds the Chest Configuration** keyed to that biome (based on the `Biomes:` list).  
   3. **Collects All Empty Slots** in the chest’s inventory.  
   4. **For Each Empty Slot**, picks one item from the configuration:
      - It randomizes a value between `0` and the sum of all item `weight`s.
      - It finds which item corresponds to that random value in the cumulative weights array.
      - It checks how many of that item have already been placed (to respect `maxAmount`).
      - If it can still add some of that item, it places **1 to (remaining allowed)** in that slot.
   5. This repeats until all empty slots are processed or each item hits its `maxAmount`.

## YAML Formatting Tips

### Valid Keys (Chest Names)
- **Alphanumeric & underscores** are safe:
  - `Chest1`, `My_Chest`, `Chest2_Forest`
- For spaces or special characters, wrap the key in quotes:
  - `"Special Chest!"`
  
### Defining Items
- **Expanded Style**:
```yml
ChestSettings:
  Chest1:
    Items:
      - MYCELIUM:5:5
      - RED_MUSHROOM:5:5
      - BROWN_MUSHROOM:5:5
    Biomes:
      - MUSHROOM_FIELDS
```

- **Compact Style**:
```yml
ChestSettings:
  Chest1:
    Items: [MYCELIUM:5:5, RED_MUSHROOM:5:5, BROWN_MUSHROOM:5:5]
    Biomes: [MUSHROOM_FIELDS]
```

Both formats are valid. The **plugin** doesn’t care which style you use, as long as YAML is valid.

## Weight vs. Probability
- The `weight` values **do not** represent direct percentages; rather, they’re **relative** likelihoods.  
- If item A has `weight = 10` and item B has `weight = 20`, item B is twice as likely to be chosen each time a slot is filled.  
- **Normalization**: The plugin sums all weights for a chest, then each item’s probability is `(itemWeight / totalWeight)`.

## Reducing Item Chances (Dummy/Filler Items)
- If you want a *lower* chance for valuable items (e.g., to ensure an item only appears 5% of the time in each slot), you can increase the total weight by adding “dummy” items, such as `AIR` or any other filler.  
  - **Example**:
```yml
ChestSettings:
  RareChest:
    Items:
      - AIR:80:1
      - ELYTRA:5:1
    Biomes: [PLAINS]
```
  - Now the total weight is `85`. Each slot has a `5/85 ≈ 5.88%` chance to get ELYTRA (until it hits its `maxAmount`).

## Example Configuration

```yml
ChestSettings:
  # A custom chest config for the "MUSHROOM_FIELDS" biome:
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

  # Overworld config for various biomes:
  Chest2:
    Items: [WOODEN_AXE:20:1, WOODEN_PICKAXE:20:1, CROSSBOW:20:1, NAME_TAG:20:1, GOLDEN_APPLE:20:1, ...]
    Biomes: [BADLANDS, BAMBOO_JUNGLE, BIRCH_FOREST, ...]

  # Nether config:
  Chest3:
    Items: [GOLDEN_APPLE:20:1, ENCHANTED_GOLDEN_APPLE:20:1, GOLD_BLOCK:20:1, ...]
    Biomes: [BASALT_DELTAS, NETHER_WASTES, CRIMSON_FOREST, SOUL_SAND_VALLEY, WARPED_FOREST]

  # The End config:
  Chest4:
    Items: [ELYTRA:20:1, DRAGON_HEAD:20:1, DRAGON_EGG:20:1, END_CRYSTAL:20:1, ...]
    Biomes: [THE_END, END_BARRENS, END_HIGHLANDS, END_MIDLANDS, SMALL_END_ISLANDS]
```

### Notes on `maxAmount`
- `maxAmount` is **per item per chest**. If the chest tries to exceed that total, it simply won’t place any more of that item.
- The plugin picks a random amount (between 1 and `maxAddable`) each time that item is selected.

### Summary
- **Chest Settings** is the parent key; each child key (e.g., `Chest1`) defines a configuration.
- **Items** are chosen per **empty slot** via a weighted random selection.
- **Biomes** specify which configuration applies to chests placed in specific biomes.
- Use large `AIR` weights (or other filler items) if you want certain items to appear only rarely.
- If multiple configs share the same biome, the plugin picks from whichever config is first in that biome’s set (iterating order isn’t guaranteed).
