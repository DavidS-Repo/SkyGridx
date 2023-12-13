package main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OreGen implements Listener {
	private final JavaPlugin plugin;
	private final Random random;
	private final Map<Material, Double> oreChances;

	public OreGen(JavaPlugin plugin) {
		this.plugin = plugin;
		this.random = new Random();
		this.oreChances = new HashMap<>();
		loadOreChances();
	}

	// Method to load ore chances from the "ores.txt" file
	private void loadOreChances() {
		File file = new File(plugin.getDataFolder(), "OreGenBlock/ores.txt");
		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						// Skip empty lines or lines starting with '#'
						continue;
					}
					String[] parts = line.split(":");
					if (parts.length == 2) {
						Material oreMaterial = Material.getMaterial(parts[0]);
						double chance = Double.parseDouble(parts[1]) / 100.0;
						if (oreMaterial != null && chance > 0 && chance <= 1) {
							oreChances.put(oreMaterial, chance);
						} else {
							plugin.getLogger().warning("Invalid ore data in ores.txt: " + line);
						}
					} else {
						plugin.getLogger().warning("Invalid line in ores.txt: " + line);
					}
				}
			} catch (IOException | NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			plugin.getLogger().warning("Missing ores.txt file. No ores will be generated.");
		}
	}

	@EventHandler
	public void onBlockForm(BlockFormEvent event) {
		Block block = event.getBlock();
		Material toType = event.getNewState().getType();
		if (toType == Material.STONE) {
			Material fromType = block.getType();
			if (fromType == Material.LAVA || fromType == Material.WATER) {
				Material oreMaterial = getRandomOreMaterial();
				if (oreMaterial != null) {
					event.setCancelled(true); // Cancel the block form event
					block.setType(oreMaterial, true); // Set the chosen ore block at the same location
				}
			}
		}
	}

	private Material getRandomOreMaterial() {
		double totalChance = 0;
		for (double chance : oreChances.values()) {
			totalChance += chance;
		}
		double randomValue = random.nextDouble() * totalChance;
		for (Map.Entry<Material, Double> entry : oreChances.entrySet()) {
			randomValue -= entry.getValue();
			if (randomValue <= 0) {
				return entry.getKey();
			}
		}
		return null;
	}
}