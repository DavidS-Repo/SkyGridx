@echo off

:: Set variables
set plugin_url=https://raw.githubusercontent.com/DavidS-Repo/SkyGridx/main/SkyGrid.jar
set datapack_url=https://raw.githubusercontent.com/DavidS-Repo/SkyGridx/main/DataPacks/Void-Biomes-1.21.04.zip
set paper_url=https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/6/downloads/paper-1.21.4-6.jar

set plugin_folder=plugins
set world_folder=world
set datapack_folder=world\datapacks

:: Create necessary folders
echo Creating necessary server folders...
mkdir %plugin_folder%
mkdir %world_folder%
mkdir %datapack_folder%

:: Download Paper server jar
echo Downloading Paper server jar...
curl -L -o server.jar %paper_url%

:: Download plugin
echo Downloading SkyGridX plugin...
curl -L -o %plugin_folder%\SkyGrid.jar %plugin_url%

:: Download datapack
echo Downloading datapack...
curl -L -o %datapack_folder%\Void-Biomes-1.21.04.zip %datapack_url%

:: Get current date and time
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do set today=%%c-%%a-%%b
for /f "tokens=1-2 delims=:" %%a in ('time /t') do set time=%%a:%%b
set datetime=%today% %time%

:: Create and sign the EULA
echo Creating and signing EULA...
(
    echo #By changing the setting below to TRUE you are indicating your agreement to our EULA ^(https://aka.ms/MinecraftEULA^).
    echo #%datetime%
    echo eula=true
) > eula.txt

:: Create run.bat
echo Creating run.bat...
(
    echo @echo off
    echo java -Xms1G -Xmx4G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -XX:+UseNUMA -XX:ParallelGCThreads=6 -XX:ConcGCThreads=6 -XX:MaxGCPauseMillis=200 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar server.jar --nogui
    echo pause
) > run.bat

:: Confirm setup completion
echo Setup completed! Starting the server...
echo Running server...

:: Run the server
call run.bat
