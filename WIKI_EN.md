# From the Ground Up (Unofficial Port) — Wiki

> This wiki covers gameplay, the technology tree, commands, installation, and FAQ for this mod.
> Chinese and English tech names are both listed; English original names appear in parentheses where they differ.
>
> 🌐 [中文 Wiki](WIKI_CN.md)

---

## Table of Contents

1. [Gameplay & Getting Started](#gameplay--getting-started)
2. [Technology Tree Overview](#technology-tree-overview)
3. [Technology Details](#technology-details)
4. [Command Reference](#command-reference)
5. [Custom Technologies (Data Pack)](#custom-technologies-data-pack)
6. [Installation & Building](#installation--building)
7. [FAQ](#faq)
8. [License](#license)

---

## Gameplay & Getting Started

This mod adds a **research system** to Minecraft: instead of knowing every recipe from the start, you must study and unlock technologies through research before you can use the corresponding items and recipes.

### Core Items

| Item | Purpose |
|---|---|
| **Research Book** | Opens the technology tree interface to view researched / unlocked / available technologies. Default key: `R`. |
| **Magnifying Glass** | Right-click blocks in the world to "observe" them and gain knowledge. Some secrets must be deciphered from a hint. |
| **Idea Table** | Combine items in it to spark "ideas", unlocking new research branches. |
| **Research Table** | Insert research parchment and solve a **puzzle** (match or connect types) to complete the research. |
| **Research Parchment** | The vessel of research — written with an idea at the Idea Table, then solved at the Research Table. |
| **Idea Parchment** | The vessel of ideas from the Idea Table. Can be crafted together with Research Parchment into Empty Parchment to recover mistaken parchments. |

### Getting Started

1. **Craft a Research Book**, press `R` to open the technology tree, and first research **Survival** — the root technology.
2. Research **Research** to unlock the Idea Table, Research Table, Research Book, and Magnifying Glass — the real research gameplay begins.
3. Observe blocks with the **Magnifying Glass**, or fulfill **criteria** (e.g. having an effect, killing a specific mob) to satisfy a technology's research prerequisites.
4. Place recipe items in the **Idea Table** to spark an idea, then write it onto a **Research Parchment**.
5. Put the parchment in the **Research Table**, solve the puzzle (place the right items / connect the right lines), and the technology is unlocked.

### Research Criteria

Each technology's research requires meeting certain conditions. Common types:

- **effects_changed** — obtain a potion effect
- **player_killed_entity** — kill a specific mob (e.g. a witch)
- **item_inventory** — have an item in your inventory
- **player_trigger / location** — trigger at a specific location

### Decipher

When the Magnifying Glass observes certain blocks (e.g. beds, cauldrons, nether stars), the result is not shown directly and must be **deciphered**. After observing, press the decipher key and follow the hint (which points to a specific block / location) to complete the decipher and gain the corresponding knowledge.

---

## Technology Tree Overview

- `survival` (Survival) is the only root technology — it has no prerequisites.
- Arrows mean **prerequisite → successor**; you must research a prerequisite before its successor.

```
survival Survival
├─ stoneworking Stoneworking
│   ├─ construction Construction
│   │   ├─ stonemasonry Stonemasonry
│   │   │   ├─ activation Activation ──→ (power Power)
│   │   │   └─ brickwork Brickwork ──→ quartz Quartz ──→ purpur Purpur
│   │   └─ carpentry Carpentry ──→ glassworking Glassworking ──→ prismarine Prismarine
│   ├─ agriculture Agriculture
│   │   ├─ cooking Cooking ──→ gilded_cuisine Gilded Cuisine
│   │   └─ dyes Dyes
│   └─ refinement Refinement
│       ├─ smithing Smithing ──→ lapidary Lapidary
│       └─ explosives Explosives
├─ boats Boats
│   └─ carts Carts ──→ transportation Transportation
├─ defense Defense
│   └─ metal_armor Metal Armor ──→ gem_armor Crystalline Armor
└─ research Research  (research root)
    ├─ bibliography Bibliography ──→ enchanting Enchanting ──→ glowing_eyes Glowing Eyes ──→ ender_knowledge Ender Knowledge
    └─ brewing Brewing

power Power  (prerequisite = construction/activation)
├─ cartography Cartography
├─ circuitry Circuitry ──→ redstone_machinery Redstone Machinery
└─ music Music
```

> Note: although `power` is a Power-category tech, its prerequisite is the construction-category `activation`; the `research` category's prerequisite is `survival`. Cross-category references are normal.

---

## Technology Details

> Unlock entries are listed by Minecraft item registry name (English ID); the corresponding in-game Chinese names follow the game language.

### Survival

| Technology | Prerequisite | Unlocks |
|---|---|---|
| **Survival** | (root) | wooden_sword, wooden_shovel, wooden_pickaxe, wooden_axe, crafting_table, torch, bowl, wooden tools & planks |
| **Stoneworking** | Survival | stone_sword, stone_shovel, stone_pickaxe, stone_axe (stone tools) |
| **Construction** | Stoneworking | granite/diorite/andesite/tuff/deepslate/basalt & polished variants, mossy cobblestone, cobblestone slab/stairs, sandstone/red sandstone & slab/stairs, snow/snow layer, nether wart block, item frame |
| **Stonemasonry** | Construction | stone slab/stairs, stone bricks/chiseled/mossy, stone brick slab/stairs, cobblestone wall/mossy, concrete powder (any color) |
| **Activation** | Stonemasonry | wooden button (any wood), stone/polished blackstone button, wooden pressure plate (any wood), stone/polished blackstone/light/heavy pressure plate, lever |
| **Brickwork** | Stonemasonry | bricks, brick slab/stairs, flower pot, nether brick/slab/stairs/fence, red nether bricks, end stone bricks, clay, terracotta (any color) |
| **Quartz** | Brickwork | quartz block/chiseled/pillar/slab/stairs, glowstone, magma block |
| **Purpur** | Quartz | purpur block/pillar/slab/stairs, end rod, shulker box, shulker box (any color) |
| **Carpentry** | Construction | wooden door (any wood), wooden trapdoor (any wood), wooden fence (any wood), fence gate (any wood), sign (any wood), chest, ladder, painting, wool (any color), bed (any color), carpet (any color), banner (any color) |
| **Glassworking** | Carpentry | stained glass, stained glass pane, glass pane, glass bottle |
| **Prismarine** | Glassworking | prismarine, prismarine bricks, dark prismarine, sea lantern |
| **Agriculture** | Stoneworking | wooden/stone hoe, wheat, hay block, melon/melon seeds, pumpkin seeds, lead, coarse dirt, jack o'lantern, leather |
| **Cooking** | Agriculture | sugar, mushroom/rabbit/beetroot stew, bread, cookie, cake, pumpkin pie, fishing rod, carrot on a stick |
| **Gilded Cuisine** | Cooking | golden apple, golden carrot, glistering melon slice |
| **Dyes** | Agriculture | light gray/gray/cyan/light blue/purple/magenta/pink/orange/lime dye |
| **Refinement** | Stoneworking | furnace, iron ingot/nugget/block, flint & steel, gold ingot/nugget/block, diamond/block, emerald/block, coal/block, lapis/block, redstone/block, slime ball/block, bone meal/block |
| **Smithing** | Refinement | anvil, bucket, iron bars, iron door/trapdoor, full iron & gold tool sets, shears |
| **Lapidary** | Smithing | full diamond tool set (sword/shovel/pickaxe/axe/hoe) |
| **Explosives** | Refinement | tnt, tnt_minecart, firework_charge, fireworks, fire charge |
| **Boats** | Survival | boats |
| **Carts** | Boats | minecart, furnace_minecart, rail |
| **Transportation** | Carts | hopper, golden/detector/activator rail, chest minecart, hopper minecart |
| **Defense** | Survival | full leather armor, armor stand, bow, arrow, shield |
| **Metal Armor** | Defense | full iron & gold armor |
| **Crystalline Armor** | Metal Armor | full diamond armor |

### Research

| Technology | Prerequisite | Unlocks |
|---|---|---|
| **Research** | Survival | paper, empty parchment, Idea Table, Research Table, Research Book, Magnifying Glass |
| **Bibliography** | Research | book, writable_book, bookshelf |
| **Enchanting** | Bibliography | enchanting_table, spectral_arrow |
| **Glowing Eyes** | Enchanting | ender_eye |
| **Ender Knowledge** | Glowing Eyes | end_crystal, ender_chest, beacon |
| **Brewing** | Research | brewing_stand, cauldron, blaze_powder, magma_cream, fermented_spider_eye, tipped_arrow |

### Power

| Technology | Prerequisite | Unlocks |
|---|---|---|
| **Power** | Construction · Activation | redstone_torch, tripwire_hook, trapped_chest, redstone_lamp |
| **Cartography** | Power | compass, clock, map |
| **Circuitry** | Power | repeater, comparator, piston, sticky_piston |
| **Redstone Machinery** | Circuitry | dispenser, dropper, observer, daylight_detector |
| **Music** | Power | noteblock, jukebox |

---

## Research Recipes (Idea Table & Research Table)

Besides meeting the **research criteria**, most technologies require two "recipe" steps before they fully unlock:

1. **Idea Table**: place the specified items into the Idea Table (order doesn't matter) to spark an **idea**, then write it onto a parchment.
2. **Research Table**: put the parchment into the Research Table and solve the puzzle — either a **match** (place the right items in a 3×3 grid) or a **connect** (place 3 items forming a production chain).

> Notes: `{"tag": "xxx"}` is a tag reference (any item under that tag); `[a, b, c]` is an item list (pick any one); `.` means an empty cell. Items are listed by registry name; in-game they display in your language.

### Construction

- **Stoneworking**
  - Idea Table: stick / any wooden tool + cobblestone
  - Puzzle (match):
    ```
    .   [string/reeds]  cobblestone
    .   stick         [string/reeds]
    stick  .            .
    ```
- **Construction**
  - Idea Table: stone / sandstone
- **Stonemasonry**
  - Idea Table: stone / polished_granite / polished_diorite / polished_andesite / polished_deepslate / polished_tuff / smooth_basalt / polished_blackstone
  - Puzzle (match):
    ```
    [stone slabs]  [stone slabs]  [stone slabs]
    [stone types]     .          [stone types]
    [stone types]  [stone stairs]  [stone types]
    ```
    > `[stone slabs]` = all slab variants; `[stone types]` = stone / polished_granite etc.; `[stone stairs]` = all stair variants.
- **Activation**
  - Idea Table: stick + redstone dust + [doors/fence gates/trapdoors]
  - Puzzle (connect): iron door → lever
- **Brickwork**
  - Idea Table: clay / clay ball + [heat items] (furnace / smoker / blast furnace / magma block / campfire / coal etc.)
  - Puzzle (connect): terracotta → brick block
- **Quartz**
  - Idea Table: quartz + glowstone dust / magma block / magma cream
- **Purpur**
  - Idea Table: popped chorus fruit + purpur block/pillar/stairs/slab/end rod
  - Puzzle (match):
    ```
    purpur slab   purpur slab   purpur slab
    purpur pillar  end rod     purpur pillar
    [purpur block/end bricks] purpur stairs [purpur block/end bricks]
    ```
- **Carpentry**
  - Idea Table: wool (any color) + planks (any wood) + wooden slab (any wood) + wooden stairs (any wood)
  - Puzzle (connect): planks → wool
- **Glassworking**
  - Idea Table: sand + glass block
- **Prismarine**
  - Idea Table: prismarine / sea lantern + prismarine shard + prismarine crystals

### Power

- **Power**
  - Idea Table: redstone dust + any switch (button / pressure plate / lever / stick)
  - Puzzle (match):
    ```
    redstone dust  .   .
    stick      redstone dust  redstone dust
    [circuit board] [circuit board] [circuit board]
    ```
    > `[circuit board]` = stone / stone slab / wool / terracotta / concrete, any one.
- **Cartography**
  - Idea Table: paper + redstone dust / feather / ink sac
- **Circuitry**
  - Idea Table: redstone dust / redstone torch + quartz / stone / stone slab
  - Puzzle (match):
    ```
    .  .  .
    redstone dust  stone  redstone torch
    [circuit board] [circuit board] [circuit board]
    ```
- **Redstone Machinery**
  - Idea Table: bow + redstone dust
  - Puzzle (match):
    ```
    .  .  .
    redstone dust  stone  repeater
    [circuit board] sticky piston [circuit board]
    ```
- **Music**
  - Idea Table: redstone dust + diamond + planks
  - Puzzle (match):
    ```
    .  [iron nugget/gold nugget/diamond]  [stick/iron ingot]
    .  music disc                    .
    [planks/wooden slab] [planks/wooden slab] [planks/wooden slab]
    ```

### Research

- **Bibliography**
  - Idea Table: paper + leather
- **Enchanting**
  - Idea Table: enchanted item (any) + book + lapis lazuli
  - Puzzle (connect): book → iron sword
- **Glowing Eyes**
  - Idea Table: ender pearl + blaze powder
- **Ender Knowledge**
  - Idea Table: [dragon egg / dragon's breath / dragon head] + nether star
  - Puzzle (match):
    ```
    .  wither skeleton skull  .
    .  [crafting table/bed]  .
    .  [dragon egg/breath/head]  .
    ```
- **Brewing**
  - Idea Table: [water bottle / water bucket] + [nether wart / sugar]
  - Puzzle (match):
    ```
    .  sugar     .
    .  nether wart  .
    .  [water bottle/water bucket] .
    ```

### Survival

- **Agriculture**
  - Idea Table: crops / seeds / berries / fruits / mushrooms (any) + dirt
  - Puzzle (match):
    ```
    .  .  .
    stick  [crop type]  stick
    dirt  dirt  dirt
    ```
    > `[crop type]` = any item under crops / seeds / berry / fruit / mushrooms tags.
- **Cooking**
  - Idea Table: heat item (magma cream / blaze powder / campfire / soul campfire etc.) + [meat/vegetables/grains]
  - Puzzle (match):
    ```
    .  .  .
    [vegetables]  raw meat (any)  [fruits]
    .  bowl  .
    ```
    > `[vegetables]` = carrot / potato / beetroot / wheat / pumpkin / mushroom etc.; `[fruits]` = apple / melon / chorus_fruit / sweet_berries etc.
- **Gilded Cuisine**
  - Idea Table: [gold block/ingot/nugget] + [apple/carrot/melon slice]
  - Puzzle (match):
    ```
    gold nugget  gold nugget  gold nugget
    gold nugget  [apple/carrot/melon slice]  gold nugget
    gold nugget  gold nugget  gold nugget
    ```
- **Dyes**
  - Idea Table: flower + dye + cactus
  - Puzzle (connect): flint → ink sac
- **Refinement**
  - Idea Table: ore (any raw ore / ore block) + heat item + pickaxe (any material)
  - Puzzle (match):
    ```
    stone materials (stone/andesite/diorite/granite)  same  same
    same  [ore type]  same
    same  heat item  same
    ```
    > `[ore type]` = any raw ore or ore block.
- **Smithing**
  - Idea Table: metal ingot (any) / metal nugget (any) / [iron/gold/copper/netherite block]
  - Puzzle (connect): planks → anvil
- **Lapidary**
  - Idea Table: diamond + emerald
  - Puzzle (connect): iron ingot → diamond
- **Explosives**
  - Idea Table: gunpowder + sand + flint and steel + dye
- **Carts**
  - Idea Table: iron ingot + [stick/boat]
  - Puzzle (match):
    ```
    [iron ingot/planks]  .  [iron ingot/planks]
    [iron ingot/planks]  [iron ingot/planks]  [iron ingot/planks]
    iron ingot  [stick/wooden slab]  iron ingot
    ```
- **Transportation**
  - Idea Table: redstone dust + [minecart/boat/rail] + chest
  - Puzzle (connect): rail → redstone dust
- **Defense**
  - Idea Table: sword (any material) + armor (any) / leather + iron ingot + planks (any wood)
- **Metal Armor**
  - Idea Table: armor + metal ingot (any) / metal nugget (any) etc.
  - Puzzle (connect): armor stand → iron ingot
- **Crystalline Armor**
  - Idea Table: armor + gems
  - Puzzle (connect): leather → diamond
- **Boats**
  - Idea Table: bowl

> **Survival** and **Research** are root technologies — they have no Idea Table / Research Table steps and unlock directly once their research criteria are met.

---

## Command Reference

All commands start with `/technology` (registered by this mod).

```
/technology grant <player> everything                       grant all technologies
/technology grant <player> only <tech>                      grant only the specified technology
/technology grant <player> through <tech>                   grant the specified technology and all its prerequisites
/technology grant <player> from <tech>                      grant the specified technology and all its successors
/technology grant <player> until <tech>                     grant the specified technology and its intermediate related techs

/technology revoke <player> everything                      revoke all technologies
/technology revoke <player> only <tech>                     revoke only the specified technology
/technology revoke <player> through <tech>                  revoke the specified technology and its prerequisites
/technology revoke <player> from <tech>                     revoke the specified technology and its successors
/technology revoke <player> until <tech>                    revoke the specified technology and its intermediate related techs

/technology test <player> <tech> [criterion]                check whether a technology / criterion is satisfied
/technology reload                                           reload technology data
```

- `<tech>` format looks like `ftgumod:survival/stoneworking`, `ftgumod:research/brewing`.
- Use **Tab auto-completion** to browse available options.
- The difference between the `only` / `through` / `from` / `until` modes:
  - `only` affects only that technology itself
  - `through` goes **up** the prerequisite chain (including all parent nodes)
  - `from` goes **down** the successor chain (including all child nodes)
  - `until` takes everything from the root to that technology (excluding the root)

---

## Custom Technologies (Data Pack)

Technology definition files are located in `data/ftgumod/technologies/`. Players can override or add technologies via data packs.

### Directory Structure

```
data/
└── ftgumod/
    └── technologies/
        ├── survival/
        │   ├── survival.json
        │   ├── stoneworking.json
        │   └── ...
        ├── construction/
        ├── research/
        └── power/
```

### Override Priority

```
world/technologies/  >  config/ftgumod/technologies/  >  Built-in (mod JAR)
```

| Directory | Purpose | Per-save? |
|-----------|---------|-----------|
| Built-in (JAR) | Mod's default technologies | — |
| `config/ftgumod/technologies/` | Global overrides / additions | No |
| `world/technologies/` | Per-save overrides / additions | Yes |

### unlock recipe_types Filtering

By default, unlock searches all recipe types. Use the `recipe_types` field to restrict the search scope:

```json
"unlock": [
  "minecraft:iron_ingot",
  {"item": "minecraft:glass", "recipe_types": ["minecraft:smelting"]}
]
```

When `recipe_types` is omitted, all types (crafting, smelting, blasting, etc.) are searched.

---

## Installation & Building

### Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.230** or later

### Installation

1. Install [NeoForge](https://neoforged.net/) for 1.21.1.
2. Download this mod's release jar.
3. Place the `.jar` file in your `mods/` folder.
4. Launch the game.

### Building from source

```bash
./gradlew build         # compile and package
./gradlew runClient     # launch a development client
./gradlew runServer     # launch a development server
```

The artifact is output to `build/libs/`.

---

## FAQ

**Q: Why can't I craft many things at the start?**
A: That's normal. You need to research first to unlock them. Open the Research Book (`R`) to see currently available research.

**Q: How do I open the Research Book?**
A: Craft a Research Book and press `R`, or rebind the key in settings.

**Q: How do I solve the puzzles?**
A: For Match puzzles, place the correct items as the hint describes; for Connect puzzles, connect the related items into a line. Hints can be obtained by deciphering with the Magnifying Glass.

**Q: What happens to old saves?**
A: This is an alpha release; undiscovered bugs may remain — test before committing to a long-term world. Since v1.1, loot tables use data pack overrides; already-generated chests are not affected.

**Q: Is it compatible with multiplayer?**
A: Yes. Research progress is saved per-player.

**Q: How do I quickly test all technologies?**
A: Use the command `/technology grant <your name> everything`.

**Q: Where is research progress stored?**
A: In player data, saved with the world / player data.

**Q: Can I customize the technology tree?**
A: Yes. Technology definitions are in `data/ftgumod/technologies/` and can be overridden or added via data packs. See the "Custom Technologies (Data Pack)" section.

---

## License

- **This port**: CC BY-NC 4.0 (Creative Commons Attribution-NonCommercial 4.0)
- **Original mod**: *From the Ground Up* by Astavie, CC BY-NC 3.0

This work is a modified adaptation of the original. The original author does not endorse this port. Build scaffolding files are MIT-licensed by the NeoForge MDK; see [MDK-LICENSE.txt](MDK-LICENSE.txt).
