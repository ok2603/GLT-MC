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
| Soup | Very filling food, and warms you up |
| Fuel Canister | Fuel for the Kettenkrad (2 minutes of driving each) |
| Kettenkrad | Rideable half-track for two. See below |
| Chito's Coat | Chestplate that protects a lot from the cold (leather + wool) |
| Yuuri's Helmet | Helmet that protects a bit from the cold (iron + green dye) |
| Chito's Journal | Placeholder, will become a writable journal |

All items are in the **Girls' Last Tour** creative tab.

### The cold

There are no monsters in the Overworld. Zombies, creepers, skeletons, phantoms, patrols and raids never appear on their own (spawn eggs, commands and monster spawners still work, and the Nether and End are unchanged). The danger is the cold instead.

- The thermometer left of the hotbar shows your **body heat**. Orange is warm, blue is cold.
- You lose heat faster in snowy biomes, at night, in snow or rain, and in water.
- A roof over your head halves the heat loss. So does wearing warm clothes (Chito's Coat is the best, leather armor helps a little), and standing right next to another player.
- You warm up next to a lit campfire, fire, lava, magma or a lit furnace, next to a Kettenkrad with its engine running, and by eating Soup.
- When body heat gets low, frost covers the screen and you slow down. At zero you start freezing to death.

### Driving the Kettenkrad

- Right-click the ground with the Kettenkrad item to place it.
- Right-click it with a **Fuel Canister** to refuel. It won't move without fuel.
- Right-click it to get on. The first player drives, a second player sits in the back.
- **W / S** drive forward and back, **A / D** turn. **Shift** gets off.
- Sneak + right-click shows the fuel left.
- Hit it a few times to pick it back up.

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
  - `registry/ModEntities.java` — all entities
  - `entity/KettenkradEntity.java` — how the Kettenkrad drives, refuels and breaks
  - `cold/BodyHeat.java` — the body heat system (all the numbers are at the top)
  - `cold/NoMonsters.java` — stops monsters from spawning
  - `client/` — the Kettenkrad's 3D model and the thermometer on screen
- `src/main/resources/assets/glt/` — textures, models and names (`lang/en_us.json`)
