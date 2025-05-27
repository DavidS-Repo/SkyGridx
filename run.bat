@echo off
for %%f in (*.jar) do set JAR=%%f
REM Launching Java with Aikar's flags
java ^
 -Xms1G ^
 -Xmx4G ^
 -XX:+UseG1GC ^
 -XX:+UnlockExperimentalVMOptions ^
 -XX:G1NewSizePercent=30 ^
 -XX:G1MaxNewSizePercent=40 ^
 -XX:G1HeapRegionSize=8M ^
 -XX:G1ReservePercent=20 ^
 -XX:G1HeapWastePercent=5 ^
 -XX:G1MixedGCCountTarget=4 ^
 -XX:InitiatingHeapOccupancyPercent=15 ^
 -XX:G1MixedGCLiveThresholdPercent=90 ^
 -XX:G1RSetUpdatingPauseTimePercent=5 ^
 -XX:SurvivorRatio=32 ^
 -XX:+PerfDisableSharedMem ^
 -XX:MaxTenuringThreshold=1 ^
 -XX:+OptimizeStringConcat ^
 -XX:+UseCompressedOops ^
 -XX:+DisableExplicitGC ^
 -XX:+AlwaysPreTouch ^
 -XX:+ParallelRefProcEnabled ^
 -XX:+UseNUMA ^
 -XX:ParallelGCThreads=16 ^
 -XX:ConcGCThreads=16 ^
 -XX:MaxGCPauseMillis=50 ^
 -Dusing.aikars.flags=https://mcflags.emc.gs ^
 -Daikars.new.flags=true ^
 -jar "%JAR%" --nogui
pause