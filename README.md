# Girls' Last Tour — Minecraft mod

A quiet survival mod inspired by the anime *Girls' Last Tour*: scavenge rations and fuel, ride the Kettenkrad, and climb a ruined, snowy city.

- Minecraft **1.21.1**
- **NeoForge** (mod id `glt`)

## What's in it so far

| Item | What it does |
| --- | --- |
| Ration Bar | Food (6 hunger) |
| Chocolate Ration | Better food, eaten quickly |
| Canned Fish | Food (8 hunger) |
| Soup | Very filling food |
| Fuel Canister | Fuel for the Kettenkrad (coming next) |
| Chito's Journal | Placeholder, will become a writable journal |

All items are in the **Girls' Last Tour** creative tab.

## How to try it

1. Install **Java 21** (for example [Temurin 21](https://adoptium.net/)).
2. Install **IntelliJ IDEA Community Edition** and open this folder. It will set up Gradle by itself (the first time takes a few minutes).
3. Run the `runClient` Gradle task (Gradle panel → Tasks → mod development → runClient), or in a terminal:
   ```
   ./gradlew runClient      # macOS / Linux
   gradlew.bat runClient    # Windows
   ```
   Minecraft opens with the mod loaded.

To get a `.jar` you can drop into a normal NeoForge `mods` folder, run `./gradlew build`. The file appears in `build/libs/`. Every push to GitHub also builds the jar automatically (Actions tab → latest run → `glt-mod`).

## Project layout

- `src/main/java/com/ok2603/glt/` — the Java code
  - `GLTMod.java` — the main mod class
  - `registry/ModItems.java` — all items
  - `registry/ModCreativeTabs.java` — the creative tab
- `src/main/resources/assets/glt/` — textures, models and names (`lang/en_us.json`)
