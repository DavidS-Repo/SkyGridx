@echo off
setlocal

:: --- CONFIG -------------------------------------------
set plugin_url=https://raw.githubusercontent.com/DavidS-Repo/SkyGridx/main/SkyGrid-1.21.6.jar
set run_bat_url=https://raw.githubusercontent.com/DavidS-Repo/SkyGridx/main/run.bat
set paper_fetch_url=https://www.davids-repo.dev/fetch_latest_paper_build/

set plugin_folder=plugins
:: -------------------------------------------------------

:: Create folders (suppress errors if they already exist)
mkdir "%plugin_folder%" 2>nul

:: Fetch latest PaperMC URL...
echo Fetching latest PaperMC URL...
for /f "usebackq tokens=*" %%A in (`curl -s "%paper_fetch_url%"`) do set paper_url=%%A

if not defined paper_url (
  echo ERROR: Could not fetch PaperMC URL.
  exit /b 1
)

:: Extract filename from URL
for %%F in ("%paper_url%") do set paper_file=%%~nxF

:: Download Paper server jar under its real name
echo Downloading Paper jar as "%paper_file%"...
curl -L -o "%paper_file%" "%paper_url%"

:: Download SkyGridX plugin
echo Downloading plugin...
curl -L -o "%plugin_folder%\SkyGrid.jar" "%plugin_url%"

:: Create and sign EULA with timestamp
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do set today=%%c-%%a-%%b
for /f "tokens=1-2 delims=:"   %%a in ('time /t') do set time=%%a:%%b
set datetime=%today% %time%

(
  echo # By changing the setting below to TRUE you are indicating your agreement to our EULA ^(https://aka.ms/MinecraftEULA^).
  echo #%datetime%
  echo eula=true
) > eula.txt

:: Download your run.bat
echo Downloading run.bat...
curl -L -o run.bat "%run_bat_url%"

:: All set -- launch!
echo Setup complete. Starting server...
call run.bat

endlocal
