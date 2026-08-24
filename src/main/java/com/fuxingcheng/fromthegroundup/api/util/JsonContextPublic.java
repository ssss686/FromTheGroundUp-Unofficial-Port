package com.fuxingcheng.fromthegroundup.api.util;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import com.fuxingcheng.fromthegroundup.api.FTGUAPI;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class JsonContextPublic {

	private final String modId;
	private final Map<String, ItemPredicate> constants = new HashMap<>();

	public JsonContextPublic(String modId) {
		this.modId = modId;
	}

	public String getModId() {
		return modId;
	}

	public String appendModId(String id) {
		if (id.contains(":"))
			return id;
		return modId + ":" + id;
	}

	@Nullable
	public ItemPredicate getConstant(String name) {
		return constants.get(name);
	}

	public void loadConstants(JsonObject[] jsons) {
		for (JsonObject json : jsons) {
			if (!json.has("ingredient"))
				throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
			constants.put(GsonHelper.getAsString(json, "name"),
					FTGUAPI.stackUtils.getItemPredicate(json.get("ingredient"), this));
		}
	}

}
