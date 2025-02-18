package main;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Utility class to hide the default enchantment display and add custom lore with friendly names.
 */
public class EnchantmentNameSupport {

	// Map of enchantment keys to friendly display names
	private static final Map<String, String> FRIENDLY_NAMES = new HashMap<>();

	static {
		FRIENDLY_NAMES.put("aqua_affinity", "Aqua Affinity");
		FRIENDLY_NAMES.put("bane_of_arthropods", "Bane of Arthropods");
		FRIENDLY_NAMES.put("binding_curse", "Curse of Binding");
		FRIENDLY_NAMES.put("blast_protection", "Blast Protection");
		FRIENDLY_NAMES.put("breach", "Breach");
		FRIENDLY_NAMES.put("channeling", "Channeling");
		FRIENDLY_NAMES.put("density", "Density");
		FRIENDLY_NAMES.put("depth_strider", "Depth Strider");
		FRIENDLY_NAMES.put("efficiency", "Efficiency");
		FRIENDLY_NAMES.put("feather_falling", "Feather Falling");
		FRIENDLY_NAMES.put("fire_aspect", "Fire Aspect");
		FRIENDLY_NAMES.put("fire_protection", "Fire Protection");
		FRIENDLY_NAMES.put("flame", "Flame");
		FRIENDLY_NAMES.put("fortune", "Fortune");
		FRIENDLY_NAMES.put("frost_walker", "Frost Walker");
		FRIENDLY_NAMES.put("impaling", "Impaling");
		FRIENDLY_NAMES.put("infinity", "Infinity");
		FRIENDLY_NAMES.put("knockback", "Knockback");
		FRIENDLY_NAMES.put("looting", "Looting");
		FRIENDLY_NAMES.put("loyalty", "Loyalty");
		FRIENDLY_NAMES.put("luck_of_the_sea", "Luck of the Sea");
		FRIENDLY_NAMES.put("lure", "Lure");
		FRIENDLY_NAMES.put("mending", "Mending");
		FRIENDLY_NAMES.put("multishot", "Multishot");
		FRIENDLY_NAMES.put("piercing", "Piercing");
		FRIENDLY_NAMES.put("power", "Power");
		FRIENDLY_NAMES.put("projectile_protection", "Projectile Protection");
		FRIENDLY_NAMES.put("protection", "Protection");
		FRIENDLY_NAMES.put("punch", "Punch");
		FRIENDLY_NAMES.put("quick_charge", "Quick Charge");
		FRIENDLY_NAMES.put("respiration", "Respiration");
		FRIENDLY_NAMES.put("riptide", "Riptide");
		FRIENDLY_NAMES.put("sharpness", "Sharpness");
		FRIENDLY_NAMES.put("silk_touch", "Silk Touch");
		FRIENDLY_NAMES.put("smite", "Smite");
		FRIENDLY_NAMES.put("soul_speed", "Soul Speed");
		FRIENDLY_NAMES.put("sweeping_edge", "Sweeping Edge");
		FRIENDLY_NAMES.put("swift_sneak", "Swift Sneak");
		FRIENDLY_NAMES.put("thorns", "Thorns");
		FRIENDLY_NAMES.put("unbreaking", "Unbreaking");
		FRIENDLY_NAMES.put("vanishing_curse", "Curse of Vanishing");
		FRIENDLY_NAMES.put("wind_burst", "Wind Burst");
	}

	/**
	 * Record representing an enchantment with its chosen level and formatting settings.
	 */
	public record ChosenEnchantment(Enchantment enchantment, int level, String levelType, String loreColor) { }

	/**
	 * Applies the given chosen enchantments to the item:
	 * <ul>
	 *   <li>Adds the enchantments (so the effects are applied).</li>
	 *   <li>Hides the default enchantment display using ItemFlag.HIDE_ENCHANTS.</li>
	 *   <li>Adds custom lore lines showing friendly names with appropriate level formatting.</li>
	 * </ul>
	 *
	 * @param item           The ItemStack to modify.
	 * @param chosenEnchants The list of chosen enchantments.
	 */
	@SuppressWarnings("deprecation")
	public static void applyCustomEnchantDisplay(ItemStack item, List<ChosenEnchantment> chosenEnchants) {
		if (item == null || chosenEnchants == null || chosenEnchants.isEmpty()) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		List<String> newLore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
		for (ChosenEnchantment chosen : chosenEnchants) {
			meta.addEnchant(chosen.enchantment(), chosen.level(), true);
			String displayLevel;
			if ("Standard".equalsIgnoreCase(chosen.levelType())) {
				displayLevel = "lvl_" + chosen.level();
			} else {
				displayLevel = toRoman(chosen.level());
			}
			String color = getColor(chosen.loreColor());
			String friendlyName = getFriendlyName(chosen.enchantment());
			newLore.add(color + friendlyName + " " + displayLevel);
		}
		meta.setLore(newLore);
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves a friendly name for the given enchantment.
	 *
	 * @param enchantment The enchantment.
	 * @return The friendly display name.
	 */
	private static String getFriendlyName(Enchantment enchantment) {
		String key = enchantment.getKey().getKey();
		return FRIENDLY_NAMES.getOrDefault(key, capitalize(key));
	}

	/**
	 * Capitalizes the first letter of the string.
	 *
	 * @param input The input string.
	 * @return The capitalized string.
	 */
	private static String capitalize(String input) {
		if (input == null || input.isEmpty()) return input;
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	/**
	 * Converts an integer to a Roman numeral string (supports numbers 1–3999).
	 *
	 * @param number The number to convert.
	 * @return The Roman numeral representation.
	 */
	private static String toRoman(int number) {
		if (number <= 0) return String.valueOf(number);
		int[] values =   {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
		String[] romans = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			while (number >= values[i]) {
				number -= values[i];
				result.append(romans[i]);
			}
		}
		return result.toString();
	}

	/**
	 * Returns the Minecraft color code corresponding to the provided color name.
	 * Accepts mixed-case input (for example, "ReD" or "blue") and returns the proper code.
	 *
	 * @param colorName The color name as defined in your config.
	 * @return The Minecraft color code string.
	 */
	private static String getColor(String colorName) {
		if (colorName == null || colorName.isEmpty()) {
			return Cc.GRAY.getMinecraft();
		}
		return switch (colorName.trim().toUpperCase()) {
		case "BLACK" -> Cc.BLACK.getMinecraft();
		case "DARK_BLUE" -> Cc.DARK_BLUE.getMinecraft();
		case "DARK_GREEN" -> Cc.DARK_GREEN.getMinecraft();
		case "DARK_AQUA" -> Cc.DARK_AQUA.getMinecraft();
		case "DARK_RED" -> Cc.DARK_RED.getMinecraft();
		case "DARK_PURPLE" -> Cc.DARK_PURPLE.getMinecraft();
		case "GOLD" -> Cc.GOLD.getMinecraft();
		case "GRAY" -> Cc.GRAY.getMinecraft();
		case "DARK_GRAY" -> Cc.DARK_GRAY.getMinecraft();
		case "BLUE" -> Cc.BLUE.getMinecraft();
		case "GREEN" -> Cc.GREEN.getMinecraft();
		case "AQUA" -> Cc.AQUA.getMinecraft();
		case "RED" -> Cc.RED.getMinecraft();
		case "LIGHT_PURPLE" -> Cc.LIGHT_PURPLE.getMinecraft();
		case "YELLOW" -> Cc.YELLOW.getMinecraft();
		case "WHITE" -> Cc.WHITE.getMinecraft();
		default -> Cc.GRAY.getMinecraft();
		};
	}
}