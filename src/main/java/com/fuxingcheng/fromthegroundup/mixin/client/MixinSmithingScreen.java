package com.fuxingcheng.fromthegroundup.mixin.client;

import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 与 NeoForge 端 EventHandler#isLockedSmithingResult 同一套判定：
 * 三个输入槽有东西、结果槽却空着，而照着配方自己算出来的产物是锁着的 ——
 * 那这个"配方有错"是锁定造成的，不是原版要表达的意思。
 */
@Environment(EnvType.CLIENT)
@Mixin(SmithingScreen.class)
public class MixinSmithingScreen {

	@Inject(method = "hasRecipeError", at = @At("HEAD"), cancellable = true)
	private void ftgumod$hideErrorWhenResultLocked(CallbackInfoReturnable<Boolean> cir) {
		if (isLockedResult((SmithingScreen) (Object) this))
			cir.setReturnValue(false);
	}

	private static boolean isLockedResult(SmithingScreen screen) {
		SmithingMenu menu = screen.getMenu();
		if (!menu.getSlot(SmithingMenu.TEMPLATE_SLOT).hasItem()
				|| !menu.getSlot(SmithingMenu.BASE_SLOT).hasItem()
				|| !menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).hasItem()
				|| menu.getSlot(menu.getResultSlot()).hasItem())
			return false;

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || minecraft.level == null)
			return false;

		SmithingRecipeInput input = new SmithingRecipeInput(
				menu.getSlot(SmithingMenu.TEMPLATE_SLOT).getItem(),
				menu.getSlot(SmithingMenu.BASE_SLOT).getItem(),
				menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).getItem());
		for (RecipeHolder<SmithingRecipe> recipe : minecraft.level.getRecipeManager()
				.getRecipesFor(RecipeType.SMITHING, input, minecraft.level))
			if (TechnologyManager.INSTANCE
					.isLocked(recipe.value().assemble(input, minecraft.level.registryAccess()), player))
				return true;
		return false;
	}
}
