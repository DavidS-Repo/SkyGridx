// PortalLocationManager.java
package main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary‐backed portal manager:
 * - portals.dat is a sequence of records [nameLen:int, name:UTF-8 bytes, x:double, y:double, z:double]
 * - load reads all entries via DataInputStream
 * - addPortal appends one record via DataOutputStream
 */
public class PortalLocationManager {
	private final JavaPlugin plugin;
	private final File dataFile;
	private final List<Location> portalLocations = new ArrayList<>();

	public PortalLocationManager(JavaPlugin plugin) {
		this.plugin = plugin;
		this.dataFile = new File(plugin.getDataFolder(), "portals.dat");
	}

	/** Ensure folder + file exist, then bulk‐load all portal coords */
	public void initialize() {
		plugin.getDataFolder().mkdirs();
		try {
			if (!dataFile.exists()) dataFile.createNewFile();
			loadFromFile();
		} catch (IOException ex) {
			plugin.getLogger().severe("Failed to init portals.dat: " + ex.getMessage());
		}
	}

	/** Read every record: [int nameLen][bytes name][double x][double y][double z] */
	private void loadFromFile() {
		portalLocations.clear();
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(dataFile)))) {
			while (true) {
				int nameLen = in.readInt();
				byte[] buf = new byte[nameLen];
				in.readFully(buf);
				String worldName = new String(buf, StandardCharsets.UTF_8);

				double x = in.readDouble();
				double y = in.readDouble();
				double z = in.readDouble();

				World w = Bukkit.getWorld(worldName);
				if (w != null) {
					portalLocations.add(new Location(w, x, y, z));
				}
			}
		} catch (EOFException eof) {
			// normal—hit end of file
		} catch (IOException ex) {
			plugin.getLogger().severe("Error reading portals.dat: " + ex.getMessage());
		}
	}

	/**
	 * Append a new portal record to portals.dat and add to memory.
	 */
	public synchronized void addPortal(Location loc) {
		portalLocations.add(loc);
		byte[] nameBytes = loc.getWorld().getName().getBytes(StandardCharsets.UTF_8);

		try (DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(dataFile, true)
						)
				)) {
			out.writeInt(nameBytes.length);
			out.write(nameBytes);
			out.writeDouble(loc.getX());
			out.writeDouble(loc.getY());
			out.writeDouble(loc.getZ());
		} catch (IOException ex) {
			plugin.getLogger().severe("Could not write portal to portals.dat: " + ex.getMessage());
		}
	}

	/**
	 * Find the nearest portal (same world) to the given Location.
	 * Returns null if none loaded.
	 */
	public synchronized Location getNearest(Location from) {
		Location best = null;
		double bestDist2 = Double.MAX_VALUE;
		for (Location portal : portalLocations) {
			if (!portal.getWorld().equals(from.getWorld())) continue;
			double d2 = portal.distanceSquared(from);
			if (d2 < bestDist2) {
				bestDist2 = d2;
				best = portal;
			}
		}
		return best;
	}

	/**
	 * Clears all loaded portals and wipes the data file clean.
	 * Can be called from anywhere you’ve got a reference to this manager.
	 */
	public synchronized void clearAllPortals() {
		portalLocations.clear();
		try (FileOutputStream out = new FileOutputStream(dataFile, false)) {
		} catch (IOException ex) {
			plugin.getLogger().severe("Failed to clear portals.dat: " + ex.getMessage());
		}
	}
}
