# SkyGridX Installation Guide for Minecraft 1.21.3 (Java 21)

Follow these steps to set up your SkyGridX plugin efficiently. This guide includes two options for installation: a quick automated setup or a detailed manual setup.

---

## **Quick Setup (Lazy Install)**

If you'd prefer an automated process, you can use the `install.bat` script. This script will:

- Create the required folders (`plugins`, `world`, `datapacks`).
- Download and place the latest versions of the plugin([SkyGridX](https://modrinth.com/plugin/skygridx/versions)), datapack([Void-Biomes-1.21.04.zip](https://github.com/DavidS-Repo/SkyGridx/blob/main/DataPacks/Void-Biomes-1.21.04.zip)), and Paper server JAR([Paper Server](https://papermc.io/downloads/paper)).
- Automatically sign the `eula.txt`.
- Generate a `run.bat` file with default memory settings: `-Xms1G -Xmx4G`.

### Steps:
1. [Download the `install.bat` script](https://github.com/DavidS-Repo/SkyGridx/blob/main/install.bat).
2. Place the script in your server directory.
3. Run the script by double-clicking it.
4. Use the generated `run.bat` to start the server in the future.

> **Note:**
> - You can adjust the memory allocation (`-Xms1G -Xmx4G`) in the `run.bat` file to increase or decrease server memory usage based on your machine's resources.
> - If you are hosting this for yourself only your sever address will be `localhost`, you don't need to open any ports or anything if its only for you

---

## **Manual Setup**

For those who prefer more control over the installation, follow the steps below:

### **1. Prepare Server Launch**
1. Create a `.bat` file and paste the following commands to initialize the server:

    ```batch
    @echo off
    java -Xms1G -Xmx4G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -XX:+UseNUMA -XX:ParallelGCThreads=6 -XX:ConcGCThreads=6 -XX:MaxGCPauseMillis=200 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar NameOfJar.jar --nogui
    pause
    ```

2. Adjust memory settings to suit your server. Replace `NameOfJar.jar` with your server's actual JAR file name.

    ![Create Batch File](https://www.toolsnexus.com/mc/1.png)

3. Save the `.bat` file and run it to initialize the server.

    ![Running Batch File](https://www.toolsnexus.com/mc/2.png)

---

### **2. Sign the EULA**
1. After running the `.bat` file, locate and open the `eula.txt` file in the server directory.
2. Change `eula=false` to `eula=true` and save the file.

    ![EULA Edit](https://www.toolsnexus.com/mc/3.png)

> **Do not restart the server yet.** Continue with the next steps.

---

### **3. Download and Set Up Plugin & Datapack**

#### **Plugin Setup**
1. Download the latest version of the SkyGridX plugin from [SkyGridX](https://modrinth.com/plugin/skygridx/versions).
2. Place the downloaded `.jar` file into the `plugins` folder of your server.

    ![Plugin Folder](https://www.toolsnexus.com/mc/4.png)
    ![Plugin Placement](https://www.toolsnexus.com/mc/5.png)

#### **Datapack Setup**
1. In the server directory, create a folder named `world`.
2. Inside the `world` folder, create a folder named `datapacks`.

    ![Datapack Folder Structure](https://www.toolsnexus.com/mc/6.png)

3. Choose and download one of the following datapacks:
   - **Option 1: Skyblock Void Worldgen**
     - **Features:** Biome data, structure bounding boxes, and some structures.
     - **Performance Impact:** Higher server load due to additional data and features.
     - **Download Link:** [Skyblock Void Worldgen](https://modrinth.com/datapack/skyblock-void-worldgen)
   - **Option 2: Void-Biomes-1.21.03.zip**
     - **Features:** Lightweight alternative with only biome data and the End dragon structure (portal and pillars).
     - **Performance Impact:** Lower server load.
     - **Download Link:** [Void-Biomes-1.21.04.zip](https://github.com/DavidS-Repo/SkyGridx/blob/main/DataPacks/Void-Biomes-1.21.04.zip)

4. Place the downloaded datapack into the `datapacks` folder.

    ![Datapack Placement](https://www.toolsnexus.com/mc/7.png)
    ![Final Datapack Setup](https://www.toolsnexus.com/mc/8.png)

> **Important:** Ensure the datapack is placed in the folder before starting the server to correctly generate the world.

---

### **4. Server Launch & World Generation**
1. Re-run the `.bat` file to start the server and generate the necessary files.
2. Monitor the console output and wait for messages indicating the server is ready.
3. Be patient during world generation, as it may take some time depending on your datapack choice.

    ![Server Console Output](https://www.toolsnexus.com/mc/pl.png)

---

### **5. Enjoy the SkyGrid World**
1. Connect to your server to explore your custom SkyGrid world.
2. Verify successful setup with a console message: `[INFO]: Chunks have been loaded. You can now connect!`

    ![Server Ready Message](https://www.toolsnexus.com/mc/comp.png)

---

## **Additional Notes**
- The SkyGrid plugin requires a Bukkit or Spigot-based server, for optimal performance use Paper or Purpur servers.
- For issues or feature requests, visit the [GitHub Issues Page](https://github.com/DavidS-Repo/SkyGridx/issues).

---
