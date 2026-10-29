# Girls' Last Tour mod: implementation spec

Source of truth: the "Girls' Last Tour mod: gameplay ideas" doc, `concept.md`, and Tavares's decisions in the project threads.
Target: Minecraft 1.21.1, NeoForge 21.1.x. Co-op: the host plays Chito, the friend who joins plays Yuuri.

Legend: ✅ already merged · 🔨 planned, with the PR it lands in.

## Already built (PRs #1 to #5)

| Feature | Notes |
|---|---|
| ✅ Rations, canned fish, soup, fuel canister, Chito's journal (plain item) | No way to find them yet except creative. |
| ✅ Kettenkrad | Fuel, 2 seats, look-at prompt, left-click camp, sneak+left-click pick up. |
| ✅ Cold instead of monsters | Body heat meter, shelter, clothes, campfire/furnace/engine heat, huddling, no natural Overworld monsters. |
| ✅ Chito / Yuuri roles (H menu) | Perks, cons, skins. Yuuri can drive slowly (Tavares's choice in PR #4, this overrides "only Chito can drive"). |
| ✅ Sit with Z | |

## Every idea, and where it goes

### 1. Survive with almost nothing
| Idea | Plan | PR |
|---|---|---|
| Cold instead of monsters | Done. Extend: higher city floors are colder, warm pipe bath, burning books. | 🔨 3, 5 |
| No farming | Conflict: vanilla worlds have dirt everywhere. Compromise: a config option (default on) that stops crops growing outdoors in cold biomes and slows them elsewhere. In the city world there is no soil at all. | 🔨 1 |
| Fuel is life | Fuel canisters only from loot (rare). Headlight only with fuel. | 🔨 1, 2 |
| Kettenkrad is home | Storage (27 slots), a bedroll you can sleep on next to it, damage and repair with scrap parts, Yuuri can push it with no fuel. | 🔨 2 |

### 2. The journal
| Idea | Plan | PR |
|---|---|---|
| Discovery diary | Journal opens a screen of entries in Chito's voice, unlocked by moments (first chocolate, stars, bath, new floor, beer, etc.). Saved per world so both girls share one diary. | 🔨 3 |
| Only Chito writes | Chito can add her own page (a short text line). Yuuri can read the auto entries, not write. | 🔨 3 |
| Burning books | Throw a book, old note, map or journal page onto a lit campfire: big burst of warmth. Burning the journal itself asks twice. | 🔨 3 |

### 3. Small moments
| Idea | Plan | PR |
|---|---|---|
| Chocolate in the tank | Wrecked tank structure with a hidden compartment; only Yuuri can find the chocolate in it. | 🔨 1 (block + loot), 6 (in the city) |
| Hot pipe bath | Leaking Hot Pipe block warms nearby water: bathing fully warms you and gives Regeneration. Drop your coat in for "dry and warm" (extra cold protection for a while). | 🔨 5 |
| Rain music | Empty cans, helmets and buckets placed on the ground play notes when rain hits them. | 🔨 5 |
| Food factory | A machine block: potatoes + sugar + fuel → ration bars. Only Chito can start it. | 🔨 5 |
| Fish farm and robot | Rare building with fish in a pool and a maintenance robot. Take more than a few fish and it gets upset (beeps, pushes you away, stops trading). | 🔨 7 |
| Beer | Rare bottle: Nausea, a little warmth, a journal entry. | 🔨 1 |
| Stone statues | Statue block: offer food for a small loot-luck bonus. Cairn block: stack a stone on the pile. | 🔨 5 |
| Starry sky | City world: a dense custom star field on clear nights. Sitting or lying under it high up (both girls present) writes a journal entry. | 🔨 3 (entry), 6 (sky) |
| Camera | Simple version: a camera that makes a "Photo of [place] on day [N]" item. Real screenshots are out of scope (very hard and heavy in multiplayer). | 🔨 5 |

### 4. The city and the climb
| Idea | Plan | PR |
|---|---|---|
| Layered city | A new world type "Last City": stacked concrete floors joined by giant pipes, dark tunnels and stairs, each floor colder and emptier. Vanilla worlds keep working. | 🔨 6 |
| Giant elevators | Elevator platform that lifts players and the Kettenkrad one floor, powered by a fuel canister; Chito starts it. | 🔨 6 |
| Maps that matter | Vanilla maps work. Kanazawa trades a map marking the next elevator for food. Maps can be burned. | 🔨 3 (burning), 7 (Kanazawa) |
| Street lamps | Lamp block that lights up at night; whole districts of them in the city. | 🔨 5 (block), 6 (districts) |
| The ending | Top floor: a final quiet scene, the last journal entry, and the credits. | 🔨 8 |

### 5. People and creatures
| Idea | Plan | PR |
|---|---|---|
| Nuko | Small white creature that follows you and eats ammo and explosives. Hold it to hear a short original radio tune (the anime song can't be included). | 🔨 4 |
| Ishii and her plane | NPC who wants plane parts. Bring them all, watch her take off and crash, get food or fuel. | 🔨 7 |
| Yuuri's rifle | Almost no ammo. Yuuri aims well (also from the back seat); Chito has a huge spread. | 🔨 4 |
| Old robots | Maintenance robots wander, tending the fish farm and plant areas. | 🔨 7 |

### 6. Co-op
| Idea | Plan | PR |
|---|---|---|
| Host is Chito, friend is Yuuri | Auto-assign on join (host → Chito, next player → Yuuri), H menu still lets you switch. | 🔨 1 |
| Chito reads the old world | Old notes and signs show text to Chito, scribbles to Yuuri. | 🔨 1 (notes), 6 (signs) |
| Chito finds maps/books/parts; Yuuri finds food | Loot bonus by role when opening containers. | 🔨 1 |
| Yuuri nibbles a ration | Config option (on by default): now and then a ration disappears from the Kettenkrad storage while Yuuri is near, with a message. | 🔨 8 |
| Shared warmth | Done (huddling). | ✅ |
| Sharing food | Feed the other player (right-click them with food): they get more food than you would. | 🔨 8 |
| Moments need both girls | Many journal moments need both players near each other. Single player still works (it only needs everyone online to be close). | 🔨 3 |
| Don't wander off | Too far apart for too long: both get "Lonely" (slower warmth recovery). | 🔨 8 |

## How the Last City is built (PR 6)

- **World type.** On the Create World screen, *World Type: Last City*. It swaps the Overworld for an empty
  flat world 512 blocks tall (dimension type `glt:last_city`) whose only biome, `glt:dead_city`, has one
  feature: `glt:city`. Vanilla world types are untouched. Nether and End stay vanilla.
- **Why a feature and not a custom chunk generator.** The feature API is the same one the ruins already
  use and is stable, so there is far less that can break between NeoForge versions. Each chunk builds only
  its own blocks from a seeded plan (`city/CityLayout.java`), so chunks fit together in any order.
- **Plan.** 8 floors, 56 blocks apart, each a 4-block concrete slab. 32x32 cells: roads on the edges,
  one lot inside (building, ruin, plaza, giant pipes, light-well hole, tank battlefield, temple, factory,
  fish farm, elevator, stair tower). Higher floors are emptier and colder. Pillars hold each floor up where
  roads cross, pipes hang under every ceiling. The top floor is open to the sky, with the summit stone at (18, 18).
- **Getting up.** One elevator per 4x4 cells on each floor (the first is always next to the start, with a
  fuel cache). Going up burns a Fuel Canister and needs Chito; going down is free. Spiral ramp towers
  (rarer) climb a quarter block per step, so the Kettenkrad can drive up without fuel.
- **Spawn.** Players start on the bottom floor at (3, 8, 20). Without a bed or bedroll, you respawn there.
- **Lamps.** Lit districts of street lamps; lamps now switch on wherever the sun doesn't reach.
- **Sky.** 4000 extra stars in the city, brighter the higher you are.
- Config: `lastCity.elevatorNeedsFuel`, `lastCity.cityHeightCold`.

## PR order

1. **Scavenging**: config file, loot in vanilla and new crates, supply crate + wrecked tank blocks, scrap parts, old notes, beer, no-farming, auto roles, role loot bonuses.
2. **Kettenkrad as home**: storage, headlight, bedroll, damage + repair, Yuuri push.
3. **Journal**: discovery diary screen, moments, Chito's own pages, burning books/maps for heat.
4. **Rifle and Nuko**.
5. **Small moments**: hot pipe bath + laundry, rain music, food factory, statues + cairn, street lamps, camera.
6. **The Last City world**: floors, pipes, tunnels, elevators, ruins, colder floors, star sky.
7. **People**: Kanazawa, Ishii, robots and the fish farm.
8. **Co-op polish and the ending**: lonely, food sharing, nibbling, the final scene.

## Risks
- The sandbox can't download NeoForge, so every build is checked by GitHub Actions.
- Textures and sounds are made by script (simple pixel art, synthesized tunes). Tavares can replace any PNG/OGG later without code changes.
- The city world generator (PR 6) is the biggest and riskiest piece; it's built last among world features so the survival loop works first.
