package main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class OreGen implements Listener {
	private final JavaPlugin plugin;
	private final Map<Material, List<OreChance>> oreChances;
	private final Map<Material, Double> totalChances;
	private final Path configFilePath;

	public OreGen(JavaPlugin plugin) {
		this.plugin = plugin;
		this.oreChances = new HashMap<>();
		this.totalChances = new HashMap<>();
		this.configFilePath = plugin.getDataFolder().toPath().resolve("OreGenBlock/ores.yml");
	}

	private void loadOreChances() {
		if (Files.exists(configFilePath)) {
			List<String> warnings = new ArrayList<>();
			try {
				List<String> lines = Files.readAllLines(configFilePath);
				Material currentBlockType = null;
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					String[] parts = line.split(":");
					if (parts.length == 1) {
						currentBlockType = Material.getMaterial(parts[0].trim());
						if (currentBlockType == null) {
							warnings.add("Invalid block type in ores.yml: " + parts[0].trim());
						}
					} else if (parts.length == 2 && currentBlockType != null) {
						Material oreMaterial = Material.getMaterial(parts[0].trim());
						double chance = Double.parseDouble(parts[1].trim()) / 100.0;
						if (oreMaterial != null && chance > 0 && chance <= 1) {
							oreChances.computeIfAbsent(currentBlockType, k -> new ArrayList<>())
							.add(new OreChance(oreMaterial, chance));
						} else {
							warnings.add("Invalid ore data in ores.yml: " + line);
						}
					} else {
						warnings.add("Invalid line in ores.yml: " + line);
					}
				}
				oreChances.forEach(this::calculateCumulativeProbabilities);
			} catch (IOException | NumberFormatException e) {
				e.printStackTrace();
			}
			warnings.forEach(plugin.getLogger()::warning);
		} else {
			plugin.getLogger().warning("Missing ores.yml file. No ores will be generated.");
		}
	}

	private void calculateCumulativeProbabilities(Material blockType, List<OreChance> chances) {
		double cumulativeProbability = 0.0;
		for (OreChance oreChance : chances) {
			cumulativeProbability += oreChance.chance;
			oreChance.cumulativeChance = cumulativeProbability;
		}
		totalChances.put(blockType, cumulativeProbability);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockForm(BlockFormEvent event) {
		Block block = event.getBlock();
		Material toType = event.getNewState().getType();
		Material fromType = block.getType();
		if ((toType == Material.STONE || toType == Material.COBBLESTONE || toType == Material.BASALT) && fromType == Material.LAVA) {
			handleOreGeneration(event, toType);
		}
	}

	private void handleOreGeneration(BlockFormEvent event, Material toType) {
		if (oreChances.isEmpty()) {
			loadOreChances();
		}
		Material replacement = determineReplacement(toType);
		if (replacement != null) {
			event.setCancelled(true);
			event.getBlock().setType(replacement, true);
		}
	}

	private Material determineReplacement(Material blockType) {
		List<OreChance> chances = oreChances.get(blockType);
		if (chances == null || chances.isEmpty()) {
			return null;
		}
		double totalChance = totalChances.get(blockType);
		double randomValue = ThreadLocalRandom.current().nextDouble() * totalChance;
		for (OreChance chance : chances) {
			if (randomValue < chance.cumulativeChance) {
				return chance.material;
			}
		}
		return null;
	}

	static class OreChance {
		public final Material material;
		public final double chance;
		public double cumulativeChance;

		public OreChance(Material material, double chance) {
			this.material = material;
			this.chance = chance;
		}
	}
}