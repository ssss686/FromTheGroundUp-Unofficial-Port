# From the Ground Up (Unofficial Port) — Wiki

> This wiki covers gameplay, the technology tree, commands, installation and common questions.
> Chinese / English technology names are listed together.
>
> 🌐 [中文 Wiki](WIKI_CN.md)

---

## Table of Contents

1. [Gameplay & Getting Started](#gameplay--getting-started)
2. [Technology Tree Overview](#technology-tree-overview)
3. [Technology Details](#technology-details)
4. [Research Recipes (Idea Table & Research Table)](#research-recipes-idea-table--research-table)
5. [Research Criteria & Triggers](#research-criteria--triggers)
6. [Command Reference](#command-reference)
7. [Custom Technologies (Data Pack)](#custom-technologies-data-pack)
8. [Installation & Building](#installation--building)
9. [FAQ](#faq)
10. [License](#license)

---

## Gameplay & Getting Started

This mod adds a **research system** to Minecraft: you no longer start with every recipe. You have to research your way through a technology tree before you can use the matching items and recipes.

### Core Items

| Item | Purpose |
|---|---|
| **Research Book** | Opens the technology tree screen. Default key: `R`. |
| **Magnifying Glass** | Right-click blocks in the world to "inspect" them and gain knowledge. Some secrets must be deciphered first. |
| **Idea Table** | Combine items inside it to trigger an "idea" and open up a new research branch. |
| **Research Table** | Put a research parchment in and solve the **puzzle** (match / connect) to finish the research. |
| **Research Parchment** | The carrier of a research. Write to it after getting an idea, then solve it on the research table. |
| **Idea Parchment** | Holds the idea produced by the idea table. Combine it with a research parchment into an empty parchment to recycle a mistake. |

### Getting Started

1. **Craft a research book**, press `R` and research **Survival** first — it is the root technology.
2. Researching **Research** unlocks the idea table, research table, research book and magnifying glass, which is what actually starts the research gameplay.
3. Satisfy a technology's **criteria** — inspect blocks with the magnifying glass, or meet a condition (having an effect, killing a certain mob, ...).
4. Put the right items into the **idea table** to get an idea, and write it onto a **research parchment**.
5. Put the parchment into the **research table** and solve the puzzle (place the right items / connect the two items). The technology is unlocked once you finish.

### Research Book Screen

- **Scroll** to zoom, **hold left click and drag** to pan; the page recenters itself and stops at the edges.
- Technology positions are **laid out automatically** (the same algorithm as the vanilla advancement screen) — data packs never need to specify coordinates. Technologies on a page are ordered by name.
- Every technology has a **frame**: task, goal, or **challenge**. Finishing a challenge research plays a sound.
- **Hidden technologies**: a technology whose JSON sets `display.hidden` is not drawn on the book until its criteria are met, then it shows up (the ocean monument, end city, ender dragon, shipwreck, witch, riding a pig and crossbow pillager ones work this way).

### Research Criteria

A technology's criteria are written exactly like vanilla advancement criteria. The common ones are:

- **effects_changed** — gain a status effect
- **player_killed_entity** — kill a specific mob (a witch, a crossbow-wielding pillager, ...)
- **started_riding** — start riding a specific vehicle (a pig, a strider)
- **location** — visit a structure or biome (an ocean monument, an end city, a soul sand valley)
- **item_used_on_block** — use a specific item on a specific block (a glass bottle or shears on a beehive)
- **inventory_changed** — your inventory changes (carrying ancient debris back to the Overworld)
- **ftgumod:item_inventory** — hold a specific item

See [Research Criteria & Triggers](#research-criteria--triggers) for the full list and its limits.

### Decipher

Inspecting some blocks (beds, cauldrons, nether stars, ancient debris) with the magnifying glass does not reveal them directly — they must be **deciphered**. Inspect, then press the decipher key and follow the hint (it points at a specific block or place) to gain the knowledge.

### JEI Research Guide

With JEI installed, pressing **R** on any item shows not only its recipes but also its **unlock requirements, prerequisite technology chain, and how to research it** (what to put on the idea table, how to solve the research puzzle, which extra criteria are still missing).

How much is shown is controlled by `researchGuideMode` in `config/ftgumod-common.toml`:

| `researchGuideMode` | Effect |
|---|---|
| `FULL` (default) | Prerequisite chain + full research method |
| `CHAIN_ONLY` | Prerequisite technology chain only |
| `DISABLED` | No research guide in JEI at all |

---

## Technology Tree Overview

- `survival` (Survival) and `research` (Research) are the two root technologies — they have no prerequisites, everything else hangs below them.
- Arrows mean **prerequisite → dependent**: you must research the former before the latter.
- `[challenge]` marks a challenge-framed technology and `[goal]` a goal-framed one; `[hidden]` marks one that does not appear on the research book until its criteria are met.

```
survival (root)
├─ stoneworking Stoneworking
│   ├─ construction Construction
│   │   ├─ stonemasonry Stonemasonry
│   │   │   ├─ brickwork Brickwork
│   │   │   │   ├─ emberwisp_ignition Emberwisp Ignition
│   │   │   │   ├─ quartz Quartz
│   │   │   │   │   └─ purpur Purpur  [challenge, hidden]
│   │   │   │   └─ glazed_tiles Glazed Tiles
│   │   │   └─ ice_harvesting Ice Harvesting
│   │   └─ carpentry Carpentry
│   │       ├─ barker Barker
│   │       ├─ emblems Emblems
│   │       └─ glassworking Glassworking
│   │           └─ prismarine Prismarine  [challenge, hidden]
│   ├─ agriculture Agriculture
│   │   ├─ apiculture Apiculture
│   │   ├─ cooking Cooking
│   │   │   ├─ flower_language Flower Language
│   │   │   └─ gilded_cuisine Gilded Cuisine
│   │   └─ dyes Dyes
│   └─ refinement Refinement
│       ├─ smithing Smithing
│       │   ├─ conduit Conduit Power  [challenge, hidden]
│       │   └─ lapidary Lapidary
│       │       └─ netherite Gold from the Embers
│       │           ├─ alloy_tools Alloy Tools  [goal]
│       │           └─ netherite_passport Netherite Passport
│       └─ power Power
│           ├─ activation Activation
│           │   └─ explosives Explosives
│           ├─ cartography Cartography
│           ├─ carts Carts
│           │   └─ transportation Transportation
│           ├─ circuitry Circuitry
│           │   └─ redstone_machinery Redstone Machinery
│           └─ music Music
├─ boats Boats
├─ defense Defense
│   └─ metal_armor Metal Armor
│       ├─ gem_armor Crystalline Armor
│       │   └─ emberforged Emberforged  [goal]
│       └─ turtles Turtle Hermit
├─ fishing Fishing Ballad
│   ├─ carrot_protocol The Carrot Protocol
│   │   └─ warped_protocol Warped Protocol  [goal]
│   └─ hunting Hunting
│       └─ clockwork_malice Clockwork Malice  [goal]
└─ research Research (root)
    ├─ bibliography Bibliography
    │   └─ enchanting Enchanting  [challenge, hidden]
    │       └─ glowing_eyes Glowing Eyes  [challenge, hidden]
    │           └─ ender_knowledge Ender Knowledge  [challenge, hidden]
    └─ brewing Brewing  [challenge, hidden]
```

> Note: the **Power** group hangs off **Refinement**, while `activation`, `carts`, `explosives` and `transportation` live on the power page; **Research** is an independent root, not connected to Survival. Cross-group references are normal.

---

## Technology Details

> Unlocks are listed by their in-game English names; `any ××` means any item of that tag counts. Technology IDs look like `ftgumod:construction/stonemasonry`.

### Survival

| Technology | Requires | Unlocks |
|---|---|---|
| **Survival** | (root) | any planks, any wooden slab, any wooden stairs, Stick, Wooden Sword, Wooden Shovel, Wooden Pickaxe, Wooden Axe, Crafting Table, Torch, Bowl, Campfire |
| **Stoneworking** | Survival | Stone Sword, Stone Shovel, Stone Pickaxe, Stone Axe |
| **Agriculture** | Stoneworking | Wooden Hoe, Stone Hoe, Wheat Crops, Hay Bale, Melon, Melon Seeds, Pumpkin Seeds, Composter, Lead, Smoker, Coarse Dirt, Jack o'Lantern, Leather, Nether Wart Block |
| **Apiculture** | Agriculture | Beehive, Honey Bottle, Honey Block, Honeycomb Block |
| **Cooking** | Agriculture | Sugar, Mushroom Stew, Rabbit Stew, Beetroot Soup, Bread, Cookie, Cake, Pumpkin Pie, Dried Kelp |
| **Flower Language** | Cooking | Suspicious Stew |
| **Gilded Cuisine** | Cooking | Golden Apple, Golden Carrot, Glistering Melon Slice |
| **Dyes** | Agriculture | Light Gray Dye, Gray Dye, Cyan Dye, Light Blue Dye, Purple Dye, Magenta Dye, Pink Dye, Orange Dye, Lime Dye |
| **Refinement** | Stoneworking | Furnace, Iron Ingot, Iron Nugget, Block of Iron, Flint and Steel, Gold Ingot, Gold Nugget, Block of Gold, Diamond, Block of Diamond, Emerald, Block of Emerald, Coal, Block of Coal, Lapis Lazuli, Block of Lapis Lazuli, Redstone Dust, Block of Redstone, Slimeball, Slime Block, Nether Quartz, Bone Meal, Bone Block, Dried Kelp Block |
| **Smithing** | Refinement | Anvil, Bucket, Smithing Table, Blast Furnace, Lantern, Iron Bars, Iron Door, Iron Trapdoor, Iron Chain, Iron Sword, Iron Shovel, Iron Pickaxe, Iron Axe, Iron Hoe, Shears, Golden Sword, Golden Shovel, Golden Pickaxe, Golden Axe, Golden Hoe |
| **Conduit Power** `[challenge, hidden]` | Smithing | Conduit |
| **Lapidary** | Smithing | Diamond Sword, Diamond Shovel, Diamond Pickaxe, Diamond Axe, Diamond Hoe |
| **Gold from the Embers** | Lapidary | Ancient Debris, Netherite Scrap, Netherite Ingot, Block of Netherite |
| **Netherite Passport** | Gold from the Embers | Netherite Upgrade Smithing Template |
| **Alloy Tools** `[goal]` | Gold from the Embers | Netherite Sword, Netherite Shovel, Netherite Pickaxe, Netherite Axe, Netherite Hoe |
| **Boats** | Survival | every boat (including the bamboo raft) |
| **Defense** | Survival | Leather Cap, Leather Tunic, Leather Pants, Leather Boots, Leather Horse Armor, Armor Stand, Shield |
| **Metal Armor** | Defense | Iron Helmet, Iron Chestplate, Iron Leggings, Iron Boots, Golden Helmet, Golden Chestplate, Golden Leggings, Golden Boots |
| **Crystalline Armor** | Metal Armor | Diamond Helmet, Diamond Chestplate, Diamond Leggings, Diamond Boots |
| **Emberforged** `[goal]` | Crystalline Armor | Netherite Helmet, Netherite Chestplate, Netherite Leggings, Netherite Boots |
| **Turtle Hermit** | Metal Armor | Turtle Shell |
| **Fishing Ballad** | Survival | Fishing Rod |
| **The Carrot Protocol** | Fishing Ballad | Carrot on a Stick |
| **Warped Protocol** `[goal]` | The Carrot Protocol | Warped Fungus on a Stick |
| **Hunting** | Fishing Ballad | Bow, Arrow, Fletching Table |
| **Clockwork Malice** `[goal]` | Hunting | Crossbow, Target |

### Research

| Technology | Requires | Unlocks |
|---|---|---|
| **Research** | (root) | Paper, Empty Parchment, Idea Table, Research Table, Research Book, Magnifying Glass |
| **Bibliography** | Research | Book, Book and Quill, Bookshelf, Lectern |
| **Enchanting** `[challenge, hidden]` | Bibliography | Enchanting Table, Spectral Arrow, Grindstone, Respawn Anchor |
| **Glowing Eyes** `[challenge, hidden]` | Enchanting | Eye of Ender |
| **Ender Knowledge** `[challenge, hidden]` | Glowing Eyes | End Crystal, Ender Chest, Beacon |
| **Brewing** `[challenge, hidden]` | Research | Brewing Stand, Cauldron, Blaze Powder, Magma Cream, Fermented Spider Eye, Tipped Arrow |

### Construction

| Technology | Requires | Unlocks |
|---|---|---|
| **Construction** | Stoneworking | Granite/Polished Granite, Diorite/Polished Diorite, Andesite/Polished Andesite, Tuff/Polished Tuff, Deepslate/Polished Deepslate, Basalt/Polished Basalt, Mossy Cobblestone, Mossy Cobblestone Stairs/Slab/Wall, Cobblestone Stairs/Slab/Wall, Stone, Sandstone, Sandstone Stairs/Slab/Wall, Red Sandstone, Red Sandstone Stairs/Slab/Wall, Snow Block/Snow, Blackstone, Polished Blackstone, Blackstone Stairs/Slab/Wall, Stonecutter |
| **Stonemasonry** | Construction | Stone Bricks, Cracked Stone Bricks, Chiseled Stone Bricks, Stone Brick Stairs/Slab/Wall, Stone Stairs/Slab, Smooth Stone, Smooth Sandstone, Cut Sandstone, Cut Sandstone Slab, Smooth Sandstone Stairs/Slab, Smooth Red Sandstone, Cut Red Sandstone, Cut Red Sandstone Slab, Smooth Red Sandstone Stairs/Slab, Mossy Stone Bricks, Mossy Stone Brick Stairs/Slab/Wall, Granite Stairs/Slab/Wall, Polished Granite Stairs/Slab, Diorite Stairs/Slab/Wall, Polished Diorite Stairs/Slab, Andesite Stairs/Slab/Wall, Polished Andesite Stairs/Slab, any concrete powder, Smooth Basalt, Polished Blackstone Stairs/Slab/Wall, Polished Blackstone Bricks, Cracked Polished Blackstone Bricks, Chiseled Polished Blackstone, Polished Blackstone Brick Stairs/Slab/Wall |
| **Brickwork** | Stonemasonry | Bricks, Brick Stairs, Brick Slab, Brick Wall, Flower Pot, Nether Brick, Nether Bricks, Nether Brick Stairs/Slab/Wall, Nether Brick Fence, Cracked Nether Bricks, Chiseled Nether Bricks, Red Nether Bricks, Red Nether Brick Stairs/Slab/Wall, End Stone Bricks, End Stone Brick Stairs/Slab/Wall, Clay, any terracotta |
| **Quartz** | Brickwork | Block of Quartz, Chiseled Quartz Block, Quartz Pillar, Smooth Quartz Block, Quartz Bricks, Quartz Slab, Quartz Stairs, Smooth Quartz Stairs, Smooth Quartz Slab, Glowstone, Magma Block |
| **Purpur** `[challenge, hidden]` | Quartz | Purpur Block, Purpur Pillar, Purpur Slab, Purpur Stairs, End Rod, any shulker box |
| **Glazed Tiles** | Brickwork | 16 colors of glazed terracotta |
| **Ice Harvesting** | Stonemasonry | Packed Ice, Blue Ice |
| **Carpentry** | Construction | any wooden door, any wooden trapdoor, any wooden fence, any fence gate, any sign, Chest, Barrel, Loom, Ladder, Scaffolding, Item Frame, Painting, any wool, any carpet, any banner, any bed |
| **Emblems** | Carpentry | 8 banner patterns (`flower` / `creeper` / `skull` / `mojang` / `globe` / `piglin` / `flow` / `guster`) |
| **Glassworking** | Carpentry | any glass block, any glass pane, Glass Bottle |
| **Prismarine** `[challenge, hidden]` | Glassworking | Prismarine, Prismarine Stairs/Slab/Wall, Prismarine Bricks, Prismarine Brick Stairs/Slab, Dark Prismarine, Dark Prismarine Stairs/Slab, Sea Lantern |

### Power

| Technology | Requires | Unlocks |
|---|---|---|
| **Power** | Refinement | Redstone Torch, Tripwire Hook, Trapped Chest, Redstone Lamp |
| **Activation** | Power | any wooden button, Stone Button/Polished Blackstone Button, any wooden pressure plate, Stone Pressure Plate/Polished Blackstone Pressure Plate, Light Weighted Pressure Plate, Heavy Weighted Pressure Plate, Lever |
| **Explosives** | Activation | TNT, Minecart with TNT, Firework Rocket, Firework Star, Fire Charge |
| **Cartography** | Power | Compass, Clock, Empty Map, Cartography Table, Lodestone |
| **Carts** | Power | Minecart, Minecart with Furnace, Rail |
| **Transportation** | Carts | Hopper, Powered Rail, Detector Rail, Activator Rail, Minecart with Chest, Minecart with Hopper |
| **Circuitry** | Power | Redstone Repeater, Redstone Comparator, Piston, Sticky Piston |
| **Redstone Machinery** | Circuitry | Dispenser, Dropper, Observer, Daylight Detector |
| **Music** | Power | Note Block, Jukebox |

---

## Research Recipes (Idea Table & Research Table)

Besides meeting the **criteria**, most technologies also need two "recipe" steps before they actually unlock:

1. **Idea table**: put the listed items in (order does not matter) to trigger an **idea**, then write it onto a parchment. `n kinds` means you need n ingredient groups; one item from each group is enough.
2. **Research table**: put the parchment in and solve the puzzle — a **match** (place the right items on a 3×3 grid) or a **connect** (link the two items of a production chain).

> In the grids, `[Name]` is the **hint label** shown in game for that slot; one hint usually accepts several items, see the note under each entry. `.` means the slot is empty. **Survival** and **Research** are roots: no idea table or research table step, they unlock as soon as their criteria are met.

### Construction

- **Stoneworking** — Idea table: Stick or any wooden tool + Cobblestone
  ```
  .        [Rope]   Cobblestone
  .        Stick    [Rope]
  Stick    .        .
  ```
- **Construction** — Idea table: any cobblestone + Sand/Red Sand
- **Stonemasonry** — Idea table: Stone (or Polished Granite/Diorite/Andesite/Deepslate/Tuff, Smooth Basalt, Polished Blackstone)
  ```
  [Roof]   [Roof]   [Roof]
  [Wall]   .        [Wall]
  [Wall]   [Steps]  [Wall]
  ```
  > `[Roof]` = any slab; `[Steps]` = any stairs; `[Wall]` = Stone/Polished Granite/Polished Diorite/Polished Andesite/Polished Deepslate/Polished Tuff/Smooth Basalt/Polished Blackstone.
- **Brickwork** — Idea table: Clay/Clay Ball + a heat source; Research table (connect): Terracotta → Bricks
- **Emberwisp Ignition** — Idea table: Torch/Lantern/Campfire + Soul Sand/Soul Soil (criteria: visit a soul sand valley)
- **Quartz** — Idea table: Nether Quartz + Glowstone Dust/Magma Block/Magma Cream
- **Purpur** — Idea table: Popped Chorus Fruit + Purpur Block/Pillar/Stairs/Slab/End Rod
  ```
  [Roof]   [Roof]   [Roof]
  [Pillar] [Light]  [Pillar]
  [Wall]   [Steps]  [Wall]
  ```
  > `[Pillar]` = Purpur Pillar; `[Light]` = End Rod; `[Wall]` = Purpur Block/End Stone Bricks.
- **Glazed Tiles** — Idea table: any terracotta + a heat source
- **Ice Harvesting** — Idea table: Ice + Water Bucket/Bucket
- **Carpentry** — Idea table: any wool + any planks + any wooden slab + any wooden stairs; Research table (connect): Oak Planks → White Wool
- **Barker** — Idea table: any log/stripped log + any axe; unlocks the Wood and Stripped Wood blocks of every wood type
- **Emblems** — Idea table: any banner/Shield + any flower/any dye + freeze-immune wearables (leather armor / turtle shell)
- **Glassworking** — Idea table: Sand + Glass
- **Prismarine** — Idea table: Prismarine/Sea Lantern + Prismarine Shard + Prismarine Crystals

### Power

- **Power** — Idea table: Redstone Dust + button/pressure plate/lever/stick
  ```
  [Power]  .        .
  [Rod]    [Power]  [Power]
  [Board]  [Board]  [Board]
  ```
  > `[Board]` = Stone/Stone Slab/any wool/any terracotta/any concrete.
- **Activation** — Idea table: Stick + Redstone Dust + door/fence gate/trapdoor; Research table (connect): Iron Door → Lever
- **Explosives** — Idea table: Gunpowder + Sand + Flint and Steel + any dye
- **Cartography** — Idea table: Paper + Redstone Dust/Feather/Ink Sac
- **Carts** — Idea table: Iron Ingot + Stick
  ```
  [Cart]   .        [Cart]
  [Cart]   [Cart]   [Cart]
  [Rail]   [Tie]    [Rail]
  ```
  > `[Cart]` = Iron Ingot/any planks; `[Rail]` = Iron Ingot; `[Tie]` = Stick/any wooden slab.
- **Transportation** — Idea table: Redstone Dust + Minecart/Rail + Chest; Research table (connect): Rail → Redstone Dust
- **Circuitry** — Idea table: Redstone Dust/Redstone Torch + Nether Quartz/Stone/Stone Slab
  ```
  .        .        .
  [Input]  [NOT]    [NOT]
  [Board]  [Board]  [Board]
  ```
  > `[Input]` = Redstone Dust; `[NOT]` = Stone (or Redstone Torch).
- **Redstone Machinery** — Idea table: Bow + Redstone Dust
  ```
  .        .        .
  [Input]  [Pulse]  [Pulse]
  [Board]  [Pulse]  [Board]
  ```
  > `[Input]` = Redstone Dust; `[Pulse]` = Stone/Redstone Repeater/Sticky Piston.
- **Music** — Idea table: Redstone Dust + Diamond + any planks
  ```
  .        [Stylus] [Tone Arm]
  [Music]  .        .
  [Table]  [Table]  [Table]
  ```
  > `[Tone Arm]` = Stick/Iron Ingot; `[Stylus]` = any nugget/Diamond; `[Music]` = any music disc; `[Table]` = any planks/any wooden slab.

### Research

- **Bibliography** — Idea table: Paper + Leather
- **Enchanting** — Idea table: any enchanted item + Book + Lapis Lazuli; Research table (connect): Book → Iron Sword
- **Glowing Eyes** — Idea table: Ender Pearl + Blaze Powder
- **Ender Knowledge** — Idea table: Dragon Egg/Dragon's Breath/Dragon Head + Soul Sand + Wither Skeleton Skull
  ```
  .        [Wither] .
  .        [Man]    .
  .        [Dragon] .
  ```
  > `[Man]` = Crafting Table/any bed; `[Dragon]` = Dragon Egg/Dragon's Breath/Dragon Head.
- **Brewing** — Idea table: Potion/Water Bucket + Nether Wart + Sugar
  ```
  .        [Swift]  .
  .        [Awkward].
  .        [Liquid] .
  ```
  > `[Liquid]` = Potion/Water Bucket.

### Survival

- **Agriculture** — Idea table: crop/seed/berry/fruit/mushroom + Dirt
  ```
  .        .        .
  [Leaning][Crop]   [Leaning]
  [Soil]   [Soil]   [Soil]
  ```
  > `[Leaning]` = Stick; `[Crop]` = any item of the crop/seed/berry/fruit/mushroom tags; `[Soil]` = Dirt.
- **Apiculture** — Idea table: Honeycomb/Honey Bottle + Shears/axe (criteria: bottle honey from a hive, or shear honeycomb off a hive)
- **Cooking** — Idea table: a heat source/Bowl + raw meat/Potato/Carrot/Wheat/Pumpkin/Beetroot/Mushroom/Kelp
  ```
  .        .        .
  [Veggie] [Meat]   [Fruit]
  .        [Bowl]   .
  ```
  > `[Veggie]` = Carrot/Potato/Beetroot/Wheat Crops/Pumpkin/Mushroom/Kelp; `[Meat]` = any raw meat; `[Fruit]` = Apple/Melon/Chorus Fruit/Sugar Cane/Sweet Berries/Glow Berries.
- **Flower Language** — Idea table: mushroom/fungus + bowl/cauldron/bucket + any small flower
- **Gilded Cuisine** — Idea table: Block of Gold/Gold Ingot/Gold Nugget + Apple/Carrot/Melon Slice
  ```
  [Gilded] [Gilded] [Gilded]
  [Gilded] [Dish]   [Gilded]
  [Gilded] [Gilded] [Gilded]
  ```
  > `[Gilded]` = Gold Nugget; `[Dish]` = Apple/Carrot/Melon Slice.
- **Dyes** — Idea table: flower + dye + Cactus; Research table (connect): Flint → Ink Sac
- **Refinement** — Idea table: ore/raw material + a heat source + pickaxe
  ```
  [Insulator][Insulator][Insulator]
  [Insulator][Ore]     [Insulator]
  [Insulator][Fuel]    [Insulator]
  ```
  > `[Insulator]` = Cobblestone/Blackstone/Cobbled Deepslate; `[Ore]` = any raw ore or raw material; `[Fuel]` = a heat source (furnace/smoker/blast furnace/magma block/campfire/coal...).
- **Smithing** — Idea table: any ingot/nugget, Block of Copper/Gold/Iron/Netherite; Research table (connect): Oak Planks → Anvil
- **Conduit Power** — Idea table: Nautilus Shell + Kelp + Prismarine Crystals/Shard/Prismarine/Dark Prismarine/Prismarine Bricks
  ```
  [Frame]  [Frame]  [Frame]
  [Frame]  [Core]   [Frame]
  [Frame]  [Frame]  [Frame]
  ```
  > `[Core]` = Nautilus Shell.
- **Lapidary** — Idea table: any gem; Research table (connect): Iron Ingot → Diamond
- **Gold from the Embers** — Idea table: any 2 of (Ancient Debris/Netherite Scrap/Gold Ingot/Gilded Blackstone); Research table (match):
  ```
  [Smeltable][Gilded] [Smeltable]
  [Gilded]   .        [Gilded]
  [Smeltable][Gilded] [Smeltable]
  ```
  > `[Smeltable]` = Netherite Scrap/Ancient Debris/Iron Ingot/Iron Nugget (inspect ancient debris to decipher this slot); `[Gilded]` = Gold Ingot/Gold Nugget. Criteria: carry ancient debris back from the Nether to the Overworld.
- **Netherite Passport** — Idea table: any beacon payment item (Iron/Gold Ingot, Diamond, Emerald, Netherite Ingot) + Netherrack/Nether Brick; Research table (connect): Smithing Table → Netherite Upgrade Smithing Template
- **Alloy Tools** — Idea table: Ancient Debris/Netherite Scrap/Netherite Ingot/Block of Netherite + any diamond tool; Research table (connect): Minecart with Furnace → Stone Pressure Plate
- **Boats** — Idea table: Bowl
- **Defense** — Idea table: sword + Leather/armor + Iron Ingot + any planks
- **Metal Armor** — Idea table: armor + any ingot/nugget; Research table (connect): Armor Stand → Iron Ingot
- **Crystalline Armor** — Idea table: armor + any gem; Research table (connect): Leather → Diamond
- **Emberforged** — Idea table: Ancient Debris/Netherite Scrap/Netherite Ingot/Block of Netherite + any diamond armor; Research table (connect): Lodestone → Stone
- **Turtle Hermit** — Idea table: Turtle Scute + Kelp + Leather Cap/Chainmail Helmet/Golden Helmet/Iron Helmet; Research table (connect): Rabbit Hide → Lectern
- **Fishing Ballad** — Idea table: Stick/Bamboo/Blaze Rod/Breeze Rod + String + Spider Eye/Slimeball/Rotten Flesh/Sweet Berries/Apple/Glow Berries/Sea Pickle/Kelp/Egg/Totem of Undying
- **The Carrot Protocol** — Idea table: Stick/String/Bamboo/Blaze Rod/Breeze Rod/Fishing Rod + Carrot/Potato/Beetroot
- **Warped Protocol** — Idea table: Stick/String/Bamboo/Blaze Rod/Breeze Rod/Fishing Rod + Warped Fungus (criteria: ride a strider)
- **Hunting** — Idea table: Fishing Rod/Stick/Bamboo/Blaze Rod/Breeze Rod + String + Flint/Prismarine Shard/Amethyst Shard/any nugget
- **Clockwork Malice** — Idea table: Bow/Stick/Bamboo/Blaze Rod/Breeze Rod + Tripwire Hook + any ingot

> **Survival** and **Research** are the roots: no idea table or research table step, they unlock as soon as their criteria are met.

---

## Research Criteria & Triggers

A technology's `criteria` are written exactly like vanilla advancement criteria, but **only the triggers below are actually evaluated** — anything else has no listener, so the condition can never be satisfied (the mod logs a warning when it loads such a file).

| Trigger | Parameters | Meaning |
|---|---|---|
| `ftgumod:technology_unlocked` | `technology` (optional) | A technology becomes reachable; any technology if omitted |
| `ftgumod:technology_researched` | `technology` (optional) | A technology is researched; **the only technology trigger usable by a vanilla advancement** |
| `ftgumod:item_inventory` | `predicate` (an item predicate id, e.g. `ftgumod:enchantment`) | The player holds a matching item (technology-only, not for advancements) |
| `ftgumod:block_inspected` | `block` (optional), `success` (optional) | A block was inspected with the magnifying glass (`success` tells a successful decipher apart) |
| `ftgumod:recipe_locked` | — | A locked recipe was triggered |
| `ftgumod:copy_research` | — | A research was copied |
| `minecraft:location` | `player[].predicate.location` | Reaching a structure/place |
| `minecraft:player_killed_entity` | `entity[]` | Killing a mob (can add equipment, location, ... predicates) |
| `minecraft:effects_changed` | `effects` | Gaining a status effect |
| `minecraft:started_riding` | `player[].predicate.vehicle` | Starting to ride a specific vehicle |
| `minecraft:item_used_on_block` | `location[]` | Using a specific item on a specific block (combine with `minecraft:match_tool` to pin the held item) |
| `minecraft:inventory_changed` | `items[]`, `player[]` | The inventory changes (can add player predicates such as the dimension) |

Things worth knowing:

- One technology can have several criteria; the vanilla `requirements` format decides whether they must all be met or just one. Without `requirements`, each criterion forms its own group (i.e. **all of them are required**).
- Criteria **only decide whether the technology can be researched** — they do not decide whether it is drawn on the book. That is `display.hidden`'s job.
- The in-game description of a criterion uses the language key `technology.criteria.<technology path with dots>.<criterion name>`, e.g. `technology.criteria.survival.conduit.wreck` (see [Custom Technologies](#custom-technologies-data-pack)).

---

## Command Reference

All commands start with `/technology` (registered by the mod).

```
/technology grant <player> everything                    Grant every technology
/technology grant <player> only <technology id>           Grant that technology only
/technology grant <player> through <technology id>        Grant it and all of its prerequisites
/technology grant <player> from <technology id>           Grant it and all of its dependents
/technology grant <player> until <technology id>          Grant it and the technologies in between

/technology revoke <player> everything                    Revoke every technology
/technology revoke <player> only <technology id>          Revoke that technology only
/technology revoke <player> through <technology id>       Revoke it and its prerequisites
/technology revoke <player> from <technology id>          Revoke it and its dependents
/technology revoke <player> until <technology id>         Revoke it and the technologies in between

/technology test <player> <technology id> [criterion id]  Query whether a technology/criterion is met
/technology reload                                        Reload the technology data
```

- `<technology id>` looks like `ftgumod:survival/stoneworking` or `ftgumod:research/brewing`.
- **Tab completion** is word-boundary aware: `:`, `/`, `_` and `.` all count, so typing `st` suggests `ftgumod:construction/stonemasonry`, with the closest matches first.
- The four modes:
  - `only` affects that technology alone
  - `through` walks up the **prerequisite** chain (including every parent)
  - `from` walks down the **dependent** chain (including every child)
  - `until` takes everything between a root and that technology (excluding the root)

---

## Custom Technologies (Data Pack)

Technology definitions live in `data/<namespace>/technologies/`; players can override or add technologies through a data pack.

### Directory Structure

```
data/
└── ftgumod/                        # namespace
    └── technologies/
        ├── survival/               # category (group, the first half of the technology id)
        │   ├── survival.json       # id = ftgumod:survival/survival
        │   ├── stoneworking.json
        │   └── ...
        ├── construction/
        ├── research/
        └── power/
```

### Load Locations & Priority

| Location | Purpose | Saved with the world |
|------|------|-----------|
| `config/ftgumod/technologies/<namespace>/<category>/<name>.json` | Global overrides/additions | No |
| `<world folder>/technologies/<namespace>/<category>/<name>.json` | Per-world overrides/additions | Yes |
| `<world folder>/datapacks/<pack>/data/<namespace>/technologies/<category>/<name>.json` | Data pack (`.zip` works too) | Yes |
| Built in (mod JAR) | Technologies shipped with the mod | — |

**Earlier wins**: `config/` > world folder > data pack > built in. A technology defined twice keeps the highest-priority copy; the others only fill in what is still missing. Run `/technology reload` to apply changes.

> To override a built-in technology, use the `ftgumod` namespace (e.g. `config/ftgumod/technologies/ftgumod/survival/stoneworking.json`) and keep the same category and file name so the id matches.

### Technology JSON Fields

| Field | Type | Description |
|------|------|------|
| `parent` | technology id | Prerequisite; omit it for a root |
| `display` | object | The vanilla `DisplayInfo` |
| `display.icon` | `{"item": "..."}` or `{"id": "..."}` | Technology icon, both spellings accepted |
| `display.title` / `display.description` | text component | Name and description, usually `{"translate": "technology.<name>.name"}` |
| `display.frame` | `task` (default) / `goal` / `challenge` | Frame style; `challenge` plays a sound when the research is completed |
| `display.hidden` | boolean, default `false` | `true` = not drawn on the research book until the criteria are met |
| `display.x` / `display.y` | float, optional | **Both** are needed for a fixed position; writing only one is ignored with a warning. Otherwise the layout is automatic |
| `criteria` | object | Research criteria, written as in the previous section |
| `requirements` | 2D string array | Vanilla advancement format deciding AND/OR between criteria; omitted = each criterion is its own group |
| `rewards` | object | Vanilla `AdvancementRewards`, optional |
| `unlock` | array | Items unlocked once researched; each entry is an item id, a tag `{"tag": "..."}`, or `{"item": "…", "recipe_types": […]}` (see below) |
| `idea` | object | Idea table recipe |
| `idea.amount` | integer | How many ingredient groups must be filled |
| `idea.ingredients` | array | Per group: an item id, an array of item ids (any of them), `{"tag": "..."}`, or a mod item predicate — `{"type": "ftgumod:enchantment"}` (enchanted items), `{"type": "ftgumod:fluid", "fluid": "…"}` (containers of a fluid), `{"type": "ftgumod:mod", "modid": "…"}` |
| `research` | object | Research table puzzle |
| `research.type` | `ftgumod:match` / `ftgumod:connect` | Match (3×3 grid) or connect (two items) |
| `research.pattern` | 3 strings | `match` only; spaces leave a slot empty |
| `research.key` | object | `match` only; keys are the letters in `pattern`, values are `{"item": …, "hint": {"translate": …}}` |
| `research.left` / `research.right` | item | `connect` only; the two ends (an id or `{"item": …}`) |
| `gamestage` | string, optional | Requires a GameStage before it can be researched |
| `start` | boolean, default `false` | A root technology (researchable with no prerequisite) |
| `copy` | boolean, default `true` | Whether the research may be copied with the research book |

### Minimal Example

```json
{
  "display": {
    "icon": { "item": "minecraft:carrot_on_a_stick" },
    "title": { "translate": "technology.carrot_protocol.name" },
    "description": { "translate": "technology.carrot_protocol.desc" }
  },
  "parent": "ftgumod:survival/fishing",
  "criteria": {
    "ride_pig": {
      "trigger": "minecraft:started_riding",
      "conditions": {
        "player": [
          {
            "condition": "minecraft:entity_properties",
            "entity": "this",
            "predicate": { "vehicle": { "type": ["minecraft:pig"] } }
          }
        ]
      }
    }
  },
  "idea": {
    "amount": 2,
    "ingredients": [
      ["minecraft:stick", "minecraft:string", "minecraft:fishing_rod"],
      ["minecraft:carrot", "minecraft:potato", "minecraft:beetroot"]
    ]
  },
  "unlock": ["minecraft:carrot_on_a_stick"]
}
```

### Language Keys

| Key | Purpose |
|----|------|
| `technology.<technology>.name` / `.desc` | Name and description; `<technology>` is the file name without `.json`, e.g. `technology.stonemasonry.name` |
| `technology.criteria.<category>.<technology>.<criterion>` | Description of a criterion; `/` in the path becomes `.`, e.g. `technology.criteria.survival.conduit.wreck` |
| `technology.hint.<hint>` | The hint label referenced by `hint` inside `research.key`, shown on that slot of the research table |

### unlock recipe_types Filtering

unlock searches every recipe type by default. Use `recipe_types` to narrow it down:

```json
"unlock": [
  "minecraft:iron_ingot",
  {"item": "minecraft:glass", "recipe_types": ["minecraft:smelting"]}
]
```

Without `recipe_types`, every type — crafting, smelting, blasting and so on — is searched.

---

## Installation & Building

### Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.230** or newer

### Installation

1. Install [NeoForge](https://neoforged.net/) (1.21.1).
2. Download the mod jar.
3. Put the `.jar` into the `mods/` folder.
4. Start the game.

### Building from source

```bash
./gradlew build         # compile and package
./gradlew runClient     # launch a development client
./gradlew runServer     # launch a development server
```

The output goes to `build/libs/`.

---

## FAQ

**Q: I can't craft a lot of things at the start?**
A: That is intended. You have to unlock them through research. Open the research book (`R`) to see what you can research.

**Q: How do I open the research book?**
A: Craft one and press `R`, or rebind the key in the options.

**Q: Something is missing from the technology tree?**
A: Challenge technologies (ocean monument, end city, ender dragon, shipwreck, ...) are hidden by default and only appear once their criteria are met.

**Q: How do I solve the puzzles?**
A: Match puzzles want the right items on the grid; connect puzzles want two related items linked. Hints come from deciphering with the magnifying glass.

**Q: What does JEI show?**
A: Press `R` on an item to see its unlock requirements, prerequisite technology chain and how to research it. How much is shown is set by `researchGuideMode` in `config/ftgumod-common.toml`.

**Q: What happens to existing worlds?**
A: This is an alpha, so there may be undiscovered bugs — test before committing a long-term world. Since v1.1 loot tables are overridden through a data pack, so already generated chests are unaffected.

**Q: Does it work in multiplayer?**
A: Yes. Research progress is stored per player.

**Q: How do I quickly test every technology?**
A: Use `/technology grant <your name> everything`.

**Q: Where is research progress stored?**
A: In the player data, so it is saved with the world/player.

**Q: Can I customize the technology tree?**
A: Yes. Definitions live in `data/<namespace>/technologies/` and can be overridden or added through a data pack, `config/ftgumod/technologies/` or the world folder. See the "Custom Technologies (Data Pack)" section.

---

## License

- **This port**: CC BY-NC 4.0 (Creative Commons Attribution-NonCommercial 4.0)
- **Original**: *From the Ground Up* by Astavie, CC BY-NC 3.0

This work is an adapted port of the original; the original author does not endorse it. The build scaffolding files are licensed MIT by the NeoForge MDK, see [MDK-LICENSE.txt](MDK-LICENSE.txt).
