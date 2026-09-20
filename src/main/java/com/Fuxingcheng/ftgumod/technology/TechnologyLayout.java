package com.Fuxingcheng.ftgumod.technology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.Fuxingcheng.ftgumod.api.technology.ITechnology;
import net.minecraft.advancements.DisplayInfo;

/**
 * 科技树的自动排版。
 * <p>
 * display 里的 x / y 现在可以省掉：没写的科技在这里自动算出坐标，用的是原版进度树那一套
 * （Reingold–Tilford，见 vanilla 的 net.minecraft.advancements.TreeNodePosition）。
 * 两个都写了的科技一律当固定点，一个像素都不会被挪动，所以已有的 json 不需要改。
 * <p>
 * 按“页”计算：一页 = 一个根科技加它的全部后代（碰到别的根就停），和 GuiResearchBook 里
 * getChildren(tree, true) 的取法一致。服务端和客户端各算各的，但两边拿到的是同一份原始
 * json，所以只要遍历顺序确定结果就一致 —— 下面所有遍历都按注册名排过序（Technology.children
 * 是个 HashSet，顺序不定，不能直接用）。
 */
public final class TechnologyLayout {

	private TechnologyLayout() {
	}

	/** 子节点、根的顺序都按注册名来，保证两端算出同一份坐标 */
	private static final Comparator<Technology> BY_NAME = Comparator.comparing(Technology::getRegistryName);

	/** 让位次数上限，纯属防御：正常数据碰不到 */
	private static final int MAX_SHIFT = 4096;

	/**
	 * 自动排版的步长：一格就是一格。
	 * <p>
	 * 书里的格子照着原版进度树做成了 28（横）×27（纵）像素，图标框同样是 26×26、和进度页的
	 * widget 一个尺寸，连接线又是一个图标正中间连到另一个正中间（GuiResearchBook 先画线后画
	 * 图标），所以相邻两格之间正好露出 2 像素 / 1 像素的线，和进度页一模一样。
	 * <p>
	 * 手写的老 json 里 x 都是 2、4、6、8，那是按老的 24 像素格子排的，在书上会比自动排出来的
	 * 宽松一倍；那些坐标是写死的，这里不动它们。
	 */
	private static final float STEP = 1.0F;

	/** 全量排版：所有科技都算数（加载时用） */
	public static void apply() {
		apply(technology -> true);
	}

	/** 每一页都按 visible 过滤之后重排 */
	public static void apply(Predicate<Technology> visible) {
		List<Technology> roots = new ArrayList<>();
		for (Technology technology : TechnologyManager.INSTANCE.getRoots())
			roots.add(technology);
		roots.sort(BY_NAME);

		for (Technology root : roots)
			layoutPage(root, visible);
	}

	/**
	 * 单独一页重排，只算 visible 认下来的科技 —— 研究之书每次打开都调这个。
	 * <p>
	 * 为什么非过滤不可：Reingold–Tilford 把父科技摆在它所有孩子的正中间。看不见的科技要是也算一份，
	 * 父科技就会停在"画得出来的孩子"和"画不出来的那个"中间那一行 —— 半格；而画得出来的孩子在自己
	 * 的整格上，两者错开半格，26 像素的框都叠上了，连线也只能先横出去再拐个弯。过滤掉之后这一层
	 * 只剩一个孩子，父科技就落在孩子那一行，两框之间那两像素里直接出一条直线；等它完成、重新进到
	 * 排版里，父科技再挪回中间，连线自然变成多分枝。
	 * <p>
	 * 横向同理：x 是"看得见的层数"，所以看不见的那层被跳过时，它下面画得出来的科技会补上那一列，
	 * 不会在中间留一条空列。
	 */
	public static void apply(Technology root, Predicate<Technology> visible) {
		layoutPage(root, visible);
	}

	private static void layoutPage(Technology root, Predicate<Technology> visible) {
		Node tree = new Node(root, null, null, 1, 0);
		tree.buildChildren(visible);

		// 整页都写死了坐标：原样保留，一个字都不动
		if (!hasAuto(tree))
			return;

		// 先纯自动排一遍，拿到每个节点相对于树根的坐标
		tree.firstWalk();
		float min = tree.secondWalk(0.0F, 0, tree.y);
		if (min < 0.0F)
			tree.thirdWalk(-min);

		List<Node> nodes = new ArrayList<>();
		tree.collect(nodes);

		// 固定点先占位
		Set<Long> occupied = new HashSet<>();
		for (Node node : nodes)
			if (node.fixed())
				occupied.add(cell(Math.round(node.fixedX()), (int) node.fixedY()));

		// 自动节点按“最近的固定祖先”分组（没有固定祖先的归到树根那一组），整组一起平移
		Map<Node, List<Node>> groups = new LinkedHashMap<>();
		for (Node node : nodes) {
			if (node.fixed())
				continue;
			groups.computeIfAbsent(node.anchor(), key -> new ArrayList<>()).add(node);
		}

		for (Map.Entry<Node, List<Node>> group : groups.entrySet()) {
			Node anchor = group.getKey();
			// 把这一组从“树根坐标系”平移到锚点写死的坐标上；没锚点就是树根坐标系，即原版行为
			float dx = anchor == null ? 0.0F : anchor.fixedX() - anchor.x * STEP;
			float dy = anchor == null ? 0.0F : anchor.fixedY() - anchor.y * STEP;

			int shift = 0;
			while (shift < MAX_SHIFT && collides(occupied, group.getValue(), dx, dy, shift))
				shift++;

			for (Node node : group.getValue()) {
				long x = Math.round(node.x * STEP + dx);
				float y = node.y * STEP + dy + shift;
				node.tech.getDisplayInfo().setLocation((float) x, y);
				occupied.add(cell(x, (int) y));
			}
		}
	}

	private static boolean hasAuto(Node node) {
		if (!node.fixed())
			return true;
		for (Node child : node.children)
			if (hasAuto(child))
				return true;
		return false;
	}

	private static boolean collides(Set<Long> occupied, List<Node> group, float dx, float dy, int shift) {
		for (Node node : group)
			if (occupied.contains(cell(Math.round(node.x * STEP + dx), (int) (node.y * STEP + dy + shift))))
				return true;
		return false;
	}

	private static long cell(long x, int y) {
		return (x << 32) | (y & 0xFFFFFFFFL);
	}

	private static final class Node {

		private final Technology tech;
		@Nullable
		private final Node parent;
		@Nullable
		private final Node previousSibling;
		private final int childIndex;
		private final List<Node> children = new ArrayList<>();
		private Node ancestor;
		@Nullable
		private Node thread;
		private int x;
		private float y;
		private float mod;
		private float change;
		private float shift;

		Node(Technology tech, @Nullable Node parent, @Nullable Node previousSibling, int childIndex, int x) {
			this.tech = tech;
			this.parent = parent;
			this.previousSibling = previousSibling;
			this.childIndex = childIndex;
			this.ancestor = this;
			this.x = x;
			this.y = -1.0F;
		}

		boolean fixed() {
			return tech.isPositionFixed();
		}

		float fixedX() {
			return tech.getDisplayInfo().getX();
		}

		float fixedY() {
			return tech.getDisplayInfo().getY();
		}

		/** 最近的写了坐标的祖先，没有就 null */
		@Nullable
		Node anchor() {
			for (Node node = parent; node != null; node = node.parent)
				if (node.fixed())
					return node;
			return null;
		}

		void buildChildren(Predicate<Technology> visible) {
			List<Technology> kids = new ArrayList<>();
			gatherVisible(tech, visible, kids);
			kids.sort(BY_NAME);

			for (Technology child : kids) {
				Node node = new Node(child, this, children.isEmpty() ? null : children.get(children.size() - 1),
						children.size() + 1, x + 1);
				children.add(node);
				node.buildChildren(visible);
			}
		}

		/**
		 * 画得出来的孩子。中间夹着几层画不出来的（隐藏且没进度、还没解锁的）就直接穿过去，挂到最近的
		 * 可见祖先上 —— 和 GuiResearchBook.visibleAncestor 给连线找父节点是同一个取法，这样"进到排版里的"
		 * 和"画出来的"永远是同一批科技，不会有谁带着旧坐标偷偷留在那儿。
		 */
		private static void gatherVisible(Technology parent, Predicate<Technology> visible, List<Technology> out) {
			for (ITechnology child : parent.getChildren()) {
				if (!(child instanceof Technology))
					continue;
				Technology technology = (Technology) child;
				// 别的页面的根：它连同它下面那一支都不归这一页管
				if (technology.isRoot() || !TechnologyManager.INSTANCE.contains(child.getRegistryName()))
					continue;
				if (visible.test(technology))
					out.add(technology);
				else
					gatherVisible(technology, visible, out);
			}
		}

		void collect(List<Node> out) {
			out.add(this);
			for (Node child : children)
				child.collect(out);
		}

		private void firstWalk() {
			if (children.isEmpty()) {
				y = previousSibling == null ? 0.0F : previousSibling.y + 1.0F;
			} else {
				Node previous = null;

				for (Node child : children) {
					child.firstWalk();
					previous = child.apportion(previous == null ? child : previous);
				}

				executeShifts();
				float middle = (children.get(0).y + children.get(children.size() - 1).y) / 2.0F;

				if (previousSibling == null) {
					y = middle;
				} else {
					y = previousSibling.y + 1.0F;
					mod = y - middle;
				}
			}
		}

		@Nullable
		private Node previousOrThread() {
			if (thread != null)
				return thread;
			return children.isEmpty() ? null : children.get(0);
		}

		@Nullable
		private Node nextOrThread() {
			if (thread != null)
				return thread;
			return children.isEmpty() ? null : children.get(children.size() - 1);
		}

		private Node apportion(Node defaultAncestor) {
			if (previousSibling == null)
				return defaultAncestor;

			Node left = previousSibling;
			Node right = this;
			Node leftmost = parent.children.get(0);
			Node rightWalk = this;
			float f = mod;
			float f1 = mod;
			float f2 = left.mod;
			float f3;

			for (f3 = leftmost.mod; left.nextOrThread() != null && right.previousOrThread() != null;
					f1 += rightWalk.mod) {
				left = left.nextOrThread();
				right = right.previousOrThread();
				leftmost = leftmost.previousOrThread();
				rightWalk = rightWalk.nextOrThread();
				rightWalk.ancestor = this;

				float overlap = left.y + f2 - (right.y + f) + 1.0F;
				if (overlap > 0.0F) {
					left.getAncestor(this, defaultAncestor).moveSubtree(this, overlap);
					f += overlap;
					f1 += overlap;
				}

				f2 += left.mod;
				f += right.mod;
				f3 += leftmost.mod;
			}

			if (left.nextOrThread() != null && rightWalk.nextOrThread() == null) {
				rightWalk.thread = left.nextOrThread();
				rightWalk.mod += f2 - f1;
			} else {
				if (right.previousOrThread() != null && leftmost.previousOrThread() == null) {
					leftmost.thread = right.previousOrThread();
					leftmost.mod += f - f3;
				}

				defaultAncestor = this;
			}

			return defaultAncestor;
		}

		private void moveSubtree(Node node, float amount) {
			float indexDelta = node.childIndex - childIndex;

			if (indexDelta != 0.0F) {
				node.change -= amount / indexDelta;
				change += amount / indexDelta;
			}

			node.shift += amount;
			node.y += amount;
			node.mod += amount;
		}

		private Node getAncestor(Node node, Node defaultAncestor) {
			return ancestor != null && node.parent.children.contains(ancestor) ? ancestor : defaultAncestor;
		}

		private void executeShifts() {
			float offset = 0.0F;
			float total = 0.0F;

			for (int i = children.size() - 1; i >= 0; i--) {
				Node child = children.get(i);
				child.y += offset;
				child.mod += offset;
				total += child.change;
				offset += child.shift + total;
			}
		}

		private float secondWalk(float modSum, int depth, float minimum) {
			y += modSum;
			x = depth;
			if (y < minimum)
				minimum = y;

			for (Node child : children)
				minimum = child.secondWalk(modSum + mod, depth + 1, minimum);

			return minimum;
		}

		private void thirdWalk(float offset) {
			y += offset;
			for (Node child : children)
				child.thirdWalk(offset);
		}

	}

}
