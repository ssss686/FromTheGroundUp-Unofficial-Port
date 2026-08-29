# From the Ground Up (Unofficial Port)

An unofficial port of [**From the Ground Up**](https://www.curseforge.com/minecraft/mc-mods/from-the-ground-up) by Astavie, updated from Minecraft **1.12.2 (Forge)**.

This mod adds a research system to Minecraft: instead of knowing every recipe from the start, you must study and unlock technologies through research before you can use them.

📖 **Wiki**: [中文版](WIKI_CN.md) · [English](WIKI_EN.md)

## Features

- **Research Book** (default key: `R`) — browse the technology tree and track your progress.
- **Magnifying Glass** — inspect blocks in the world to gain knowledge. Some secrets must be "deciphered" with a hint.
- **Idea Table** — combine items to unlock ideas and start new research branches.
- **Research Table** — solve puzzles (match or connect types) on parchment to complete research.
- **Technology tree** — progress through interconnected technologies, from basic survival to end-game knowledge.
- Custom advancements and triggers.
- Loot injection: research parchments can appear in village, desert temple, and stronghold library chests.
- English and Chinese (简体中文) language support.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.230** or later
- Forge **52.1.0** or later
- Fabric **loader version 0.19.3 and fabric api version 0.116.15** or later 

## Installation

1. Install [NeoForge](https://neoforged.net/) / [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) / [Fabric](https://www.fabricmc.net/) for Minecraft 1.21.1.
2. Download the latest `FromTheGroundUp-Unofficial-Port` release.
3. Place the `.jar` file in your `mods/` folder.
4. Launch the game.

## Commands

```
/technology grant <player> <everything|only|through|from|until> [tech]
/technology revoke <player> <everything|only|through|from|until> [tech]
/technology test <player> <tech>
/technology reload
```

Use `/technology help` or the in-game suggestions for the full syntax.

## Building from source

```bash
./gradlew build        # compile and package the mod
./gradlew runClient    # launch a development client
```

The built jar is output to `build/libs/`.

## Notes

This is an **alpha** release, ported almost directly from the 1.12.2 code. Undiscovered bugs may remain — use with caution on long-term worlds. Feedback and bug reports are welcome.

## License

- **This port**: [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/) (Creative Commons Attribution-NonCommercial 4.0)
- **Original mod**: *From the Ground Up* by Astavie, licensed under [CC BY-NC 3.0](https://creativecommons.org/licenses/by-nc/3.0/)

This is a modified adaptation of the original work. The original author is credited but does not endorse this port. The build scaffolding files are MIT-licensed by the NeoForged MDK; see [MDK-LICENSE.txt](MDK-LICENSE.txt).
