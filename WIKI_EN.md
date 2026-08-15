# From the Ground Up (Unofficial Port) — Wiki

> This wiki covers gameplay, the technology tree, commands, installation, and FAQ for this mod.
> Chinese and English tech names are both listed; English original names appear in parentheses where they differ.

---

## Table of Contents

1. [Gameplay & Getting Started](#gameplay--getting-started)
2. [Technology Tree Overview](#technology-tree-overview)
3. [Technology Details](#technology-details)
4. [Command Reference](#command-reference)
5. [Installation & Building](#installation--building)
6. [FAQ](#faq)
7. [License](#license)

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
| **Construction** | Stoneworking | assorted stone/sandstone blocks, snow, nether wart block, item frame |
| **Stonemasonry** | Construction | stonebrick, stone slab, stone brick stairs, cobblestone wall, concrete powder |
| **Activation** | Stonemasonry | wooden/stone button, pressure plates, lever |
| **Brickwork** | Stonemasonry | brick_block, brick stairs, flower pot, nether brick, terracotta, etc. |
| **Quartz** | Brickwork | quartz_block, quartz stairs, glowstone, magma block |
| **Purpur** | Quartz | purpur_block, purpur_pillar, purpur_stairs, end rod, shulker box |
| **Carpentry** | Construction | doors, trapdoors, fences, fence gates, chest, sign, ladder, painting, wool, bed, carpet, banner |
| **Glassworking** | Carpentry | stained_glass, glass pane, glass bottle |
| **Prismarine** | Glassworking | prismarine (three kinds), sea lantern |
| **Agriculture** | Stoneworking | hoes, wheat, hay block, melon, pumpkin, lead, grass path, leather |
| **Cooking** | Agriculture | sugar, mushroom/rabbit/beetroot stew, bread, cookie, cake, pumpkin pie, fishing rod, carrot_on_a_stick |
| **Gilded Cuisine** | Cooking | golden_apple, golden carrot, speckled melon |
| **Dyes** | Agriculture | assorted dyes (red/purple/cyan/gray/pink/yellow/blue/magenta/orange, etc.) |
| **Refinement** | Stoneworking | furnace, iron/gold ingot & block, diamond, emerald, coal block, lapis, redstone, slime, bone meal/block |
| **Smithing** | Refinement | anvil, bucket, iron bars, iron door/trapdoor, full iron & gold tool sets |
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
| **Research** | Survival | paper, **ftgumod:parchment_empty (empty parchment)**, **Idea Table**, **Research Table**, **Research Book**, **Magnifying Glass** |
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
A: This is an alpha release, ported almost directly from the 1.12.2 code. Undiscovered bugs may remain — test before committing to a long-term world.

**Q: Is it compatible with multiplayer?**
A: Yes. Research progress is saved per-player.

**Q: How do I quickly test all technologies?**
A: Use the command `/technology grant <your name> everything`.

**Q: Where is research progress stored?**
A: In player data, saved with the world / player data.

---

## License

- **This port**: CC BY-NC 4.0 (Creative Commons Attribution-NonCommercial 4.0)
- **Original mod**: *From the Ground Up* by Astavie, CC BY-NC 3.0

This work is a modified adaptation of the original. The original author does not endorse this port. Build scaffolding files are MIT-licensed by the NeoForge MDK; see [MDK-LICENSE.txt](MDK-LICENSE.txt).
