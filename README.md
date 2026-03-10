# ProfitTrackerSB (Fabric Client Mod)

## Prerequisites
- Java **21** (required)
- Internet access for first Gradle dependency download
- Minecraft 1.21.10 + Fabric Loader

## Build (Windows)
1. Open terminal in project root.
2. Ensure Java 21 is active:
   ```bat
   set JAVA_HOME=C:\Program Files\Java\jdk-21
   set PATH=%JAVA_HOME%\bin;%PATH%
   java -version
   ```
3. Generate wrapper (binary wrapper jar is intentionally not tracked):
   ```bat
   gradle wrapper --no-validate-url
   ```
4. Build with wrapper:
   ```bat
   .\gradlew.bat build --refresh-dependencies
   ```

## Install
1. Copy jar from `build\\libs` to your `.minecraft\\mods` folder.
2. Install matching Fabric API for Minecraft 1.21.10.
3. Launch Minecraft with Fabric profile.

## Use
- Press **K** in-game to open Profit Tracker config.
- Config file is saved at `config/profit-tracker.json`.
- HUD is draggable and resizable with mouse on the HUD while no screen is open.

## Common issue: `25.0.1`
If Gradle fails with a message containing `25.0.1`, your terminal is using Java 25.
Switch terminal/build Java to **21** and run wrapper/build commands again.

If you get `fabric-loom ... was not found`, make sure you are running the project from this repo root and not using an old copied `settings.gradle.kts`. This repo now includes Fabric plugin resolution in `settings.gradle.kts`.


## GitHub automation
- CI build runs automatically on push/PR via `.github/workflows/ci.yml`.
- Gradle dependency update PRs are proposed weekly via `.github/dependabot.yml`.
