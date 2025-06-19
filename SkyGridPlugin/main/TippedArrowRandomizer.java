package main;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomizes a tipped arrow with a valid vanilla potion effect for Minecraft Java 1.21.6.
 * 
 * To use: Call randomizeTippedArrow(ItemStack) on any TIPPED_ARROW item.
 */
public class TippedArrowRandomizer {

	// All valid vanilla tipped arrow types for Java 1.21.6.
	// Grouped by effect.

	private static final PotionType[] ARROW_POTION_TYPES = {
			// Regeneration
			// - Arrow of Regeneration
			// - Arrow of Regeneration (long)
			// - Arrow of Regeneration II
			PotionType.REGENERATION,
			PotionType.LONG_REGENERATION,
			PotionType.STRONG_REGENERATION,

			// Swiftness
			// - Arrow of Swiftness
			// - Arrow of Swiftness (long)
			// - Arrow of Swiftness II
			PotionType.SWIFTNESS,
			PotionType.LONG_SWIFTNESS,
			PotionType.STRONG_SWIFTNESS,

			// Fire Resistance
			// - Arrow of Fire Resistance
			// - Arrow of Fire Resistance (long)
			PotionType.FIRE_RESISTANCE,
			PotionType.LONG_FIRE_RESISTANCE,

			// Poison
			// - Arrow of Poison
			// - Arrow of Poison (long)
			// - Arrow of Poison II
			PotionType.POISON,
			PotionType.LONG_POISON,
			PotionType.STRONG_POISON,

			// Healing (Instant Health)
			// - Arrow of Healing
			// - Arrow of Healing II
			PotionType.HEALING,
			PotionType.STRONG_HEALING,

			// Night Vision
			// - Arrow of Night Vision
			// - Arrow of Night Vision (long)
			PotionType.NIGHT_VISION,
			PotionType.LONG_NIGHT_VISION,

			// Weakness
			// - Arrow of Weakness
			// - Arrow of Weakness (long)
			PotionType.WEAKNESS,
			PotionType.LONG_WEAKNESS,

			// Strength
			// - Arrow of Strength
			// - Arrow of Strength (long)
			// - Arrow of Strength II
			PotionType.STRENGTH,
			PotionType.LONG_STRENGTH,
			PotionType.STRONG_STRENGTH,

			// Slowness
			// - Arrow of Slowness
			// - Arrow of Slowness (long)
			// - Arrow of Slowness IV
			PotionType.SLOWNESS,
			PotionType.LONG_SLOWNESS,
			PotionType.STRONG_SLOWNESS,

			// Harming (Instant Damage)
			// - Arrow of Harming
			// - Arrow of Harming II
			PotionType.HARMING,
			PotionType.STRONG_HARMING,

			// Water Breathing
			// - Arrow of Water Breathing
			// - Arrow of Water Breathing (long)
			PotionType.WATER_BREATHING,
			PotionType.LONG_WATER_BREATHING,

			// Invisibility
			// - Arrow of Invisibility
			// - Arrow of Invisibility (long)
			PotionType.INVISIBILITY,
			PotionType.LONG_INVISIBILITY,

			// Leaping (Jump Boost)
			// - Arrow of Leaping
			// - Arrow of Leaping (long)
			// - Arrow of Leaping II
			PotionType.LEAPING,
			PotionType.LONG_LEAPING,
			PotionType.STRONG_LEAPING,

			// Luck
			// - Arrow of Luck
			PotionType.LUCK,

			// Slow Falling
			// - Arrow of Slow Falling
			// - Arrow of Slow Falling (long)
			PotionType.SLOW_FALLING,
			PotionType.LONG_SLOW_FALLING,

			// Turtle Master (Resistance + Slowness)
			// - Arrow of the Turtle Master
			// - Arrow of the Turtle Master (long)
			// - Arrow of the Turtle Master II
			PotionType.TURTLE_MASTER,
			PotionType.LONG_TURTLE_MASTER,
			PotionType.STRONG_TURTLE_MASTER
	};

	private final ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * Applies a random vanilla tipped arrow effect to the given ItemStack.
	 * @param item Must be a TIPPED_ARROW.
	 */
	public void randomizeTippedArrow(ItemStack item) {
		if (item == null || item.getType() != Material.TIPPED_ARROW) return;

		PotionMeta meta = (PotionMeta) item.getItemMeta();
		if (meta == null) return;

		PotionType type = ARROW_POTION_TYPES[random.nextInt(ARROW_POTION_TYPES.length)];
		meta.setBasePotionType(type);

		item.setItemMeta(meta);
	}
}