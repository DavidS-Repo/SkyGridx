package main;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class PreGenerator {
    private boolean enabled = false;
    private Timer timer;
    private final JavaPlugin plugin;
    private int chunksPerRun, printTime;
    private int chunksPerSec;

    private static final String[] worldNames = {"overworld", "nether", "end"};
    private final Map<String, WorldData> worldDataMap = new HashMap<>();
    
    private static final String ENABLED_WARNING_MESSAGE = ChatColor.YELLOW + ("Pre-Generator is already enabled.");
	private static final String ENABLED_MESSAGE = ChatColor.GREEN + ("Pre-generation has been enabled.");
	private static final String DISABLED_WARNING_MESSAGE = ChatColor.YELLOW + ("Pre-Generator is already disabled.");
	private static final String DISABLED_MESSAGE = ChatColor.RED + ("Pre-generation disabled.");

    private class WorldData {
        final String dataFileName;
        final World world;
        int x = 0, z = 0, dx = 0, dz = -1, chunkPerMin = 0, chunksPerSec = 0;
        long totalChunksProcessed = 0L, totalWorldChunks = 14062361500009L;

        WorldData(String dataFileName, World world) {
            this.dataFileName = dataFileName;
            this.world = world;
        }
    }

    public PreGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldDataMap.put("overworld", new WorldData("overworld_pregenerator_data.txt", plugin.getServer().getWorlds().get(0)));
        this.worldDataMap.put("nether", new WorldData("nether_pregenerator_data.txt", plugin.getServer().getWorlds().get(1)));
        this.worldDataMap.put("end", new WorldData("end_pregenerator_data.txt", plugin.getServer().getWorlds().get(2)));
    }

    public void enable() {
        if (enabled) {
            Bukkit.broadcastMessage(ENABLED_WARNING_MESSAGE);
            return;
        }
        enabled = true;
        Bukkit.broadcastMessage(ENABLED_MESSAGE);
        loadProcessedChunks();
        startGeneration();
        startPrintInfoTimer();
    }

    public void disable() {
        if (!enabled) {
            Bukkit.broadcastMessage(DISABLED_WARNING_MESSAGE);
            return;
        }
        saveProcessedChunks();
        stopPrintInfoTimer();
        enabled = false;
        Bukkit.broadcastMessage(DISABLED_MESSAGE);
    }

    private void startPrintInfoTimer() {
        if (timer == null) {
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    printInfo();
                }
            }, 0, (printTime * 60000)); // printTime * 60,000 milliseconds (60 seconds)
        }
    }

    private void stopPrintInfoTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public void setValues(int chunksPerRun, int printTime) {
        this.chunksPerRun = chunksPerRun;
        this.printTime = printTime;
    }

    private void startGeneration() {
        for (String worldName : worldNames) {
            generateChunkBatch(worldName);
        }
    }

    private void generateChunkBatch(String worldName) {
        WorldData data = worldDataMap.get(worldName);
        if (!enabled || data == null) {
            return;
        }

        CompletableFuture<Void> allChunksLoaded = CompletableFuture.completedFuture(null);

        for (int i = 0; i < chunksPerRun; i++) {
            if (!enabled) {
                break;
            }

            allChunksLoaded = allChunksLoaded.thenComposeAsync(ignored ->
                PaperLib.getChunkAtAsync(data.world, data.x, data.z, true)
                    .thenAcceptAsync(chunk -> unloadChunkAsync(chunk, data.world))
            ).thenComposeAsync(ignored -> CompletableFuture.runAsync(() -> {
                data.totalChunksProcessed++;
                data.chunkPerMin++;

                if (data.x == data.z || (data.x < 0 && data.x == -data.z) || (data.x > 0 && data.x == 1 - data.z)) {
                    int temp = data.dx;
                    data.dx = -data.dz;
                    data.dz = temp;
                }
                data.x += data.dx;
                data.z += data.dz;
            }));

            if (data.totalChunksProcessed >= data.totalWorldChunks) {
                disable();
                break;
            }
        }

        allChunksLoaded.thenRun(() -> {
            if (enabled) {
                saveProcessedChunks(worldName);
                generateChunkBatch(worldName);
            }
        });
    }

    private void unloadChunkAsync(Chunk chunk, World world) {
        Bukkit.getScheduler().runTask(plugin, () -> unloadChunk(chunk, world));
    }

    private void unloadChunk(Chunk chunk, World world) {
        if (world.isChunkLoaded(chunk.getX(), chunk.getZ())) {
            world.unloadChunk(chunk.getX(), chunk.getZ(), true);
        }
    }

    private void printInfo() {
        chunksPerSec = 0;

        for (String worldName : worldNames) {
            WorldData data = worldDataMap.get(worldName);
            if (data != null) {
                data.chunksPerSec = (data.chunkPerMin / (60 * printTime));
                chunksPerSec += data.chunksPerSec;

                data.chunksPerSec = 0;
                data.chunkPerMin = 0;
            }
        }

        Bukkit.broadcastMessage("Pregen Chunks/Sec: " + chunksPerSec);
    }

    private void saveProcessedChunks() {
        for (String worldName : worldNames) {
            saveProcessedChunks(worldName);
        }
    }

    private void saveProcessedChunks(String worldName) {
        WorldData data = worldDataMap.get(worldName);
        if (data == null) {
            return;
        }

        File dataFile = new File(plugin.getDataFolder(), data.dataFileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile, false))) {
            writer.println(data.x + "_" + data.z + "_" + data.dx + "_" + data.dz);
            writer.println(data.totalChunksProcessed);
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.broadcastMessage("Total Processed " + worldName + " Chunks " + data.totalChunksProcessed + ".");
        }
    }

    private void loadProcessedChunks() {
        for (String worldName : worldNames) {
            loadProcessedChunks(worldName);
        }
    }

    private void loadProcessedChunks(String worldName) {
        WorldData data = worldDataMap.get(worldName);
        if (data == null) {
            return;
        }

        File dataFile = new File(plugin.getDataFolder(), data.dataFileName);

        if (dataFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String lastLine = reader.readLine();

                while (lastLine != null) {
                    String[] coords = lastLine.split("_");
                    data.x = Integer.parseInt(coords[0]);
                    data.z = Integer.parseInt(coords[1]);
                    data.dx = Integer.parseInt(coords[2]);
                    data.dz = Integer.parseInt(coords[3]);

                    String totalChunksLine = reader.readLine();

                    if (totalChunksLine != null) {
                        data.totalChunksProcessed = Long.parseLong(totalChunksLine);
                    }
                    lastLine = reader.readLine();
                }
                Bukkit.broadcastMessage("Loaded " + data.totalChunksProcessed + " processed chunks from: " + dataFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
