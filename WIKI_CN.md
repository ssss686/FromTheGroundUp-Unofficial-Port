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
4. [研究配方（构思台与研究台）](#研究配方构思台与研究台)
5. [研究条件与触发器](#研究条件与触发器)
6. [指令参考](#指令参考)
7. [自定义科技（数据包）](#自定义科技数据包)
8. [安装与构建](#安装与构建)
9. [FAQ](#faq)
10. [许可](#许可)

---

## 玩法与新手指南

本 mod 给 Minecraft 添加了一套**研究系统**：你不再一开始就拥有全部配方，而是必须通过研究逐步解锁科技树，才能使用对应的物品和配方。

### 核心物品

| 物品 | 用途 |
|---|---|
| **研究之书**（Research Book） | 打开科技树界面，查看已研究/未研究/可解锁的科技。默认按键 `R`。 |
| **放大镜**（Magnifying Glass） | 对世界中的方块按右键"观察"，可获得知识。某些秘密需要破译提示。 |
| **构思台**（Idea Table） | 将物品放入其中组合，以激发"灵感"，从而开启新的研究分支。 |
| **研究台**（Research Table） | 放入研究羊皮纸，解决**拼图**（匹配 / 连接两类）来完成研究。 |
| **研究羊皮纸**（Research Parchment） | 研究的载体，在构思台得到灵感后写入，再放到研究台解决。 |
| **构想羊皮纸**（Idea Parchment） | 构思台产生的灵感载体。可与研究羊皮纸一起合成为空白羊皮纸，以回收误操作的羊皮纸。 |

### 上手流程

1. **合成研究之书**，按 `R` 打开科技树，先研究**生存（Survival）**——这是根科技。
2. 研究**研究（Research）**后，解锁构思台、研究台、研究之书、放大镜，正式开启研究玩法。
3. 用**放大镜**观察方块、或通过达成**条件**（如身上有某效果、击杀特定生物）来满足科技的研究前提。
4. 在**构思台**放入配方物品激发灵感，把灵感写入**研究羊皮纸**。
5. 将羊皮纸放入**研究台**，解决拼图（放对物品/连对线），完成后该科技即解锁。

### 研究之书界面

- **滚轮**缩放，**按住左键拖动**平移；页面会自动居中，拖到边缘会卡住。
- 科技位置**自动排版**（和原版进度页同一套算法），数据包里不需要写坐标；同一页上的科技按名字排序。
- 每个科技有**边框**：普通（task）、目标（goal）、**挑战（challenge）**。完成挑战级研究时会播放音效。
- **隐藏科技**：JSON 里标了 `display.hidden` 的科技，在它的研究条件达成之前不会画在书上；条件一达成就露头（海底神殿、末地城、末影龙、沉船、女巫、骑猪、击杀持弩掠夺者这几条就是这样）。

### 研究条件（Criteria）

每个科技的研究需要满足一定条件，条件的写法与原版进度一致，常见的有：

- **effects_changed** —— 获得某个药水效果
- **player_killed_entity** —— 击杀特定生物（如女巫、手持弩的掠夺者）
- **started_riding** —— 骑上特定坐骑（如猪）
- **location** —— 抵达某个结构（如海底神殿、末地城）
- **ftgumod:item_inventory** —— 背包里拥有某物品

完整清单与限制见 [研究条件与触发器](#研究条件与触发器)。

### 破译（Decipher）

放大镜观察某些方块（如床、炼药锅、下界之星）时，不会直接显示，需要**破译**。观察后按下破译键，并根据提示（会指向特定方块/地点）完成破译，从而获得对应知识。

### JEI 研究指南

装了 JEI 时，对任意物品按下 **R** 键，除了配方还能看到它的**解锁条件、所属前置科技链、以及研究方法**（构思台要放什么、研究台拼图怎么解、还差哪些附加条件）。

显示多少由配置决定，见 `config/ftgumod-common.toml`：

| `researchGuideMode` | 效果 |
|---|---|
| `FULL`（默认） | 前置科技链 + 完整研究方法 |
| `CHAIN_ONLY` | 只显示前置科技链 |
| `DISABLED` | JEI 里完全不显示研究指南 |

---

## 科技树总览

- `survival`（生存）与 `research`（研究）是两个根科技，没有前置；其余科技都挂在它们下面。
- 箭头表示**前置 → 后继**，研究后继需先研究前置。
- 标 `[挑战]` 的是挑战级科技（边框 challenge），标 `[隐藏]` 的科技在条件达成前不出现在研究之书上。

```
survival 生存 (根)
├─ stoneworking 采石匠
│   ├─ construction 建造
│   │   ├─ stonemasonry 石材加工
│   │   │   ├─ brickwork 砌墙工
│   │   │   │   ├─ quartz 石英
│   │   │   │   │   └─ purpur 紫珀  [挑战·隐藏]
│   │   │   │   └─ glazed_tiles 琉璃匠
│   │   │   └─ ice_harvesting 采冰匠
│   │   └─ carpentry 木工
│   │       ├─ emblems 徽记
│   │       └─ glassworking 玻璃匠
│   │           └─ prismarine 海军陆战队  [挑战·隐藏]
│   ├─ agriculture 农业
│   │   ├─ cooking 烹饪
│   │   │   ├─ flower_language 花语
│   │   │   └─ gilded_cuisine 镀金菜肴
│   │   └─ dyes 染料
│   └─ refinement 冶炼
│       ├─ smithing 锻造
│       │   ├─ conduit 潮涌之力  [挑战·隐藏]
│       │   └─ lapidary 珠宝匠
│       └─ power 能源
│           ├─ activation 激活
│           │   └─ explosives 炸药
│           ├─ cartography 制图学
│           ├─ carts 推车
│           │   └─ transportation 物流业
│           ├─ circuitry 电路
│           │   └─ redstone_machinery 红石机械
│           └─ music 音乐
├─ boats 航船
├─ defense 防御
│   └─ metal_armor 铁甲
│       ├─ gem_armor 水晶甲？
│       └─ turtles 龟仙人
├─ fishing 渔歌
│   ├─ carrot_protocol 胡萝卜协议
│   └─ hunting 狩猎
│       └─ clockwork_malice 发条恶意  [目标]
└─ research 研究 (根)
    ├─ bibliography 文献
    │   └─ enchanting 附魔  [挑战·隐藏]
    │       └─ glowing_eyes 发光之眼  [挑战·隐藏]
    │           └─ ender_knowledge 末路认知  [挑战·隐藏]
    └─ brewing 酿造  [挑战·隐藏]
```

> 注：**能源（power）**类虽然自成一组，但它挂在**冶炼**下面，而 `activation`（激活）、`carts`（推车）、`explosives`（炸药）、`transportation`（物流业）四项归在能源页里；**研究（research）**是独立根科技，不与生存相连。跨类引用是正常的。

---

## 科技明细

> 解锁项按游戏内中文名列出；`任意××` 表示该标签下任意一种物品都算。科技 ID 形如 `ftgumod:construction/stonemasonry`。

### 生存类（Survival）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **生存** Survival | （根） | 任意木板、任意木台阶、任意木楼梯、木棍、木剑、木锹、木镐、木斧、工作台、火把、碗、营火 |
| **采石匠** Stoneworking | 生存 | 石剑、石锹、石镐、石斧 |
| **农业** Agriculture | 采石匠 | 木锄、石锄、小麦植株、干草捆、西瓜、西瓜种子、南瓜种子、堆肥桶、拴绳、烟熏炉、砂土、南瓜灯、皮革、下界疣块 |
| **烹饪** Cooking | 农业 | 糖、蘑菇煲、兔肉煲、甜菜汤、面包、曲奇、蛋糕、南瓜派、干海带 |
| **花语** Flower Language | 烹饪 | 谜之炖菜 |
| **镀金菜肴** Gilded Cuisine | 烹饪 | 金苹果、金胡萝卜、闪烁的西瓜片 |
| **染料** Dyes | 农业 | 淡灰色染料、灰色染料、青色染料、淡蓝色染料、紫色染料、品红色染料、粉红色染料、橙色染料、黄绿色染料 |
| **冶炼** Refinement | 采石匠 | 熔炉、铁锭、铁粒、铁块、打火石、金锭、金粒、金块、钻石、钻石块、绿宝石、绿宝石块、煤炭、煤炭块、青金石、青金石块、红石粉、红石块、黏液球、黏液块、下界石英、骨粉、骨块、干海带块 |
| **锻造** Smithing | 冶炼 | 铁砧、铁桶、锻造台、高炉、灯笼、铁栏杆、铁门、铁活板门、铁剑、铁锹、铁镐、铁斧、铁锄、剪刀、金剑、金锹、金镐、金斧、金锄 |
| **潮涌之力** Conduit Power `[挑战·隐藏]` | 锻造 | 潮涌核心 |
| **珠宝匠** Lapidary | 锻造 | 钻石剑、钻石锹、钻石镐、钻石斧、钻石锄 |
| **航船** Boats | 生存 | 各类船（含竹筏） |
| **防御** Defense | 生存 | 皮革帽子、皮革外套、皮革裤子、皮革靴子、皮革马铠、盔甲架、盾牌 |
| **铁甲** Metal Armor | 防御 | 铁头盔、铁胸甲、铁护腿、铁靴子、金头盔、金胸甲、金护腿、金靴子 |
| **水晶甲？** Crystalline Armor | 铁甲 | 钻石头盔、钻石胸甲、钻石护腿、钻石靴子 |
| **龟仙人** Turtle Hermit | 铁甲 | 海龟壳 |
| **渔歌** Fishing Ballad | 生存 | 钓鱼竿 |
| **胡萝卜协议** The Carrot Protocol | 渔歌 | 胡萝卜钓竿 |
| **狩猎** Hunting | 渔歌 | 弓、箭、制箭台 |
| **发条恶意** Clockwork Malice `[目标]` | 狩猎 | 弩 |

### 研究类（Research）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **研究** Research | （根） | 纸、空白羊皮纸、构思台、研究台、研究之书、放大镜 |
| **文献** Bibliography | 研究 | 书、书与笔、书架、讲台 |
| **附魔** Enchanting `[挑战·隐藏]` | 文献 | 附魔台、光灵箭、砂轮 |
| **发光之眼** Glowing Eyes `[挑战·隐藏]` | 附魔 | 末影之眼 |
| **末路认知** Ender Knowledge `[挑战·隐藏]` | 发光之眼 | 末地水晶、末影箱、信标 |
| **酿造** Brewing `[挑战·隐藏]` | 研究 | 酿造台、炼药锅、烈焰粉、岩浆膏、发酵蛛眼、药箭 |

### 建造类（Construction）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **建造** Construction | 采石匠 | 花岗岩/磨制花岗岩、闪长岩/磨制闪长岩、安山岩/磨制安山岩、凝灰岩/磨制凝灰岩、深板岩/磨制深板岩、玄武岩/磨制玄武岩、苔石、苔石楼梯/台阶/墙、圆石楼梯/台阶/墙、石头、砂岩、砂岩楼梯/台阶/墙、红砂岩、红砂岩楼梯/台阶/墙、雪块/雪、切石机 |
| **石材加工** Stonemasonry | 建造 | 石砖、裂纹石砖、雕纹石砖、石砖楼梯/台阶/墙、石头楼梯/台阶、平滑石头、平滑砂岩、切制砂岩、切制砂岩台阶、平滑砂岩楼梯/台阶、平滑红砂岩、切制红砂岩、切制红砂岩台阶、平滑红砂岩楼梯/台阶、苔石砖、苔石砖楼梯/台阶/墙、花岗岩楼梯/台阶/墙、磨制花岗岩楼梯/台阶、闪长岩楼梯/台阶/墙、磨制闪长岩楼梯/台阶、安山岩楼梯/台阶/墙、磨制安山岩楼梯/台阶、任意混凝土粉末 |
| **砌墙工** Brickwork | 石材加工 | 红砖块、红砖楼梯、红砖台阶、红砖墙、花盆、下界砖、下界砖块、下界砖楼梯、下界砖台阶、下界砖墙、下界砖栅栏、红色下界砖块、红色下界砖楼梯、红色下界砖台阶、红色下界砖墙、末地石砖、末地石砖楼梯、末地石砖台阶、末地石砖墙、黏土、任意陶瓦 |
| **石英** Quartz | 砌墙工 | 石英块、雕纹石英块、石英柱、平滑石英块、石英砖、石英台阶、石英楼梯、平滑石英楼梯、平滑石英台阶、荧石、岩浆块 |
| **紫珀** Purpur `[挑战·隐藏]` | 石英 | 紫珀块、紫珀柱、紫珀台阶、紫珀楼梯、末地烛、任意潜影盒 |
| **琉璃匠** Glazed Tiles | 砌墙工 | 16 色带釉陶瓦 |
| **采冰匠** Ice Harvesting | 石材加工 | 浮冰、蓝冰 |
| **木工** Carpentry | 建造 | 任意木门、任意木活板门、任意木栅栏、任意栅栏门、任意告示牌、箱子、木桶、织布机、梯子、脚手架、物品展示框、画、任意羊毛、任意地毯、任意旗帜、任意床 |
| **徽记** Emblems | 木工 | 8 种旗帜图案（`flower` / `creeper` / `skull` / `mojang` / `globe` / `piglin` / `flow` / `guster`） |
| **玻璃匠** Glassworking | 木工 | 任意玻璃块、任意玻璃板、玻璃瓶 |
| **海军陆战队** Prismarine `[挑战·隐藏]` | 玻璃匠 | 海晶石、海晶石楼梯、海晶石台阶、海晶石墙、海晶石砖、海晶石砖楼梯、海晶石砖台阶、暗海晶石、暗海晶石楼梯、暗海晶石台阶、海晶灯 |

### 能源类（Power）

| 科技 | 前置 | 解锁 |
|---|---|---|
| **能源** Power | 冶炼 | 红石火把、绊线钩、陷阱箱、红石灯 |
| **激活** Activation | 能源 | 任意木质按钮、石头按钮/磨制黑石按钮、任意木质压力板、石头压力板/磨制黑石压力板、轻质测重压力板、重质测重压力板、拉杆 |
| **炸药** Explosives | 激活 | TNT、TNT矿车、烟花火箭、烟火之星、火焰弹 |
| **制图学** Cartography | 能源 | 指南针、时钟、空地图、制图台 |
| **推车** Carts | 能源 | 矿车、动力矿车、铁轨 |
| **物流业** Transportation | 推车 | 漏斗、动力铁轨、探测铁轨、激活铁轨、运输矿车、漏斗矿车 |
| **电路** Circuitry | 能源 | 红石中继器、红石比较器、活塞、黏性活塞 |
| **红石机械** Redstone Machinery | 电路 | 发射器、投掷器、侦测器、阳光探测器 |
| **音乐** Music | 能源 | 音符盒、唱片机 |

---

## 研究配方（构思台与研究台）

除了满足**研究条件**外，多数科技还需要完成两道"配方"步骤才能正式解锁：

1. **构思台**：把指定物品放进构思台（无序摆放即可）激发**灵感**，写入羊皮纸。`需 n 样` 表示要凑齐 n 组原料，每组原料放其中任意一种即可。
2. **研究台**：把羊皮纸放入研究台，解决拼图 —— **匹配**（按 3×3 网格放对物品）或 **连接**（放 2 个物品连成一条"产物链"）。

> 网格里的 `[名字]` 是该格在游戏里显示的**提示名**，一个提示名往往对应多种物品，具体见每条的说明；`.` 表示该格为空。**生存**与**研究**两个根科技没有构思台/研究台步骤，条件满足后直接解锁。

### 建造类（Construction）

- **采石匠** Stoneworking —— 构思台：木棍或任意木质工具 + 圆石
  ```
  .        [绳索]   圆石
  .        木棍     [绳索]
  木棍      .        .
  ```
  > `[绳索]` = 线；提示是给玩家的谜面，下同。
- **建造** Construction —— 构思台：任意圆石类方块 + 沙子/红沙
- **石材加工** Stonemasonry —— 构思台：石头（或磨制花岗岩/闪长岩/安山岩/深板岩/凝灰岩、平滑玄武岩、磨制黑石）
  ```
  [顶部]  [顶部]  [顶部]
  [墙壁]   .      [墙壁]
  [墙壁]  [步道]  [墙壁]
  ```
  > `[顶部]` = 任意台阶；`[步道]` = 任意楼梯；`[墙壁]` = 石头/磨制花岗岩/磨制闪长岩/磨制安山岩/磨制深板岩/磨制凝灰岩/平滑玄武岩/磨制黑石。
- **砌墙工** Brickwork —— 构思台：黏土/黏土球 + 加热物；研究台（连接）：陶瓦 → 红砖块
- **石英** Quartz —— 构思台：下界石英 + 荧石粉/岩浆块/岩浆膏
- **紫珀** Purpur —— 构思台：爆裂紫颂果 + 紫珀块/紫珀柱/紫珀楼梯/紫珀台阶/末地烛
  ```
  [顶部]  [顶部]  [顶部]
  [台柱]  [光照]  [台柱]
  [墙壁]  [步道]  [墙壁]
  ```
  > `[台柱]` = 紫珀柱；`[光照]` = 末地烛；`[墙壁]` = 紫珀块/末地石砖。
- **琉璃匠** Glazed Tiles —— 构思台：任意陶瓦 + 加热物
- **采冰匠** Ice Harvesting —— 构思台：冰 + 水桶/铁桶
- **木工** Carpentry —— 构思台：任意羊毛 + 任意木板 + 任意木台阶 + 任意木楼梯；研究台（连接）：橡木木板 → 白色羊毛
- **徽记** Emblems —— 构思台：任意旗帜/盾牌 + 任意花/任意染料 + 防冻装备（皮革装备/海龟壳）
- **玻璃匠** Glassworking —— 构思台：沙子 + 玻璃
- **海军陆战队** Prismarine —— 构思台：海晶石/海晶灯 + 海晶碎片 + 海晶砂粒

### 能源类（Power）

- **能源** Power —— 构思台：红石粉 + 按钮/压力板/拉杆/木棍
  ```
  [能量]   .       .
  [杠杆]  [能量]  [能量]
  [电路板][电路板][电路板]
  ```
  > `[电路板]` = 石头/石头台阶/任意羊毛/任意陶瓦/任意混凝土。
- **激活** Activation —— 构思台：木棍 + 红石粉 + 门/栅栏门/活板门；研究台（连接）：铁门 → 拉杆
- **炸药** Explosives —— 构思台：火药 + 沙子 + 打火石 + 任意染料
- **制图学** Cartography —— 构思台：纸 + 红石粉/羽毛/墨囊
- **推车** Carts —— 构思台：铁锭 + 木棍
  ```
  [推车]   .      [推车]
  [推车]  [推车]  [推车]
  [轨道]  [连接]  [轨道]
  ```
  > `[推车]` = 铁锭/任意木板；`[轨道]` = 铁锭；`[连接]` = 木棍/任意木台阶。
- **物流业** Transportation —— 构思台：红石粉 + 矿车/铁轨 + 箱子；研究台（连接）：铁轨 → 红石粉
- **电路** Circuitry —— 构思台：红石粉/红石火把 + 下界石英/石头/石头台阶
  ```
  .        .        .
  [输入]   [非门]   [非门]
  [电路板] [电路板] [电路板]
  ```
  > `[输入]` = 红石粉；`[非门]` = 石头（或红石火把）。
- **红石机械** Redstone Machinery —— 构思台：弓 + 红石粉
  ```
  .        .        .
  [输入]   [脉冲器] [脉冲器]
  [电路板] [脉冲器] [电路板]
  ```
  > `[输入]` = 红石粉；`[脉冲器]` = 石头/红石中继器/黏性活塞。
- **音乐** Music —— 构思台：红石粉 + 钻石 + 任意木板
  ```
  .        [唱针]   [唱臂]
  [音乐]    .        .
  [桌台]   [桌台]   [桌台]
  ```
  > `[唱臂]` = 木棍/铁锭；`[唱针]` = 任意金属粒/钻石；`[音乐]` = 任意唱片；`[桌台]` = 任意木板/任意木台阶。

### 研究类（Research）

- **文献** Bibliography —— 构思台：纸 + 皮革
- **附魔** Enchanting —— 构思台：任意附魔物品 + 书 + 青金石；研究台（连接）：书 → 铁剑
- **发光之眼** Glowing Eyes —— 构思台：末影珍珠 + 烈焰粉
- **末路认知** Ender Knowledge —— 构思台：龙蛋/龙息/龙首 + 灵魂沙 + 凋灵骷髅头颅
  ```
  .        [凋零]   .
  .        [人类]   .
  .        [龙]     .
  ```
  > `[人类]` = 工作台/任意床；`[龙]` = 龙蛋/龙息/龙首。
- **酿造** Brewing —— 构思台：药水/水桶 + 下界疣 + 糖
  ```
  .        [迅捷]   .
  .        [粗制]   .
  .        [流体]   .
  ```
  > `[迅捷]` = 糖；`[粗制]` = 下界疣；`[流体]` = 药水/水桶。

### 生存类（Survival）

- **农业** Agriculture —— 构思台：作物/种子/浆果/水果/蘑菇 + 泥土
  ```
  .        .        .
  [支撑]   [作物]   [支撑]
  [土壤]   [土壤]   [土壤]
  ```
  > `[支撑]` = 木棍；`[作物]` = 作物/种子/浆果/水果/蘑菇标签下任意物品；`[土壤]` = 泥土。
- **烹饪** Cooking —— 构思台：加热物/碗 + 生肉/马铃薯/胡萝卜/小麦/南瓜/甜菜根/蘑菇/海带
  ```
  .        .        .
  [蔬菜]   [肉类]   [水果]
  .        [木碗]   .
  ```
  > `[蔬菜]` = 胡萝卜/马铃薯/甜菜根/小麦植株/南瓜/蘑菇/海带；`[肉类]` = 任意生肉；`[水果]` = 苹果/西瓜/紫颂果/甘蔗/甜浆果/发光浆果。
- **花语** Flower Language —— 构思台：蘑菇/菌类 + 碗/炼药锅/桶 + 任意小型花
- **镀金菜肴** Gilded Cuisine —— 构思台：金块/金锭/金粒 + 苹果/胡萝卜/西瓜片
  ```
  [鎏金]  [鎏金]  [鎏金]
  [鎏金]  [菜肴]  [鎏金]
  [鎏金]  [鎏金]  [鎏金]
  ```
  > `[鎏金]` = 金粒；`[菜肴]` = 苹果/胡萝卜/西瓜片。
- **染料** Dyes —— 构思台：花 + 染料 + 仙人掌；研究台（连接）：燧石 → 墨囊
- **冶炼** Refinement —— 构思台：矿石/粗矿 + 加热物 + 镐
  ```
  [隔离]  [隔离]  [隔离]
  [隔离]  [冶炼物][隔离]
  [隔离]  [燃料]  [隔离]
  ```
  > `[隔离]` = 圆石/黑石/深板岩圆石；`[冶炼物]` = 任意原矿或粗矿；`[燃料]` = 加热物（熔炉/烟熏炉/高炉/岩浆块/营火/煤炭等）。
- **锻造** Smithing —— 构思台：金属锭/金属粒/铜块/金块/铁块/下界合金块；研究台（连接）：橡木木板 → 铁砧
- **潮涌之力** Conduit Power —— 构思台：鹦鹉螺壳 + 海带 + 海晶砂粒/海晶碎片/海晶石/暗海晶石/海晶石砖
  ```
  [框架]  [框架]  [框架]
  [框架]  [核心]  [框架]
  [框架]  [框架]  [框架]
  ```
  > `[核心]` = 鹦鹉螺壳。
- **珠宝匠** Lapidary —— 构思台：任意宝石；研究台（连接）：铁锭 → 钻石
- **航船** Boats —— 构思台：碗
- **防御** Defense —— 构思台：剑 + 皮革/护甲 + 铁锭 + 任意木板
- **铁甲** Metal Armor —— 构思台：护甲 + 金属锭/金属粒；研究台（连接）：盔甲架 → 铁锭
- **水晶甲？** Crystalline Armor —— 构思台：护甲 + 宝石；研究台（连接）：皮革 → 钻石
- **龟仙人** Turtle Hermit —— 构思台：海龟鳞甲 + 海带 + 皮革帽子/锁链头盔/金头盔/铁头盔；研究台（连接）：兔子皮 → 讲台
- **渔歌** Fishing Ballad —— 构思台：木棍/竹子/烈焰棒/旋风棒 + 线 + 蜘蛛眼/黏液球/腐肉/甜浆果/苹果/发光浆果/海泡菜/海带/鸡蛋/不死图腾
- **胡萝卜协议** The Carrot Protocol —— 构思台：木棍/线/竹子/烈焰棒/旋风棒/钓鱼竿 + 胡萝卜/马铃薯/甜菜根
- **狩猎** Hunting —— 构思台：钓鱼竿/木棍/竹子/烈焰棒/旋风棒 + 线 + 燧石/海晶碎片/紫水晶碎片/任意金属粒
- **发条恶意** Clockwork Malice —— 构思台：弓/木棍/竹子/烈焰棒/旋风棒 + 绊线钩 + 任意金属锭

> **生存**（Survival）与**研究**（Research）为根科技，无构思台/研究台步骤，研究条件满足后直接解锁。

---

## 研究条件与触发器

科技 JSON 里的 `criteria` 写法与原版进度完全一致，但**只有下面这些触发器真的会被判定**——其余触发器没有任何监听者，条件永远不会达成（加载时会往日志里打一条 warning）。

| 触发器 | 参数 | 说明 |
|---|---|---|
| `ftgumod:technology_unlocked` | `technology`（可选） | 解锁（位置够得着）指定科技时；不写则任意科技 |
| `ftgumod:technology_researched` | `technology`（可选） | 完成研究指定科技时；**这是唯一能直接用于原版成就的科技触发器** |
| `ftgumod:item_inventory` | `predicate`（物品谓词 id，如 `ftgumod:enchantment`） | 背包里拥有匹配的物品（只用于科技，不能用于成就） |
| `ftgumod:block_inspected` | `block`（可选）、`success`（可选） | 用放大镜观察过某方块（`success` 区分破译成功与否） |
| `ftgumod:recipe_locked` | — | 触发到被锁配方时 |
| `ftgumod:copy_research` | — | 复制研究时 |
| `minecraft:location` | `player[].predicate.location` | 抵达某个结构/位置 |
| `minecraft:player_killed_entity` | `entity[]` | 击杀特定生物（可加装备、位置等谓词） |
| `minecraft:effects_changed` | `effects` | 获得某种状态效果 |
| `minecraft:started_riding` | `player[].predicate.vehicle` | 开始骑乘特定坐骑 |

几个要点：

- 一个科技可以写多条判据，用原版的 `requirements` 写法决定"全都要满足"还是"满足其一"。不写 `requirements` 时，每条判据各算一组（即**全部都要满足**）。
- 判据**只决定"能不能研究"**，不决定这个科技画不画在研究之书上——画不画由 `display.hidden` 单独说了算。
- 判据在游戏内的说明文字走语言键 `technology.criteria.<科技路径点分>.<判据名>`，例如 `technology.criteria.survival.conduit.wreck`（书写规范见 [自定义科技](#自定义科技数据包)）。

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
- 输入时可用 **Tab 自动补全**：`:`、`/`、`_`、`.` 都算词边界，所以敲 `st` 就能补到 `ftgumod:construction/stonemasonry`，补全结果按贴合程度排序。
- `only` / `through` / `from` / `until` 四种模式的区别：
  - `only` 只影响该科技本身
  - `through` 沿**前置链**向上（含所有父节点）
  - `from` 沿**后继链**向下（含所有子节点）
  - `until` 取根到该科技之间（不含根）

---

## 自定义科技（数据包）

科技定义文件位于 `data/<命名空间>/technologies/`，玩家可通过数据包覆盖或新增科技。

### 目录结构

```
data/
└── ftgumod/                        # 命名空间
    └── technologies/
        ├── survival/               # 类别（分组，决定科技 id 的前半段）
        │   ├── survival.json       # id = ftgumod:survival/survival
        │   ├── stoneworking.json
        │   └── ...
        ├── construction/
        ├── research/
        └── power/
```

### 加载位置与覆盖优先级

| 位置 | 用途 | 是否随存档 |
|------|------|-----------|
| `config/ftgumod/technologies/<命名空间>/<类别>/<名>.json` | 全局修改/新增 | 否 |
| `<存档目录>/technologies/<命名空间>/<类别>/<名>.json` | 存档级修改/新增 | 是 |
| `<存档目录>/datapacks/<包>/data/<命名空间>/technologies/<类别>/<名>.json` | 数据包（支持 `.zip`） | 是 |
| 内置（模组 JAR） | 模组自带科技 | — |

**先加载的优先**：`config/` > 存档目录 > 数据包 > 内置。同名的科技只会留下优先级最高的那一份，后面的只是"补空缺"。改完用 `/technology reload` 重新加载即可生效。

> 覆盖内置科技时命名空间要写成 `ftgumod`（例如 `config/ftgumod/technologies/ftgumod/survival/stoneworking.json`），类别与文件名也要和原科技一致，id 才对得上。

### 科技 JSON 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `parent` | 科技 id | 前置科技；根科技不写 |
| `display` | 对象 | 原版进度同款 `DisplayInfo` |
| `display.icon` | `{"item": "..."}` 或 `{"id": "..."}` | 科技图标，两种写法都收 |
| `display.title` / `display.description` | 文本组件 | 名称与描述，一般写成 `{"translate": "technology.<科技名>.name"}` |
| `display.frame` | `task`（默认）/ `goal` / `challenge` | 边框样式，`challenge` 在研究完成时会放音效 |
| `display.hidden` | 布尔，默认 `false` | `true` = 研究条件达成前不画在研究之书上 |
| `display.x` / `display.y` | 浮点，可选 | **两个都写**才固定坐标；只写一个会被忽略并打一条 warning。不写则自动排版 |
| `criteria` | 对象 | 研究条件，写法见上一节 |
| `requirements` | 二维字符串数组 | 同原版进度，决定判据之间是"与"还是"或"；不写 = 每条判据各自一组 |
| `rewards` | 对象 | 原版 `AdvancementRewards`，可选 |
| `unlock` | 数组 | 研究完成后解锁的物品；每项可以是物品 id、标签 `{"tag": "..."}`、或 `{"item": "…", "recipe_types": […]}`（见下） |
| `idea` | 对象 | 构思台配方 |
| `idea.amount` | 整数 | 需要凑齐几组原料 |
| `idea.ingredients` | 数组 | 每组原料：物品 id、物品 id 数组（任选其一）、`{"tag": "..."}`，或用模组物品谓词 `{"type": "ftgumod:enchantment"}`（附魔物品）、`{"type": "ftgumod:fluid", "fluid": "…"}`、`{"type": "ftgumod:mod", "modid": "…"}` |
| `research` | 对象 | 研究台谜题 |
| `research.type` | `ftgumod:match` / `ftgumod:connect` | 匹配（3×3 网格）或连接（两个物品） |
| `research.pattern` | 3 个字符串 | `match` 专用，空格表示留空 |
| `research.key` | 对象 | `match` 专用，键是 `pattern` 里的字母，值为 `{"item": …, "hint": {"translate": …}}` |
| `research.left` / `research.right` | 物品 | `connect` 专用，两端物品（可用 `{"item": …}` 或直接写 id） |
| `gamestage` | 字符串，可选 | 需要某个游戏阶段（GameStage）才能研究 |
| `start` | 布尔，默认 `false` | 根科技（无前置时可研究） |
| `copy` | 布尔，默认 `true` | 是否允许用研究之书复制这份研究 |

### 最小示例

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

### 语言键

| 键 | 用途 |
|----|------|
| `technology.<科技名>.name` / `.desc` | 科技名与描述，`<科技名>` 是类别目录下的文件名（不含 `.json`），例如 `technology.stonemasonry.name` |
| `technology.criteria.<类别>.<科技名>.<判据名>` | 判据的说明文字，路径里的 `/` 换成 `.`，例如 `technology.criteria.survival.conduit.wreck` |
| `technology.hint.<提示名>` | `research.key` 里 `hint` 指向的提示名，决定研究台格子上的提示文字 |

### 自定义 unlock 的 recipe_types 过滤

unlock 默认搜索全部配方类型。可通过 `recipe_types` 字段限制搜索范围：

```json
"unlock": [
  "minecraft:iron_ingot",
  {"item": "minecraft:glass", "recipe_types": ["minecraft:smelting"]}
]
```

不指定 `recipe_types` 时，crafting、smelting、blasting 等所有类型都会被搜索。

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
A: 正常。你需要先通过研究解锁。打开研究之书（`R`）查看当前可研究项。

**Q: 研究之书怎么打开？**
A: 合成研究之书后按 `R`，或在设置里改绑定的按键。

**Q: 科技树上有东西没显示出来？**
A: 挑战级科技（海底神殿、末地城、末影龙、沉船等）默认是隐藏的，达成条件后才会出现在书上。

**Q: 拼图怎么解？**
A: 匹配拼图（Match）按提示放对物品；连接拼图（Connect）把相关联的物品连成线。提示可通过放大镜破译获得。

**Q: 装了 JEI 能看到什么？**
A: 对物品按 `R` 可以看到它的解锁条件、前置科技链和研究方法；显示多少可以在 `config/ftgumod-common.toml` 的 `researchGuideMode` 里调。

**Q: 已有旧存档会怎样？**
A: 这是 alpha 版，可能存在未发现的 bug，建议在长期存档前先测试。v1.1 起战利品表改用数据包覆盖方式，旧存档中已生成的箱子不受影响。

**Q: 和多人游戏兼容吗？**
A: 支持。研究进度按玩家独立保存。

**Q: 如何快速测试所有科技？**
A: 使用指令 `/technology grant <你的名字> everything`。

**Q: 研究进度存在哪？**
A: 存在玩家数据中，随存档/玩家数据保存。

**Q: 可以自定义科技树吗？**
A: 可以。科技定义位于 `data/<命名空间>/technologies/`，支持通过数据包、`config/ftgumod/technologies/` 或存档目录覆盖或新增。详见"自定义科技（数据包）"章节。

---

## 许可

- **本移植版**：CC BY-NC 4.0（Creative Commons Attribution-NonCommercial 4.0）
- **原作**：*From the Ground Up* by Astavie，CC BY-NC 3.0

本作品是原作的改编移植，原作者不为此背书。构建脚手架文件为 NeoForge MDK 的 MIT 许可，见 [MDK-LICENSE.txt](MDK-LICENSE.txt)。
