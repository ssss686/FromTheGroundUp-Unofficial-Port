package com.fuxingcheng.fromthegroundup.api.util;

import net.minecraft.resources.ResourceLocation;

public class JsonContextPublic {

	private final String modId;

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

}
