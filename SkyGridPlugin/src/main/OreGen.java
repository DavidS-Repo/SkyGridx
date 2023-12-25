package main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class OreGen implements Listener {
    private final JavaPlugin plugin;
    private final Map<Material, Double> stoneChances;
    private final Map<Material, Double> cobblestoneChances;

    public OreGen(JavaPlugin plugin) {
        this.plugin = plugin;
        this.stoneChances = new HashMap<>();
        this.cobblestoneChances = new HashMap<>();
        loadOreChances();
    }

    private void loadOreChances() {
        File configFile = new File(plugin.getDataFolder(), "OreGenBlock/ores.yml");
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                String currentBlockType = "";
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split(":");
                    if (parts.length == 1) {
                        currentBlockType = parts[0].trim();
                    } else if (parts.length == 2) {
                        Material oreMaterial = Material.getMaterial(parts[0].trim());
                        double chance = Double.parseDouble(parts[1].trim()) / 100.0;
                        if (oreMaterial != null && chance > 0 && chance <= 1) {
                            if ("STONE".equalsIgnoreCase(currentBlockType)) {
                                stoneChances.put(oreMaterial, chance);
                            } else if ("COBBLESTONE".equalsIgnoreCase(currentBlockType)) {
                                cobblestoneChances.put(oreMaterial, chance);
                            }
                        } else {
                            plugin.getLogger().warning("Invalid ore data in ores.yml: " + line);
                        }
                    } else {
                        plugin.getLogger().warning("Invalid line in ores.yml: " + line);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().warning("Missing ores.yml file. No ores will be generated.");
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        Block block = event.getBlock();
        Material toType = event.getNewState().getType();
        Material fromType = block.getType();
        if ((toType == Material.STONE || toType == Material.COBBLESTONE) &&
                (fromType == Material.LAVA || fromType == Material.WATER)) {
            Material replacement = determineReplacement(toType);
            if (replacement != null) {
                event.setCancelled(true);
                block.setType(replacement, true);
            }
        }
    }

    private Material determineReplacement(Material blockType) {
        if (blockType == Material.STONE) {
            return getRandomOreMaterial(stoneChances);
        } else if (blockType == Material.COBBLESTONE) {
            return getRandomOreMaterial(cobblestoneChances);
        }
        return null;
    }

    private Material getRandomOreMaterial(Map<Material, Double> oreChances) {
        double totalChance = oreChances.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = Math.random() * totalChance;
        for (Map.Entry<Material, Double> entry : oreChances.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0) {
                return entry.getKey();
            }
        }
        return null;
    }
}
