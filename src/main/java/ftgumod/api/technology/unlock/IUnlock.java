package ftgumod.api.technology.unlock;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import ftgumod.api.util.JsonContextPublic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;

public interface IUnlock {

	boolean isDisplayed();

	@Nullable
	Ingredient getIcon();

	boolean unlocks(ItemStack stack);

	void unlock(ServerPlayer player);

	void lock(ServerPlayer player);

	interface Factory<T extends IUnlock> {

		T deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology);

	}

}
