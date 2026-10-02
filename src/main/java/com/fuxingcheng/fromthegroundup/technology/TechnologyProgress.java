package com.fuxingcheng.fromthegroundup.technology;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import net.minecraft.advancements.Criterion;

public class TechnologyProgress {

	// 单机里客户端和服务器共用一个 TechnologyManager，两份线程都会读这份进度
	// （服务器：判据轮询、解锁判断；客户端：收包后的 grant/revoke、渲染进度页），
	// 所以用 ConcurrentHashMap，别用普通 HashMap。
	private final Map<String, Boolean> criteria = new ConcurrentHashMap<>();
	private volatile String[][] requirements = new String[0][];

	public void update(Map<String, Criterion<?>> p_update_1_, String[][] p_update_2_) {
		Set<String> lvt_3_1_ = p_update_1_.keySet();

		// 原来是用迭代器边遍历边 remove，两个线程一起走就会抛 ConcurrentModificationException；
		// 那一抛会顺着 refreshListeners 把整趟重挂监听带走，判据就再也挂不上了。
		this.criteria.keySet().removeIf(key -> !lvt_3_1_.contains(key));

		for (String key : lvt_3_1_)
			this.criteria.putIfAbsent(key, false);

		this.requirements = p_update_2_;
	}

	public Iterable<String> getRemaningCriteria() {
		List<String> lvt_1_1_ = Lists.newArrayList();
		Iterator var2 = this.criteria.entrySet().iterator();

		while (var2.hasNext()) {
			Map.Entry<String, Boolean> lvt_3_1_ = (Map.Entry) var2.next();
			if (!(lvt_3_1_.getValue())) {
				lvt_1_1_.add(lvt_3_1_.getKey());
			}
		}

		return lvt_1_1_;
	}

	public boolean grantCriterion(String p_grantCriterion_1_) {
		// 原来是先 get 再 put，两个线程同时授同一条会各自读到 false、都返回 true；
		// replace 是原子的，语义一样：键在、且当前是 false 才改成 true
		return this.criteria.replace(p_grantCriterion_1_, false, true);
	}

	public Iterable<String> getCompletedCriteria() {
		List<String> lvt_1_1_ = Lists.newArrayList();
		Iterator var2 = this.criteria.entrySet().iterator();

		while (var2.hasNext()) {
			Map.Entry<String, Boolean> lvt_3_1_ = (Map.Entry) var2.next();
			if (lvt_3_1_.getValue()) {
				lvt_1_1_.add(lvt_3_1_.getKey());
			}
		}

		return lvt_1_1_;
	}

	public boolean revokeCriterion(String p_revokeCriterion_1_) {
		// 同上，撤销也走原子的 CAS
		return this.criteria.replace(p_revokeCriterion_1_, true, false);
	}

	public boolean isDone() {
		if (this.requirements.length == 0) {
			return false;
		} else {
			String[][] var1 = this.requirements;
			int var2 = var1.length;

			for (int var3 = 0; var3 < var2; ++var3) {
				String[] lvt_4_1_ = var1[var3];
				boolean lvt_5_1_ = false;
				String[] var6 = lvt_4_1_;
				int var7 = lvt_4_1_.length;

				for (int var8 = 0; var8 < var7; ++var8) {
					String lvt_9_1_ = var6[var8];
					Boolean lvt_10_1_ = this.getCriterionProgress(lvt_9_1_);
					if (lvt_10_1_ != null && lvt_10_1_) {
						lvt_5_1_ = true;
						break;
					}
				}

				if (!lvt_5_1_) {
					return false;
				}
			}

			return true;
		}
	}

	@Nullable
	public Boolean getCriterionProgress(String p_getCriterionProgress_1_) {
		return this.criteria.get(p_getCriterionProgress_1_);
	}

	public boolean hasProgress() {
		Iterator var1 = this.criteria.values().iterator();

		Boolean lvt_2_1_;
		do {
			if (!var1.hasNext()) {
				return false;
			}

			lvt_2_1_ = (Boolean) var1.next();
		} while (!lvt_2_1_);

		return true;
	}

}
