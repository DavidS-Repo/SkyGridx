package main;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * The Cc class provides utility methods and constants for handling console colors
 * and formatted messaging within the Minecraft server environment.
 * 
 * This version allows the use of color constants like `cc.RED` which automatically
 * switch between ANSI codes for the console and Minecraft color codes for in-game messages.
 */
public class Cc {

	// Define color constants that dynamically switch between ANSI and Minecraft color codes
	public static final colorCode RESET = new colorCode("\033[0m", "§r");
	public static final colorCode BLACK = new colorCode("\033[0;30m", "§0");
	public static final colorCode DARK_BLUE = new colorCode("\033[0;34m", "§1");
	public static final colorCode DARK_GREEN = new colorCode("\033[0;32m", "§2");
	public static final colorCode DARK_AQUA = new colorCode("\033[0;36m", "§3");
	public static final colorCode DARK_RED = new colorCode("\033[0;31m", "§4");
	public static final colorCode DARK_PURPLE = new colorCode("\033[0;35m", "§5");
	public static final colorCode GOLD = new colorCode("\033[0;33m", "§6");
	public static final colorCode GRAY = new colorCode("\033[0;37m", "§7");
	public static final colorCode DARK_GRAY = new colorCode("\033[1;30m", "§8");
	public static final colorCode BLUE = new colorCode("\033[1;34m", "§9");
	public static final colorCode GREEN = new colorCode("\033[1;32m", "§a");
	public static final colorCode AQUA = new colorCode("\033[1;36m", "§b");
	public static final colorCode RED = new colorCode("\033[1;31m", "§c");
	public static final colorCode LIGHT_PURPLE = new colorCode("\033[1;35m", "§d");
	public static final colorCode YELLOW = new colorCode("\033[1;33m", "§e");
	public static final colorCode WHITE = new colorCode("\033[1;37m", "§f");

	/**
	 * Logs a colored message to the server console using ANSI colors.
	 * 
	 * @param color The colorCode object for the desired color.
	 * @param message The message to be logged.
	 */
	public static void logS(colorCode color, String message) {
		Bukkit.getLogger().info(color.getAnsi() + message + RESET.getAnsi());
	}

	/**
	 * Logs a plain message to the server console without any color formatting.
	 * 
	 * @param message The message to be logged.
	 */
	public static void logSB(String message) {
		Bukkit.getLogger().info(message);
	}

	/**
	 * Sends a colored message to a specific CommandSender (player or console).
	 * Automatically uses ANSI codes for ConsoleCommandSender and Minecraft color codes for in-game CommandSenders.
	 * 
	 * @param sender The recipient of the message.
	 * @param color The colorCode object for the desired color.
	 * @param message The message to be sent.
	 */
	public static void sendS(CommandSender sender, colorCode color, String message) {
		if (sender instanceof ConsoleCommandSender) {
			// Send ANSI color-coded message to console
			sender.sendMessage(color.getAnsi() + message + RESET.getAnsi());
		} else {
			// Send Minecraft color-coded message to in-game CommandSender
			sender.sendMessage(color.getMinecraft() + message + RESET.getMinecraft());
		}
	}

	/**
	 * Formats an object into a colored string for console display with ANSI codes.
	 * 
	 * @param color The colorCode object for the desired color.
	 * @param value The object to be formatted.
	 * @return A string representation of the object with the specified color in ANSI format.
	 */
	public static String logO(colorCode color, Object value) {
		return color.getAnsi() + String.valueOf(value) + RESET.getAnsi();
	}

	/**
	 * Formats an object into a fixed-width colored string for console display with ANSI codes.
	 * 
	 * @param color The colorCode object for the desired color.
	 * @param value The object to be formatted.
	 * @param width The desired width of the formatted string.
	 * @return A fixed-width string representation of the object with the specified color in ANSI format.
	 */
	public static String fA(colorCode color, Object value, int width) {
		String formattedValue = String.format("%-" + width + "s", value);
		return color.getAnsi() + formattedValue + RESET.getAnsi();
	}

	/**
	 * Pads a world name with spaces to ensure consistent formatting.
	 * 
	 * @param worldName The name of the world to pad.
	 * @param maxLength The total desired length of the padded string.
	 * @return The padded world name enclosed in brackets.
	 */
	public static String padWorldName(String worldName, int maxLength) {
		int paddingLength = maxLength - worldName.length();
		String padding = " ".repeat(Math.max(0, paddingLength));
		return "[" + worldName + "]" + padding;
	}

	/**
	 * Inner class to store both ANSI and Minecraft color codes.
	 */
	public static class colorCode {
		private final String ansi;
		private final String minecraft;

		public colorCode(String ansi, String minecraft) {
			this.ansi = ansi;
			this.minecraft = minecraft;
		}

		public String getAnsi() {
			return ansi;
		}

		public String getMinecraft() {
			return minecraft;
		}
	}
}