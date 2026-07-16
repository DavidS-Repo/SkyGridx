package main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to hide default enchant display and add custom lore with friendly names.
 */
public class EnchantmentNameSupport {
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

	/**
	 * Holds an enchantment choice with level, format type, and lore color.
	 */
	public record ChosenEnchantment(Enchantment enchantment, int level,
			String levelType, String loreColor) { }

	/**
	 * Applies custom enchant display on gear or books.
	 * Hides vanilla list, adds true enchants or stored enchants plus lore.
	 *
	 * @param item           the ItemStack to modify
	 * @param chosenEnchants list of chosen enchants with formatting
	 */
	public static void applyCustomEnchantDisplay(ItemStack item,
			List<ChosenEnchantment> chosenEnchants) {
		if (item == null || chosenEnchants == null || chosenEnchants.isEmpty()) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}

		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		List<Component> newLore = meta.lore() == null
				? new ArrayList<>()
				: new ArrayList<>(meta.lore());

		if (item.getType() == Material.ENCHANTED_BOOK
				&& meta instanceof EnchantmentStorageMeta storage) {
			// store enchants so book works in anvil
			for (ChosenEnchantment c : chosenEnchants) {
				storage.addStoredEnchant(c.enchantment(), c.level(), true);
			}
			item.setItemMeta(storage);
			return;
		}

		// gear: apply enchants and lore
		for (ChosenEnchantment c : chosenEnchants) {
			meta.addEnchant(c.enchantment(), c.level(), true);
			String displayLevel = c.levelType().equalsIgnoreCase("Standard")
					? "lvl_" + c.level()
					: toRoman(c.level());
			String color = getColor(c.loreColor);
			String name = getFriendlyName(c.enchantment());
			newLore.add(LEGACY.deserialize(color + name + " " + displayLevel));
		}

		meta.lore(newLore);
		item.setItemMeta(meta);
	}

	/**
	 * Builds a friendly name from enchant key (split on underscore).
	 * @param enchantment the enchantment
	 * @return e.g. "Fire Aspect"
	 */
	public static String getFriendlyName(Enchantment enchantment) {
		String key = enchantment.getKey().getKey();
		String[] parts = key.split("_");
		StringBuilder out = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) continue;
			out.append(Character.toUpperCase(part.charAt(0)))
			.append(part.substring(1).toLowerCase())
			.append(' ');
		}
		return out.toString().trim();
	}

	/**
	 * Converts integer to Roman numeral (1–3999).
	 * @param number positive int
	 * @return Roman string
	 */
	static String toRoman(int number) {
		if (number <= 0) return String.valueOf(number);
		int[] vals = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
		String[] romans = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < vals.length; i++) {
			while (number >= vals[i]) {
				number -= vals[i];
				r.append(romans[i]);
			}
		}
		return r.toString();
	}

	/**
	 * Maps a color name to its chat code via Cc.
	 * @param colorName e.g. "red" or "DARK_AQUA"
	 * @return chat code like §c or §3
	 */
	public static String getColor(String colorName) {
		if (colorName == null) {
			return Cc.GRAY.getMinecraft();
		}
		switch (colorName.trim().toUpperCase()) {
		case "BLACK":        return Cc.BLACK.getMinecraft();
		case "DARK_BLUE":    return Cc.DARK_BLUE.getMinecraft();
		case "DARK_GREEN":   return Cc.DARK_GREEN.getMinecraft();
		case "DARK_AQUA":    return Cc.DARK_AQUA.getMinecraft();
		case "DARK_RED":     return Cc.DARK_RED.getMinecraft();
		case "DARK_PURPLE":  return Cc.DARK_PURPLE.getMinecraft();
		case "GOLD":         return Cc.GOLD.getMinecraft();
		case "GRAY":         return Cc.GRAY.getMinecraft();
		case "DARK_GRAY":    return Cc.DARK_GRAY.getMinecraft();
		case "BLUE":         return Cc.BLUE.getMinecraft();
		case "GREEN":        return Cc.GREEN.getMinecraft();
		case "AQUA":         return Cc.AQUA.getMinecraft();
		case "RED":          return Cc.RED.getMinecraft();
		case "LIGHT_PURPLE": return Cc.LIGHT_PURPLE.getMinecraft();
		case "YELLOW":       return Cc.YELLOW.getMinecraft();
		case "WHITE":        return Cc.WHITE.getMinecraft();
		default:             return Cc.GRAY.getMinecraft();
		}
	}
}
