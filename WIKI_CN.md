# From the Ground Up (Unofficial Port) — Wiki

> 本 Wiki 覆盖本 mod 的玩法、科技树、指令、安装与常见问题。
> 中文 / 英文科技名并列出。英文原版名见括号。
>
> 🌐 [English Wiki](WIKI_EN.md)

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
| **构想羊皮纸**（Idea Parchment） | 构思台产生的灵感载体。可与研究羊皮纸一起合成为空白羊皮纸，以回收误操作的羊皮纸。 |

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
| **建造** Construction | 采石匠 | 花岗岩/闪长岩/安山岩/凝灰岩/深板岩/玄武岩及其抛光变体、苔石、圆石台阶/楼梯、砂岩/红砂岩及其台阶/楼梯、雪/雪层、下界疣块、物品展示框 |
| **石材加工** Stonemasonry | 建造 | 石台阶/楼梯、石砖/錾制石砖/苔石砖、石砖台阶/楼梯、圆石墙/苔石墙、混凝土粉末 |
| **激活** Activation | 石材加工 | 各种按钮（`#button`）、石按钮/磨制黑石按钮、各种压力板（`#pressure_plate`）、石压力板/磨制黑石压力板/轻质/重质压力板、拉杆 |
| **砌墙工** Brickwork | 石材加工 | 砖块、砖台阶/楼梯、花盆、下界砖/下界砖台阶/楼梯/栅栏、红色下界砖、末地石砖、黏土、陶瓦（`#stainedHardenedClay`） |
| **石英** Quartz | 砌墙工 | 石英块/錾制石英块/石英柱/石英台阶/石英楼梯、萤石、岩浆块 |
| **紫珀** Purpur | 石英 | 紫珀块、紫珀柱、紫珀台阶、紫珀楼梯、末地烛、潜影盒、各色潜影箱（`#shulker`） |
| **木工** Carpentry | 建造 | 门（`#door`）、活板门（`#trapdoor`）、栅栏（`#fence`）、栅栏门（`#fence_gate`）、告示牌（`#sign`）、箱子、梯子、画、羊毛（`#wool`）、床（`#bed`）、地毯（`#carpet`）、旗帜（`#banner`） |
| **玻璃匠** Glassworking | 木工 | 染色玻璃（`#stained_glass`）、染色玻璃板（`#stained_glass_pane`）、玻璃板、玻璃瓶 |
| **海军陆战队** Prismarine | 玻璃匠 | 海晶石、海晶石砖、暗海晶石、海晶灯 |
| **农业** Agriculture | 采石匠 | 木锄、石锄、小麦、干草块、西瓜/西瓜种子、南瓜种子、拴绳、砂土、南瓜灯、皮革 |
| **烹饪** Cooking | 农业 | 糖、蘑菇煲、兔肉煲、甜菜汤、面包、曲奇、蛋糕、南瓜派、钓鱼竿、胡萝卜钓竿 |
| **镀金菜肴** Gilded Cuisine | 烹饪 | 金苹果、金胡萝卜、闪烁的西瓜片 |
| **染料** Dyes | 农业 | 浅灰色/灰色/青色/浅蓝色/紫色/品红色/粉红色/橙色/黄绿色染料 |
| **冶炼** Refinement | 采石匠 | 熔炉、铁锭/铁粒/铁块、打火石、金锭/金粒/金块、钻石/钻石块、绿宝石/绿宝石块、煤炭/煤块、青金石/青金石块、红石/红石块、黏液球/黏液块、骨粉/骨块 |
| **锻造** Smithing | 冶炼 | 铁砧、桶、铁栏杆、铁门/铁活板门、全套铁制与金制工具、剪刀 |
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

## 研究配方（构思台与研究台）

除了满足**研究条件**外，多数科技还需要完成两道"配方"步骤才能正式解锁：

1. **构思台**：把指定物品放进构思台（无序摆放即可）激发**灵感**，写入羊皮纸。
2. **研究台**：把羊皮纸放入研究台，解决拼图 —— **匹配**（按 3×3 网格放对物品）或 **连接**（放 3 个物品连成一条"产物链"）。

> 标注说明：`#xxx` 为 mod 自定义标签（可放列表内任意一种）；`.` 表示该格为空。物品用注册名列出，游戏内显示为对应中文名。

### 建造类（Construction）

- **采石匠** Stoneworking
  - 构思台：木棍/任意木质工具 + 圆石
  - 研究台（匹配）：
    ```
    .   [线/甘蔗]  圆石
    .   木棍      [线/甘蔗]
    木棍   .        .
    ```
- **建造** Construction
  - 构思台：`#stone`（石头或砂岩）
- **石材加工** Stonemasonry
  - 构思台：`#smoothStone`（平滑/抛光石类）
  - 研究台（匹配）：
    ```
    #stoneSlab  #stoneSlab  #stoneSlab
    #smoothStone   .        #smoothStone
    #smoothStone  #stairsStone  #smoothStone
    ```
- **激活** Activation
  - 构思台：木棍 + 任意门/栅栏门/活板门/铁门/铁活板门 + 红石粉
  - 研究台（连接）：铁门 → 拉杆
- **砌墙工** Brickwork
  - 构思台：黏土/黏土球 + `#heat`（加热物）
  - 研究台（连接）：陶瓦 → 砖块
- **石英** Quartz
  - 构思台：石英 + 萤石粉/岩浆块/岩浆膏
- **紫珀** Purpur
  - 构思台：紫颂果（烹熟）+ 紫珀块/柱/楼梯/台阶/末地烛
  - 研究台（匹配）：
    ```
    紫珀台阶  紫珀台阶  紫珀台阶
    紫珀柱    末地烛   紫珀柱
    [紫珀块/末地石砖] 紫珀楼梯 [紫珀块/末地石砖]
    ```
- **木工** Carpentry
  - 构思台：羊毛 + 木板 + 木台阶 + 木楼梯
  - 研究台（连接）：木板 → 羊毛
- **玻璃匠** Glassworking
  - 构思台：沙子 + 玻璃块
- **海军陆战队** Prismarine
  - 构思台：海晶石/海晶灯 + 海晶碎片 + 海晶砂粒

### 能源类（Power）

- **能源** Power
  - 构思台：红石粉 + 任意开关类（按钮/压力板/拉杆/木棍）
  - 研究台（匹配）：
    ```
    红石粉   .      .
    木棍    红石粉  红石粉
    [电路板] [电路板] [电路板]
    ```
    > `[电路板]` = 石头/石台阶/羊毛/陶瓦/混凝土 任选其一。
- **制图学** Cartography
  - 构思台：纸 + 红石粉/羽毛/墨囊
- **电路** Circuitry
  - 构思台：红石粉/红石火把 + 石英/石头/石台阶
  - 研究台（匹配）：
    ```
    .   .   .
    红石粉  石头  红石火把
    [电路板] [电路板] [电路板]
    ```
- **红石机械** Redstone Machinery
  - 构思台：弓 + 红石粉
  - 研究台（匹配）：
    ```
    .   .   .
    红石粉  石头  中继器
    [电路板] 粘性活塞 [电路板]
    ```
- **音乐** Music
  - 构思台：红石粉 + 钻石 + 木板
  - 研究台（匹配）：
    ```
    .    [铁粒/金粒/钻石]   [木棍/铁锭]
    .    唱片          .
    [木板/木台阶] [木板/木台阶] [木板/木台阶]
    ```

### 研究类（Research）

- **文献** Bibliography
  - 构思台：纸 + 皮革
- **附魔** Enchanting
  - 构思台：任意附魔物品
  - 研究台（连接）：书 → 铁剑
- **发光之眼** Glowing Eyes
  - 构思台：末影珍珠 + 烈焰粉
- **末路认知** Ender Knowledge
  - 构思台：`#dragon`（龙蛋/龙息/龙首）+ 下界之星
  - 研究台（匹配）：
    ```
    .   下界之星   .
    .   [工作台/床]  .
    .   #dragon   .
    ```
- **酿造** Brewing
  - 构思台：水瓶（或水桶）+ 下界疣/糖
  - 研究台（匹配）：
    ```
    .   糖     .
    .   下界疣   .
    .   水瓶    .
    ```

### 生存类（Survival）

- **农业** Agriculture
  - 构思台：`#crop`（作物）+ 泥土
  - 研究台（匹配）：
    ```
    .   .   .
    木棍  #crop  木棍
    泥土  泥土  泥土
    ```
- **烹饪** Cooking
  - 构思台：`#heat`（加热物）+ 生肉/土豆/胡萝卜/小麦
  - 研究台（匹配）：
    ```
    .   .   .
    [胡萝卜/土豆/甜菜根]  #rawMeat  [苹果/西瓜/紫颂果]
    .   木碗   .
    ```
- **镀金菜肴** Gilded Cuisine
  - 构思台：金块/金锭/金粒 + 苹果/胡萝卜/西瓜
  - 研究台（匹配）：
    ```
    金粒  金粒  金粒
    金粒  [苹果/胡萝卜/西瓜]  金粒
    金粒  金粒  金粒
    ```
- **染料** Dyes
  - 构思台：`#flower`（花）+ 染料 + 仙人掌
  - 研究台（连接）：燧石 → 墨囊
- **冶炼** Refinement
  - 构思台：`#ore`（铁/金矿石）+ `#heat`（加热物）+ `#pickaxe`（镐）
  - 研究台（匹配）：
    ```
    圆石  圆石  圆石
    圆石  #ore  圆石
    圆石  #heat  圆石
    ```
- **锻造** Smithing
  - 构思台：`#metal`（铁/金锭·粒·块）
  - 研究台（连接）：木板 → 铁砧
- **珠宝匠** Lapidary
  - 构思台：钻石 + 绿宝石
  - 研究台（连接）：铁锭 → 钻石
- **炸药** Explosives
  - 构思台：火药 + 沙子 + 打火石 + 染料
- **推车** Carts
  - 构思台：铁锭 + 木棍/船
  - 研究台（匹配）：
    ```
    [铁锭/木板]  .   [铁锭/木板]
    [铁锭/木板]  [铁锭/木板]  [铁锭/木板]
    铁锭   [木棍/木台阶]   铁锭
    ```
- **物流业** Transportation
  - 构思台：红石粉 + 矿车/船/铁轨 + 箱子
  - 研究台（连接）：铁轨 → 红石粉
- **防御** Defense
  - 构思台：`#sword`（剑）+ `#armor`（盔甲）/皮革 + 铁锭 + 木板
- **铁甲** Metal Armor
  - 构思台：`#armor`（盔甲）+ `#metal`（金属）
  - 研究台（连接）：盔甲架 → 铁锭
- **水晶甲** Crystalline Armor
  - 构思台：`#armor`（盔甲）+ `#gem`（宝石）
  - 研究台（连接）：皮革 → 钻石
- **航船** Boats
  - 构思台：木碗

> **生存**（Survival）与**研究**（Research）为根科技，无构思台/研究台步骤，研究条件满足后直接解锁。

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
A: 这是 alpha 版，可能存在未发现的 bug，建议在长期存档前先测试。v1.1 起战利品表改用数据包覆盖方式，旧存档中已生成的箱子不受影响。

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
