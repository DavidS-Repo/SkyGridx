# Biome-Specific Block Generation

## Overview

The SkyGrid plugin now uses YAML configuration files to define which blocks should generate in each biome. This approach provides a more structured and readable configuration process, as well as greater flexibility in specifying multiple biomes per distribution.

## How the Configuration Works

1. **Top-Level Keys:**  
   Your configuration file (e.g., `world.yml`, `world_nether.yml`, `world_the_end.yml`) will contain two main sections:
   - `biomes`: Maps biomes (or multiple biomes) to a specific distribution.
   - `distributions`: Defines sets of block materials along with their relative probabilities.

2. **Multiple Biomes Per Distribution:**  
   You can assign multiple biomes to the same distribution by listing them in a single key, separated by commas. Each biome listed will use the same set of blocks and probabilities.

3. **Default Distribution (Optional):**  
   If a biome does not have a defined distribution, the plugin can fall back to a `default_distribution` if provided. This ensures that even unmapped biomes still generate blocks, preventing gaps in block generation.

4. **No More Biome Headers with Dashes:**  
   Previously, biome-specific configurations were defined using headers like `-BIOME_NAME-`. With the new YAML format, simply define a key in the `biomes` section:
```yml
biomes:
     DESERT:
       distribution: desert_distribution
 ```
   or for multiple biomes:
 ```yml
biomes:
     OCEAN, COLD_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_OCEAN, FROZEN_OCEAN, LUKEWARM_OCEAN:
       distribution: oceanic_distribution
 ```

5. **Supported Biomes:**
   For a list of valid biome names, refer to the [Spigot/Bukkit Biome documentation](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html).

## Example Configuration

Below is a sample YAML configuration (`world.yml`) that demonstrates assigning distributions to single and multiple biomes, along with defining a fallback `default_distribution`:

```yaml
# Example world.yml

biomes:
  # Single biome mapped to "desert_distribution"
  DESERT:
    distribution: desert_distribution

  # Multiple biomes sharing the same "oceanic_distribution"
  OCEAN, COLD_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_OCEAN, FROZEN_OCEAN, LUKEWARM_OCEAN:
    distribution: oceanic_distribution

distributions:
  # Distribution for desert biomes
  desert_distribution:
    SAND: 100

  # Distribution for oceanic biomes
  oceanic_distribution:
    SAND: 80
    SANDSTONE: 9.0
    SEA_PICKLE: 1.0
    TUBE_CORAL: 1.0
    TUBE_CORAL_FAN: 1.0
    HORN_CORAL: 1.0
    HORN_CORAL_FAN: 1.0
    FIRE_CORAL: 1.0
    FIRE_CORAL_FAN: 1.0
    BUBBLE_CORAL: 1.0
    BUBBLE_CORAL_FAN: 1.0
    BRAIN_CORAL: 1.0
    BRAIN_CORAL_FAN: 1.0

default_distribution:
  # Fallback distribution used if a biome isn't defined in the 'biomes' section
  DIRT: 50
  STONE: 30
  SAND: 20
```

![zoomed out biome support](https://www.toolsnexus.com/mc/out.jpg)

![zoomed in biome support](https://www.toolsnexus.com/mc/in.jpg)

## Key Points

- **Flexible Assignment:**  
  Assign one or many biomes to the same distribution by listing them in a single YAML key separated by commas.
  
- **Simple Percentages:**  
  Percentages do not need to sum to 100%. They represent relative weights, and the plugin normalizes them internally.

- **Maintainable Structure:**  
  YAML structure makes it easier to organize and maintain biome-specific distributions as your server evolves.

## Further Customization

- You can create as many distributions as needed and assign them to any number of biomes.
- Add or remove entries from distributions as your world’s gameplay dynamics change.
- Use a `default_distribution` to ensure that all biomes have some form of block generation.

---