# From the Ground Up (Unofficial Port) — Wiki

> 本 Wiki 覆盖本 mod 的玩法、科技树、指令、安装与常见问题。
> 中文 / 英文科技名并列出。英文原版名见括号。

---

## 目录

1. [玩法与新手指南](#玩法与新手指南)
2. [科技树总览](#科技树总览)
3. [科技明细](#科技明细)
4. [指令参考](#指令参考)
5. [安装与构建](#安装与构建)
6. [FAQ](#faq)
7. [许可](#许可)

---

## 玩法与新手指南

本 mod 给 Minecraft 添加了一套**研究系统**：你不再一开始就拥有全部配方，而是必须通过研究逐步解锁科技树，才能使用对应的物品和配方。

### 核心物品

| 物品 | 用途 |
|---|---|
| **研究书**（Research Book） | 打开科技树界面，查看已研究/未研究/可解锁的科技。默认按键 `R`。 |
| **放大镜**（Magnifying Glass） | 对世界中的方块按右键"观察"，可获得知识。某些秘密需要破译提示。 |
| **构思台**（Idea Table） | 将物品放入其中组合，以激发"灵感"，从而开启新的研究分支。 |
| **研究台**（Research Table） | 放入研究羊皮纸，解决**拼图**（匹配 / 连接两类）来完成研究。 |
| **研究羊皮纸**（Research Parchment） | 研究的载体，在构思台得到灵感后写入，再放到研究台解决。 |

### 上手流程

1. **合成研究书**，按 `R` 打开科技树，先研究**生存（Survival）**——这是根科技。
2. 研究**研究（Research）**后，解锁构思台、研究台、研究书、放大镜，正式开启研究玩法。
3. 用**放大镜**观察方块、或通过达成**条件**（如身上有某效果、击杀特定生物）来满足科技的研究前提。
4. 在**构思台**放入配方物品激发灵感，把灵感写入**研究羊皮纸**。
5. 将羊皮纸放入**研究台**，解决拼图（放对物品/连对线），完成后该科技即解锁。

### 研究条件（Criteria）

每个科技的研究需要满足一定条件，常见类型：

- **effects_changed** —— 获得某个药水效果
- **player_killed_entity** —— 击杀特定生物（如女巫）
- **item_inventory** —— 背包里拥有某物品
- **player_trigger / location** —— 在特定位置触发

### 破译（Decipher）

放大镜观察某些方块（如床、炼药锅、下界之星）时，不会直接显示，需要**破译**。观察后按下破译键，并根据提示（会指向特定方块/地点）完成破译，从而获得对应知识。

---

## 科技树总览

- `survival`（生存）是唯一的根科技，没有前置。
- 箭头表示**前置 → 后继**，研究后继需先研究前置。

```
survival 生存
├─ stoneworking 采石匠
│   ├─ construction 建造
│   │   ├─ stonemasonry 石材加工
│   │   │   ├─ activation 激活 ──→ (power 能源)
│   │   │   └─ brickwork 砌墙工 ──→ quartz 石英 ──→ purpur 紫珀
│   │   └─ carpentry 木工 ──→ glassworking 玻璃匠 ──→ prismarine 海军陆战队
│   ├─ agriculture 农业
│   │   ├─ cooking 烹饪 ──→ gilded_cuisine 镀金菜肴
│   │   └─ dyes 染料
│   └─ refinement 冶炼
│       ├─ smithing 锻造 ──→ lapidary 珠宝匠
│       └─ explosives 炸药
├─ boats 航船
│   └─ carts 推车 ──→ transportation 物流业
├─ defense 防御
│   └─ metal_armor 铁甲 ──→ gem_armor 水晶甲
└─ research 研究  (研究类根)
    ├─ bibliography 文献 ──→ enchanting 附魔 ──→ glowing_eyes 发光之眼 ──→ ender_knowledge 末路认知
    └─ brewing 酿造

power 能源  (前置 = construction/activation)
├─ cartography 制图学
├─ circuitry 电路 ──→ redstone_machinery 红石机械
└─ music 音乐
```

> 注：`power` 虽是能源类，但其前置是建造类的 `activation`；`research` 类的前置是 `survival`。跨类引用是正常的。

---

## 科技明细

> 解锁项以 Minecraft 物品注册名（英文 ID）列出，游戏中对应物品的中文名会随游戏语言显示。

### 生存类（Survival）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **生存** Survival | （根） | wooden_sword, wooden_shovel, wooden_pickaxe, wooden_axe, crafting_table, torch, bowl 等木质工具与木板类 |
| **采石匠** Stoneworking | 生存 | stone_sword, stone_shovel, stone_pickaxe, stone_axe（石制工具） |
| **建造** Construction | 采石匠 | 各类石头/砂岩方块、雪、下界疣块、物品展示框等 |
| **石材加工** Stonemasonry | 建造 | stonebrick、石台阶、石砖楼梯、圆石墙、混凝土粉末 |
| **激活** Activation | 石材加工 | wooden/stone_button、各种压力板、拉杆 |
| **砌墙工** Brickwork | 石材加工 | brick_block、砖楼梯、花盆、下界砖、陶瓦等 |
| **石英** Quartz | 砌墙工 | quartz_block、石英楼梯、萤石、岩浆块 |
| **紫珀** Purpur | 石英 | purpur_block、purpur_pillar、purpur_stairs、末地烛、潜影盒 |
| **木工** Carpentry | 建造 | 门、活板门、栅栏、栅栏门、箱子、告示牌、梯子、画、羊毛、床、地毯、旗帜 |
| **玻璃匠** Glassworking | 木工 | stained_glass、玻璃板、玻璃瓶 |
| **海军陆战队** Prismarine | 玻璃匠 | prismarine（三种）、海晶灯 |
| **农业** Agriculture | 采石匠 | 各种锄、小麦、干草块、西瓜、南瓜、拴绳、泥土（草径）、皮革 |
| **烹饪** Cooking | 农业 | 糖、蘑菇煲、兔肉煲、甜菜汤、面包、曲奇、蛋糕、南瓜派、钓鱼竿 |
| **镀金菜肴** Gilded Cuisine | 烹饪 | golden_apple、金胡萝卜、闪烁的西瓜 |
| **染料** Dyes | 农业 | 多种染料（红/紫/青/灰/粉/黄/蓝/品红/橙等） |
| **冶炼** Refinement | 采石匠 | 熔炉、铁锭/铁块、金锭/金块、钻石/绿宝石、煤块、青金石、红石、黏液球/黏液块、骨粉/骨块 |
| **锻造** Smithing | 冶炼 | 铁砧、桶、铁栏、铁门/活板门、全套铁制与金制工具 |
| **珠宝匠** Lapidary | 锻造 | 全套钻石工具（剑/锹/镐/斧/锄） |
| **炸药** Explosives | 冶炼 | tnt、tnt_minecart、firework_charge、烟花、火焰弹 |
| **航船** Boats | 生存 | 各类船 |
| **推车** Carts | 航船 | minecart、furnace_minecart、rail |
| **物流业** Transportation | 推车 | 漏斗、各种充能/探测/激活铁轨、箱子矿车、漏斗矿车 |
| **防御** Defense | 生存 | 全套皮革护甲、盔甲架、弓、箭、盾牌 |
| **铁甲** Metal Armor | 防御 | 全套铁制与金制护甲 |
| **水晶甲** Crystalline Armor | 铁甲 | 全套钻石护甲 |

### 研究类（Research）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **研究** Research | 生存 | paper、**ftgumod:parchment_empty（空羊皮纸）**、**构思台**、**研究台**、**研究书**、**放大镜** |
| **文献** Bibliography | 研究 | book、writable_book、bookshelf（书、可写书、书架） |
| **附魔** Enchanting | 文献 | enchanting_table、spectral_arrow（附魔台、光灵箭） |
| **发光之眼** Glowing Eyes | 附魔 | ender_eye（末影之眼） |
| **末路认知** Ender Knowledge | 发光之眼 | end_crystal、ender_chest、beacon（末影水晶、末影箱、信标） |
| **酿造** Brewing | 研究 | brewing_stand、cauldron、blaze_powder、magma_cream、fermented_spider_eye、tipped_arrow |

### 能源类（Power）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **能源** Power | 建造·激活 | redstone_torch、tripwire_hook、trapped_chest、redstone_lamp |
| **制图学** Cartography | 能源 | compass、clock、map（指南针、时钟、地图） |
| **电路** Circuitry | 能源 | repeater、comparator、piston、sticky_piston |
| **红石机械** Redstone Machinery | 电路 | dispenser、dropper、observer、daylight_detector |
| **音乐** Music | 能源 | noteblock、jukebox（音符盒、唱片机） |

---

## 指令参考

所有指令以 `/technology` 开头（由 mod 注册）。

```
/technology grant <玩家> everything                      授予全部科技
/technology grant <玩家> only <科技ID>                    仅授予指定科技
/technology grant <玩家> through <科技ID>                 授予指定科技及其所有前置
/technology grant <玩家> from <科技ID>                    授予指定科技及其所有后继
/technology grant <玩家> until <科技ID>                   授予指定科技及其中间的关联科技

/technology revoke <玩家> everything                      撤销全部科技
/technology revoke <玩家> only <科技ID>                    仅撤销指定科技
/technology revoke <玩家> through <科技ID>                 撤销指定科技及其前置
/technology revoke <玩家> from <科技ID>                    撤销指定科技及其后继
/technology revoke <玩家> until <科技ID>                   撤销指定科技及其中间关联

/technology test <玩家> <科技ID> [条件ID]                  查询科技/条件是否已满足
/technology reload                                         重新加载科技数据
```

- `<科技ID>` 格式形如 `ftgumod:survival/stoneworking`、`ftgumod:research/brewing`。
- 输入时可用 **Tab 自动补全**查看可选项。
- `only` / `through` / `from` / `until` 四种模式的区别：
  - `only` 只影响该科技本身
  - `through` 沿**前置链**向上（含所有父节点）
  - `from` 沿**后继链**向下（含所有子节点）
  - `until` 取根到该科技之间（不含根）

---

## 安装与构建

### 环境要求

- Minecraft **1.21.1**
- NeoForge **21.1.230** 或更高

### 安装

1. 安装 [NeoForge](https://neoforged.net/)（1.21.1）。
2. 下载本 mod 的发布 jar。
3. 将 `.jar` 放入 `mods/` 文件夹。
4. 启动游戏。

### 从源码构建

```bash
./gradlew build         # 编译并打包
./gradlew runClient     # 启动开发客户端
./gradlew runServer     # 启动开发服务器
```

产物位于 `build/libs/`。

---

## FAQ

**Q: 一开始很多东西合成不了？**
A: 正常。你需要先通过研究解锁。打开研究书（`R`）查看当前可研究项。

**Q: 研究书怎么打开？**
A: 合成研究书后按 `R`，或在设置里改绑定的按键。

**Q: 拼图怎么解？**
A: 匹配拼图（Match）按提示放对物品；连接拼图（Connect）把相关联的物品连成线。提示可通过放大镜破译获得。

**Q: 已有旧存档会怎样？**
A: 这是 alpha 版，代码基本照搬 1.12.2，可能存在未发现的 bug，建议在长期存档前先测试。

**Q: 和多人游戏兼容吗？**
A: 支持。研究进度按玩家独立保存。

**Q: 如何快速测试所有科技？**
A: 使用指令 `/technology grant <你的名字> everything`。

**Q: 研究进度存在哪？**
A: 存在玩家数据中，随存档/玩家数据保存。

---

## 许可

- **本移植版**：CC BY-NC 4.0（Creative Commons Attribution-NonCommercial 4.0）
- **原作**：*From the Ground Up* by Astavie，CC BY-NC 3.0

本作品是原作的改编移植，原作者不为此背书。构建脚手架文件为 NeoForge MDK 的 MIT 许可，见 [MDK-LICENSE.txt](MDK-LICENSE.txt)。
