package com.fuxingcheng.fromthegroundup.command;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyInfoMessage;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyMessage;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class CommandTechnology {

	private static final SimpleCommandExceptionType PLAYER_NOT_FOUND =
			new SimpleCommandExceptionType(Component.translatable("commands.technology.playerNotFound"));

	private static CommandSyntaxException techNotFound(String id) {
		return new SimpleCommandExceptionType(Component.translatable(
				"commands.technology.technologyNotFound", id)).create();
	}

	public static Technology findTechnology(String id) throws CommandSyntaxException {
		ResourceLocation rl = ResourceLocation.tryParse(id);
		if (rl == null)
			throw techNotFound(id);
		Technology tech = TechnologyManager.INSTANCE.getTechnology(rl);
		if (tech == null)
			throw techNotFound(id);
		return tech;
	}

	private static com.mojang.brigadier.tree.CommandNode<CommandSourceStack> buildCommandTree() {
		return Commands.literal("technology")
				.requires(s -> s.hasPermission(2))
				.then(Commands.literal("reload")
						.executes(ctx -> reload(ctx.getSource())))
				.then(Commands.literal("grant")
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(PLAYER_SUGGEST)
								.then(Commands.literal("everything")
										.executes(ctx -> everything(ctx.getSource(),
												getPlayer(ctx, "player"), ActionType.GRANT)))
								.then(Commands.literal("only")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.ONLY,
														findTechnology(getTechArg(ctx)), null, ActionType.GRANT))))
								.then(Commands.literal("through")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.THROUGH,
														findTechnology(getTechArg(ctx)), null, ActionType.GRANT))))
								.then(Commands.literal("from")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.FROM,
														findTechnology(getTechArg(ctx)), null, ActionType.GRANT))))
								.then(Commands.literal("until")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.UNTIL,
														findTechnology(getTechArg(ctx)), null, ActionType.GRANT))))))
				.then(Commands.literal("revoke")
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(PLAYER_SUGGEST)
								.then(Commands.literal("everything")
										.executes(ctx -> everything(ctx.getSource(),
												getPlayer(ctx, "player"), ActionType.REVOKE)))
								.then(Commands.literal("only")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.ONLY,
														findTechnology(getTechArg(ctx)), null, ActionType.REVOKE))))
								.then(Commands.literal("through")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.THROUGH,
														findTechnology(getTechArg(ctx)), null, ActionType.REVOKE))))
								.then(Commands.literal("from")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.FROM,
														findTechnology(getTechArg(ctx)), null, ActionType.REVOKE))))
								.then(Commands.literal("until")
										.then(Commands.argument("technology", StringArgumentType.greedyString())
												.suggests(TECH_SUGGEST)
												.executes(ctx -> perform(ctx.getSource(),
														getPlayer(ctx, "player"), Mode.UNTIL,
														findTechnology(getTechArg(ctx)), null, ActionType.REVOKE))))))
				.then(Commands.literal("test")
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(PLAYER_SUGGEST)
								.then(Commands.argument("technology", StringArgumentType.greedyString())
										.executes(ctx -> test(ctx.getSource(),
												getPlayer(ctx, "player"),
												StringArgumentType.getString(ctx, "technology"),
												getCriterionArgOrEmpty(ctx))))))
				.build();
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.getRoot().addChild(buildCommandTree());
	}

	private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGEST =
			(ctx, builder) -> SharedSuggestionProvider.suggest(
					ctx.getSource().getOnlinePlayerNames(), builder);
	private static final SuggestionProvider<CommandSourceStack> TECH_SUGGEST =
			(ctx, builder) -> SharedSuggestionProvider.suggest(
					TechnologyManager.INSTANCE.getRegistryNames().stream().map(Object::toString), builder);

	private static ServerPlayer getPlayer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(StringArgumentType.getString(ctx, name));
		if (player == null)
			throw PLAYER_NOT_FOUND.create();
		return player;
	}

	private static String getTechArg(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		return StringArgumentType.getString(ctx, "technology");
	}

	// 可选: test 命令可以带 4 个参数 (tech criterion); Brigade 不支持可选参数，正则支持
	private static String getCriterionArgOrEmpty(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		return "";
	}

	private static int reload(CommandSourceStack sender) throws CommandSyntaxException {
		TechnologyManager.player = sender;
		TechnologyManager.INSTANCE.setRegistryAccess(sender.getServer().registryAccess());
		TechnologyManager.INSTANCE.reload(sender.getServer().getWorldPath(
				net.minecraft.world.level.storage.LevelResource.ROOT).toFile());
		PacketDispatcher.sendToAll(new TechnologyInfoMessage(TechnologyManager.INSTANCE.cache));
		sender.sendSuccess(() -> Component.translatable("commands.technology.reload.success"), true);
		TechnologyManager.player = null;
		return 1;
	}

	// 对应旧版 Mode.EVERYTHING — 操作所有科技
	private static int everything(CommandSourceStack sender, ServerPlayer player, ActionType type) throws CommandSyntaxException {
		Set<Technology> set = new LinkedHashSet<>();
		for (Technology tech : TechnologyManager.INSTANCE)
			set.add(tech);
		type.perform(player, set);
		if (set.isEmpty())
			throw new SimpleCommandExceptionType(Component.translatable(
					"commands.technology." + type.name + ".everything.failed", player.getName())).create();
		sender.sendSuccess(() -> Component.translatable(
				"commands.technology." + type.name + ".everything.success", player.getName(), set.size()), true);

		if (type == ActionType.GRANT)
			PacketDispatcher.sendTo(new TechnologyMessage(player, true, set.toArray(new Technology[0])), player);
		return 1;
	}

	// 对应旧版 perform() with Mode — 对指定科技的子树/父链操作
	private static int perform(CommandSourceStack sender, ServerPlayer player, Mode mode,
			Technology tech, String criterion, ActionType type) throws CommandSyntaxException {
		Set<Technology> set = getTechnologies(tech, mode);
		type.perform(player, set);
		if (set.isEmpty())
			throw mode.fail(type, tech.getRegistryName().toString(), player.getName());
		mode.success(sender, type, tech.getRegistryName().toString(), player.getName(), set.size());

		if (type == ActionType.GRANT)
			PacketDispatcher.sendTo(new TechnologyMessage(player, true, set.toArray(new Technology[0])), player);
		return 1;
	}

	// 对应旧版 getTechnologies() — 根据 mode 收集科技集合
	private static Set<Technology> getTechnologies(Technology tech, Mode mode) {
		Set<Technology> set = new LinkedHashSet<>();
		if (mode.parents)
			for (Technology parent = tech.getParent(); parent != null; parent = parent.getParent())
				set.add(parent);

		if (mode.children)
			tech.getChildren(set, false);
		else
			set.add(tech);

		return set;
	}

	private static int test(CommandSourceStack sender, ServerPlayer player, String techId, String criterion) throws CommandSyntaxException {
		Technology tech = findTechnology(techId);

		if (!criterion.isEmpty()) {
			// test criterion
			if (!tech.hasCustomUnlock())
				throw new SimpleCommandExceptionType(Component.translatable(
						"commands.technology.criterionNotFound", tech.getRegistryName().toString(), criterion)).create();

			Boolean progress = TechnologyManager.INSTANCE.getProgress(player, tech).getCriterionProgress(criterion);
			if (progress == null)
				throw new SimpleCommandExceptionType(Component.translatable(
						"commands.technology.criterionNotFound", tech.getRegistryName().toString(), criterion)).create();
			if (!progress)
				throw new SimpleCommandExceptionType(Component.translatable(
						"commands.technology.test.criterion.notDone", player.getName(), tech.getRegistryName().toString(), criterion)).create();
			sender.sendSuccess(() -> Component.translatable("commands.technology.test.criterion.success",
					player.getName(), tech.getRegistryName().toString(), criterion), true);
		} else {
			// test technology
			if (!tech.isResearched(player))
				throw new SimpleCommandExceptionType(Component.translatable(
						"commands.technology.test.technology.notDone", player.getName(), tech.getRegistryName().toString())).create();
			sender.sendSuccess(() -> Component.translatable("commands.technology.test.technology.success",
					player.getName(), tech.getRegistryName().toString()), true);
		}
		return 1;
	}

	private enum ActionType {
		GRANT("grant") {
			@Override
			protected boolean perform(Player player, Technology tech) {
				if (tech.isResearched(player))
					return false;
				tech.setResearched(player, true);
				return true;
			}
		},
		REVOKE("revoke") {
			@Override
			protected boolean perform(Player player, Technology tech) {
				if (!tech.hasProgress(player))
					return false;
				tech.removeResearched(player);
				return true;
			}
		};

		final String name;

		ActionType(String name) {
			this.name = name;
		}

		void perform(Player player, Iterable<Technology> techs) {
			Iterator<Technology> iterator = techs.iterator();
			while (iterator.hasNext())
				if (!perform(player, iterator.next()))
					iterator.remove();
		}

		protected abstract boolean perform(Player player, Technology tech);
	}

	private enum Mode {
		ONLY("only", false, false),
		THROUGH("through", true, true),
		FROM("from", false, true),
		UNTIL("until", true, false);

		final String name;
		final boolean parents;
		final boolean children;

		Mode(String name, boolean parents, boolean children) {
			this.name = name;
			this.parents = parents;
			this.children = children;
		}

		CommandSyntaxException fail(ActionType type, Object... args) {
			return new SimpleCommandExceptionType(Component.translatable(
					"commands.technology." + type.name + "." + this.name + ".failed", args)).create();
		}

		void success(CommandSourceStack sender, ActionType type, Object... args) {
			sender.sendSuccess(() -> Component.translatable(
					"commands.technology." + type.name + "." + this.name + ".success", args), true);
		}
	}

}
