package main;

import org.bukkit.Material;

public class ItemSettings {
	Material material;
	double weight;
	int maxAmount;

	ItemSettings(Material material, double weight, int maxAmount) {
		this.material = material;
		this.weight = weight;
		this.maxAmount = maxAmount;
	}
}