package ftgumod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootUtils {

	public static LootTable merge(LootTable base, LootTable inject) {
		var ops = JsonOps.INSTANCE;
		var baseJson = LootTable.DIRECT_CODEC.encodeStart(ops, base).result().orElse(null);
		var injectJson = LootTable.DIRECT_CODEC.encodeStart(ops, inject).result().orElse(null);
		if (baseJson == null || injectJson == null) return base;

		JsonObject obj = baseJson.getAsJsonObject();
		JsonObject injectObj = injectJson.getAsJsonObject();
		JsonArray pools = obj.has("pools") ? obj.getAsJsonArray("pools") : new JsonArray();
		if (injectObj.has("pools"))
			pools.addAll(injectObj.getAsJsonArray("pools"));
		if (!obj.has("pools"))
			obj.add("pools", pools);

		return LootTable.DIRECT_CODEC.parse(ops, obj).result().orElse(base);
	}

}
