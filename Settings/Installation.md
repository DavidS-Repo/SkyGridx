# Installation Instructions:
 
1. **Prepare Server Launch:**
 - Run the following commands within a **.bat** file to initialize the server:

```
@echo off
java -Xms1G -Xmx4G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -XX:+UseNUMA -XX:ParallelGCThreads=6 -XX:ConcGCThreads=6 -XX:MaxGCPauseMillis=200 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar NameOfJar.jar --nogui
pause
```
 - Remember to adjust memory settings to suit your server and replace **NameOfJar.jar** with your server's actual jar file name.

![instrunctions step 1](https://www.toolsnexus.com/mc/1.png)
![instrunctions step 2](https://www.toolsnexus.com/mc/2.png)

2. **Sign the EULA:**
 - Once the EULA is signed, refrain from starting the server yet.

![instrunctions step 3](https://www.toolsnexus.com/mc/3.png)

3. **Download Plugin and Datapack:**
 - Obtain the latest version of the plugin from [SkyGridX](https://modrinth.com/plugin/skygridx/versions) and place it into the auto-generated `plugins` folder.

![instrunctions step 4](https://www.toolsnexus.com/mc/4.png)
![instrunctions step 5](https://www.toolsnexus.com/mc/5.png)

 - Create a new folder named `world` within the server directory.

![instrunctions step 6](https://www.toolsnexus.com/mc/6.png)
![instrunctions step 7](https://www.toolsnexus.com/mc/7.png)

 - Inside `world,` create a folder named `datapacks` and add the datapack downloaded from 
[Skyblock Void Worldgen](https://modrinth.com/datapack/skyblock-void-worldgen) which includes biomes, boundary boxes for structures and some structures, or
[Void-Biomes-1.21.03.zip](https://github.com/DavidS-Repo/SkyGridx/blob/main/Void-Biomes-1.21.03.zip), which is made by me, is lightweight, and will reduce chunk generation load when compared to `Skyblock Void Worldgen` and only includes biome data and the end portal and pillars.

![instrunctions step 8](https://www.toolsnexus.com/mc/8.png)
![instrunctions step 9](https://www.toolsnexus.com/mc/9.png)
![instrunctions step 10](https://www.toolsnexus.com/mc/10.png)

> Note: Ensure the datapack is placed here before launching the server to generate the world.

4. **Server Launch & World Generation:**
 - Re-run the **.bat** file to start the server and allow all necessary files to generate.
 - Watch for console messages indicating you to wait.
 
![instrunctions step 11](https://www.toolsnexus.com/mc/pl.png)
 
5. **Enjoy the SkyGrid World:**
 - Connect to the server to verify completion after ([INFO]: Chunks have been loaded. You can now connect!).

![instrunctions step 12](https://www.toolsnexus.com/mc/comp.png)

# A few additional notes to consider:
 
- The SkyGrid plugin requires a Bukkit or Spigot server for optimal performance and functionality. 
- I would recommend paper since its more optimized a some features like the pregenerator will work best with it.
- If you run into any issues or have feature request, make sure to submit them in the issues page of the [github](https://github.com/DavidS-Repo/SkyGridx/issues).
