package main;

import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class LootTableSelector {

	/**
	 * Chooses a LootTable based on weighted probabilities.
	 * Expects a list of maps where each map has a key equal to the loot table name
	 * (as defined in the LootTables enum)
	 * and a value that is a list containing a map with a "Weight" property.
	 */
	public static LootTable chooseWeightedLootTable(List<Map<?, ?>> lootTablesConfig) {
		if (lootTablesConfig == null || lootTablesConfig.isEmpty()) {
			return null;
		}

		double totalWeight = 0.0;
		// Sum all weights.
		for (Map<?, ?> lootTableEntry : lootTablesConfig) {
			for (Object key : lootTableEntry.keySet()) {
				Object value = lootTableEntry.get(key);
				if (value instanceof List<?> list && !list.isEmpty()) {
					Object firstEntry = list.get(0);
					if (firstEntry instanceof Map<?, ?> weightMap && weightMap.containsKey("Weight")) {
						try {
							double weight = Double.parseDouble(weightMap.get("Weight").toString());
							totalWeight += weight;
						} catch (NumberFormatException ignored) { }
					}
				}
			}
		}

		if (totalWeight <= 0) {
			return null;
		}

		double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
		double cumulativeWeight = 0.0;

		// Select loot table based on cumulative weights.
		for (Map<?, ?> lootTableEntry : lootTablesConfig) {
			for (Object keyObj : lootTableEntry.keySet()) {
				String lootTableName = keyObj.toString();
				double weight = 0.0;
				Object value = lootTableEntry.get(keyObj);
				if (value instanceof List<?> list && !list.isEmpty()) {
					Object firstEntry = list.get(0);
					if (firstEntry instanceof Map<?, ?> weightMap && weightMap.containsKey("Weight")) {
						try {
							weight = Double.parseDouble(weightMap.get("Weight").toString());
						} catch (NumberFormatException ignored) { }
					}
				}
				cumulativeWeight += weight;
				if (randomValue < cumulativeWeight) {
					try {
						// Use the LootTables enum to retrieve the LootTable.
						return LootTables.valueOf(lootTableName).getLootTable();
					} catch (IllegalArgumentException e) {
						// Invalid key from config, returns null
						return null;
					}
				}
			}
		}
		return null;
	}
}
