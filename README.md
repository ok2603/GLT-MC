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
| Chito's Journal | The diary of the trip. See below |

All items are in the **Girls' Last Tour** creative tab.

### Chito and Yuuri

Press **H** to open the character menu and choose who you play as. Your player turns into that character, and you get their perks and cons. The key can be changed in Options → Controls.

| | Chito, the thinker | Yuuri, the strong one |
| --- | --- | --- |
| Perks | Drives the Kettenkrad faster (+1 block/second). Uses 25% less fuel | Breaks blocks 30% faster. Hits 30% harder. Gets cold 25% slower |
| Cons | Breaks blocks 25% slower. Hits 30% softer. Gets cold 15% faster | Gets hungry faster. Speeds up the Kettenkrad at 40% and steers it at 60% of normal |

Standing next to each other slows the cold for both. All role numbers are at the top of `role/Roles.java`. Players who haven't picked a role play normally.

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
- Right-click it to get on. The first player drives, a second player sits in the back. Anyone can drive, but Chito is faster and Yuuri is clumsier.
- **W / S** drive forward and back, **A / D** turn. **Shift** gets off.
- Sneak + right-click shows the fuel left.
- When it's stopped, look at it to see what you can do. **Left-click** sets up camp: a campfire appears on the side of the Kettenkrad you're standing on (one camp at a time).
- **Sneak + left-click** a few times to pick it back up.

### Sitting

Press **Z** to sit down on the ground, and Z (or Shift) again to stand up. Nice by the campfire.

## The full trip (parts 1 to 8)

### Playing together
- The **host is Chito**, the friend who joins (LAN or the Essential mod) is **Yuuri**. You can still switch with H.
- Chito reads old notes and signs (Yuuri sees scribbles), repairs machines, starts elevators and the food factory, and writes in the journal. Yuuri aims the rifle well, finds extra food in crates, finds the chocolate in tanks, and can push the Kettenkrad when it's out of fuel.
- Right-click the other girl with food to **share** it: it fills her more than it would fill you.
- Stay close. Far apart for a minute (48 blocks) and you both feel **Lonely**: fires and the engine warm you only half as well.
- A hungry Yuuri sometimes sneaks a ration from the Kettenkrad.

### Scavenging and survival
- Supply caches and wrecked tanks appear in new worlds. Vanilla chests get rations and fuel too.
- Crops don't grow outdoors in the cold (configurable). The **food factory** turns crops and sugar into ration bars.
- Throw paper, books, maps or notes on a lit campfire for a burst of warmth. A **hot pipe bath** warms you fully; dip Chito's coat in it to make it extra warm for a while.
- Rain on cans, buckets and helmets plays notes. Leave food at a **statue** for luck, add a stone to a **cairn**. Street lamps switch on in the dark. The **camera** takes "photos" of places.

### The Kettenkrad is home
- Sneak + right-click: its 27-slot storage. Scrap parts repair it. It wears out slowly and breaks if it crashes. The headlight shines while the engine runs. A **bedroll** next to it is a bed.

### The journal
- Chito's journal fills itself with entries when something new happens (the first chocolate, the stars, a bath, a new floor...). Many need both girls together. Chito can write her own pages.

### Yuuri's rifle and Nuko
- The rifle is very accurate for Yuuri and wild for Chito. Cartridges are very rare. **Nuko** follows you if you feed it, eats cartridges, and plays a little tune when you hold it.

### The Last City world
- On the Create World screen choose **World Type: Last City**. You start on the bottom of a city of 8 stacked floors, each colder and emptier than the one below.
- **Elevators** lift you, your friend and the Kettenkrad to the next floor. Going up burns a Fuel Canister and needs Chito; the first elevator is next to the start, with a cache of fuel. **Ramp towers** let you climb without fuel, slowly.
- You'll meet **Kanazawa** (trade food for a map to the next elevator), **Ishii** (bring her 8 scrap parts and a fuel canister and watch her fly), and old **robots** (the one at a fish farm gives a fish a day, but don't take too many).
- At the very top there is a black stone. Stand next to it together to end the trip.

### Settings
Server settings are in `serverconfig/glt-server.toml` inside the world folder: no monsters, farming rules, auto roles, character names, journal "together" rule, loot, lonely distance, Yuuri's nibbling, elevator fuel and the city's cold.

### Editing the Kettenkrad model

The Kettenkrad's model and texture are in `blockbench/kettenkrad.bbmodel`. Open it in Blockbench, change it, then use File → Export → Java Entity (Mojmaps) and send the exported code and texture over to be put into `client/KettenkradModel.java` and `textures/entity/kettenkrad.png`.

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
  - `role/` — Chito and Yuuri's roles, perks and cons
  - `client/` — the Kettenkrad's 3D model, the thermometer, the H menu, and Chito/Yuuri's look
- `src/main/resources/assets/glt/textures/entity/player/` — Chito's and Yuuri's skins
- `src/main/resources/assets/glt/` — textures, models and names (`lang/en_us.json`)
