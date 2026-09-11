package com.fuxingcheng.fromthegroundup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;

/**
 * 配置文件对齐 NeoForge：<code>config/ftgumod-common.toml</code>，带注释，小节 [ftgumod]。
 *
 * <p>Fabric 没有 ModConfigSpec，这里手写一份只认这份文件用到的 TOML 子集读写器：
 * 键名、注释、顺序都照抄 NeoForge 那份 SPEC，两边的配置文件可以互相丢。
 * 认不得的键和玩家自己写的注释会原样留着，只改认得的键的值。
 */
public final class FTGUConfig {

	public static boolean cachedLoadDefaultTechnologies = true;
	public static boolean cachedGiveResearchBook = true;
	public static boolean cachedAllowResearchCopy = true;
	public static ResearchGuideMode cachedResearchGuideMode = ResearchGuideMode.FULL;

	/**
	 * JEI 研究指南显示档位
	 */
	public enum ResearchGuideMode {
		/** 前置科技链 + 研究方法（创作台原料、研究台谜题、附加解锁条件） */
		FULL,
		/** 只显示前置科技链 */
		CHAIN_ONLY,
		/** 不显示研究指南：JEI 里连栏位一起隐藏 */
		DISABLED;

		/**
		 * 按序数取值，越界退回默认档。
		 *
		 * 档位会随 TechnologyInfoMessage 从服务端发到客户端，两端模组版本不一致时
		 * 序数可能对不上，不能直接下标取值。
		 */
		public static ResearchGuideMode byOrdinal(int ordinal) {
			ResearchGuideMode[] values = values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FULL;
		}
	}

	/** NeoForge 的 ModConfig.Type.COMMON 就是这个文件名，直接放在 config/ 下 */
	private static final String CONFIG_FILE = "ftgumod-common.toml";
	private static final String SECTION = "ftgumod";
	private static final String TITLE = "FTGU Mod Configuration";

	/** 键名、注释、顺序与 NeoForge 的 ModConfigSpec 一一对应 */
	private static final List<Option> OPTIONS = List.of(
			new Option("allowResearchCopy", "If enabled, researches can be copied"),
			new Option("loadDefaultTechnologies", "If disabled, default technologies will not be loaded"),
			new Option("giveResearchBook", "If enabled, every player will get a research book when they join a new world or server"),
			new Option("researchGuideMode",
					"How much of the research guide is shown in JEI.",
					"FULL = prerequisite technologies, and how to research them (idea table ingredients, research table puzzle, other unlock conditions)",
					"CHAIN_ONLY = prerequisite technologies only",
					"DISABLED = no research guide in JEI at all",
					"Allowed Values: FULL, CHAIN_ONLY, DISABLED"));

	private record Option(String key, String... comments) {}

	public static void load() {
		File configFile = configFile();
		Map<String, String> values = read(configFile);

		// 读不到的键用默认值，跟 ModConfigSpec 一样
		cachedAllowResearchCopy = readBoolean(values, "allowResearchCopy", true);
		cachedLoadDefaultTechnologies = readBoolean(values, "loadDefaultTechnologies", true);
		cachedGiveResearchBook = readBoolean(values, "giveResearchBook", true);
		cachedResearchGuideMode = readMode(values, "researchGuideMode", ResearchGuideMode.FULL);

		// NeoForge 首次加载会把整份文件（含注释）写出来，缺键也会补回去，这里照做
		save();
	}

	public static void save() {
		File configFile = configFile();
		Map<String, String> values = currentValues();

		List<String> lines = new ArrayList<>();
		if (configFile.exists())
			try {
				lines.addAll(Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				// 读不出来就别覆盖，免得把玩家的配置写没了
				FromTheGroundUp.LOGGER.warn("Failed to read config file {}", configFile, e);
				return;
			}

		// 逐行过一遍：认得的键换成本次的值，其余内容（玩家自己加的注释、认不得的键）原样留着
		List<String> out = new ArrayList<>(lines.size() + 16);
		Set<String> updated = new HashSet<>();
		boolean fresh = lines.isEmpty();
		boolean inSection = false;
		int sectionEnd = -1; // [ftgumod] 这一节结束的位置，缺的键插这儿

		if (fresh)
			out.add("#" + TITLE);

		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("[")) {
				if (inSection)
					sectionEnd = out.size();
				inSection = isSection(trimmed);
				out.add(line);
				continue;
			}

			String key = inSection ? keyOf(trimmed) : null;
			if (key != null && values.containsKey(key)) {
				out.add(indentOf(line) + key + " = " + values.get(key));
				updated.add(key);
			} else {
				out.add(line);
			}
		}
		if (inSection)
			sectionEnd = out.size();

		List<String> missing = new ArrayList<>();
		for (Option option : OPTIONS)
			if (!updated.contains(option.key()))
				missing.add(option.key());

		if (!missing.isEmpty())
			if (sectionEnd >= 0) {
				out.addAll(sectionEnd, block(missing, values));
			} else {
				// 文件里压根没有这一节（或者文件不存在），整节补在最后
				if (!out.isEmpty() && !fresh)
					out.add("");
				out.add("[" + SECTION + "]");
				out.addAll(block(missing, values));
			}

		try {
			Files.write(configFile.toPath(), out, StandardCharsets.UTF_8);
		} catch (IOException e) {
			FromTheGroundUp.LOGGER.warn("Failed to save config file {}", configFile, e);
		}
	}

	/** 配置文件直接在 config/ 下；config/ftgumod/ 那个文件夹只放自定义科技 */
	private static File configFile() {
		return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile();
	}

	private static Map<String, String> currentValues() {
		Map<String, String> values = new HashMap<>();
		values.put("allowResearchCopy", Boolean.toString(cachedAllowResearchCopy));
		values.put("loadDefaultTechnologies", Boolean.toString(cachedLoadDefaultTechnologies));
		values.put("giveResearchBook", Boolean.toString(cachedGiveResearchBook));
		values.put("researchGuideMode", "\"" + cachedResearchGuideMode.name() + "\"");
		return values;
	}

	/** 缺的键按 OPTIONS 的顺序带注释补出来 */
	private static List<String> block(List<String> keys, Map<String, String> values) {
		List<String> block = new ArrayList<>();
		for (Option option : OPTIONS)
			if (keys.contains(option.key())) {
				for (String comment : option.comments())
					block.add("\t#" + comment);
				block.add("\t" + option.key() + " = " + values.get(option.key()));
			}
		return block;
	}

	private static Map<String, String> read(File configFile) {
		Map<String, String> values = new HashMap<>();
		if (!configFile.exists())
			return values;

		List<String> lines;
		try {
			lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			FromTheGroundUp.LOGGER.warn("Failed to read config file {}", configFile, e);
			return values;
		}

		// 键名不分小节：这份文件只有 [ftgumod] 一节，缺了表头也照样认
		for (String line : lines) {
			String content = stripComment(line).trim();
			if (content.isEmpty() || content.startsWith("["))
				continue;

			int eq = content.indexOf('=');
			if (eq <= 0)
				continue;

			String key = content.substring(0, eq).trim();
			String value = content.substring(eq + 1).trim();
			if (!key.isEmpty() && !value.isEmpty())
				values.put(key, unquote(value));
		}
		return values;
	}

	private static boolean readBoolean(Map<String, String> values, String key, boolean def) {
		String value = values.get(key);
		if (value == null)
			return def;
		if (value.equalsIgnoreCase("true"))
			return true;
		if (value.equalsIgnoreCase("false"))
			return false;

		FromTheGroundUp.LOGGER.warn("Config value {} = {} is not a boolean, using {}", key, value, def);
		return def;
	}

	private static ResearchGuideMode readMode(Map<String, String> values, String key, ResearchGuideMode def) {
		String value = values.get(key);
		if (value == null)
			return def;

		try {
			return ResearchGuideMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			FromTheGroundUp.LOGGER.warn("Config value {} = {} is not a research guide mode, using {}", key, value, def);
			return def;
		}
	}

	/** 切掉行尾注释，引号里的 # 不算 */
	private static String stripComment(String line) {
		boolean basic = false, literal = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (basic) {
				if (c == '\\')
					i++;
				else if (c == '"')
					basic = false;
			} else if (literal) {
				if (c == '\'')
					literal = false;
			} else if (c == '"') {
				basic = true;
			} else if (c == '\'') {
				literal = true;
			} else if (c == '#') {
				return line.substring(0, i);
			}
		}
		return line;
	}

	/** 去掉字符串两边的引号（单引号是字面量，不还原转义）；不是字符串就原样返回 */
	private static String unquote(String value) {
		if (value.length() < 2)
			return value;

		char quote = value.charAt(0);
		if (quote != '"' && quote != '\'' || value.charAt(value.length() - 1) != quote)
			return value;

		String body = value.substring(1, value.length() - 1);
		if (quote == '\'')
			return body;

		StringBuilder sb = new StringBuilder(body.length());
		for (int i = 0; i < body.length(); i++) {
			char c = body.charAt(i);
			if (c != '\\' || i + 1 >= body.length()) {
				sb.append(c);
				continue;
			}
			// 反斜杠加 u 的 Unicode 转义之类不还原，反正是坏值，下面会退回默认档
			switch (body.charAt(++i)) {
				case 'n' -> sb.append('\n');
				case 't' -> sb.append('\t');
				case 'r' -> sb.append('\r');
				case '"' -> sb.append('"');
				case '\\' -> sb.append('\\');
				default -> sb.append(body.charAt(i));
			}
		}
		return sb.toString();
	}

	private static boolean isSection(String trimmed) {
		return trimmed.replace(" ", "").replace("\t", "").equals("[" + SECTION + "]");
	}

	private static String keyOf(String trimmed) {
		if (trimmed.isEmpty() || trimmed.startsWith("#"))
			return null;
		String content = stripComment(trimmed);
		int eq = content.indexOf('=');
		return eq <= 0 ? null : content.substring(0, eq).trim();
	}

	private static String indentOf(String line) {
		int i = 0;
		while (i < line.length() && Character.isWhitespace(line.charAt(i)))
			i++;
		return line.substring(0, i);
	}

}
