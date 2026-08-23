package com.Fuxingcheng.ftgumod.api.technology.recipe;

import java.util.List;

import com.google.gson.JsonObject;
import com.Fuxingcheng.ftgumod.api.technology.ITechnology;
import com.Fuxingcheng.ftgumod.api.util.BlockSerializable;
import com.Fuxingcheng.ftgumod.api.util.JsonContextPublic;
import net.minecraft.resources.ResourceLocation;

public interface IResearchRecipe {

	/**
	 * @param block     The new block that has been inspected
	 * @param inspected The already inspected block listed on the magnifying glass
	 * @return If the newly inspected block will help with researching this
	 */
	boolean inspect(BlockSerializable block, List<BlockSerializable> inspected);

	IPuzzle createInstance();

	ITechnology getTechnology();

	void setTechnology(ITechnology tech);

	interface Factory<T extends IResearchRecipe> {

		T deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology);

	}

}
