package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Pair;
import org.systemf.compiler.util.PathUnionFindNode;
import org.systemf.compiler.util.Tree;

import java.util.*;

public enum DominanceAnalysis implements AttributeProvider<Function, DominanceAnalysisResult> {
	INSTANCE;

	@Override
	public DominanceAnalysisResult getAttribute(Function entity) {
		var cfg = QueryManager.getInstance().getAttribute(entity, CFGAnalysisResult.class);
		return new DominanceAnalysisContext(entity.getEntryBlock(), cfg).analysis();
	}

	private static class DominanceAnalysisContext {
		private final Map<BasicBlock, Integer> dfn = new HashMap<>();
		private final Map<Integer, BasicBlock> dfnInv = new HashMap<>();
		private final Map<BasicBlock, BasicBlock> father = new HashMap<>();
		private final Map<BasicBlock, BasicBlock> semiDom = new HashMap<>();
		private final Map<BasicBlock, Set<BasicBlock>> semiDomInv = new HashMap<>();
		private final Map<BasicBlock, PathUnionFindNode<DominanceInfo>> unionFind = new HashMap<>();
		private final Map<BasicBlock, BasicBlock> iDomInfo = new HashMap<>();
		private final BasicBlock entry;
		private final Map<BasicBlock, Set<BasicBlock>> successors;
		private final Map<BasicBlock, Set<BasicBlock>> predecessors;
		private int dfnCnt = 0;

		public DominanceAnalysisContext(BasicBlock entry, CFGAnalysisResult cfg) {
			this.entry = entry;
			this.successors = cfg.successors();
			this.predecessors = cfg.predecessors();
		}

		private void dfs(BasicBlock cur) {
			var curDfn = dfnCnt++;
			dfn.put(cur, curDfn);
			dfnInv.put(curDfn, cur);
			unionFind.put(cur, new PathUnionFindNode<>(null));
			for (var nxt : successors.get(cur)) {
				if (dfn.containsKey(nxt)) continue;
				father.put(nxt, cur);
				dfs(nxt);
			}
		}

		private void handleImmediateDom(BasicBlock cur) {
			var sDomChildren = semiDomInv.get(cur);
			if (sDomChildren == null) return;
			for (var nxt : sDomChildren) {
				var nxtNode = unionFind.get(nxt);
				nxtNode.find(this::mergeInfo);
				iDomInfo.put(nxt, nxtNode.data.minBlock());
			}
		}

		private void calcSemiDom(int curDfn) {
			var cur = dfnInv.get(curDfn);
			int curSemiDom = dfnCnt;
			for (var pred : predecessors.get(cur)) {
				var predDfn = dfn.get(pred);
				if (predDfn == curDfn) continue;
				if (predDfn < curDfn) curSemiDom = Math.min(curSemiDom, predDfn);
				else {
					var predNode = unionFind.get(pred);
					predNode.find(this::mergeInfo);
					curSemiDom = Math.min(curSemiDom, predNode.data.minSemiDom);
				}
			}
			var curNode = unionFind.get(cur);
			var curSemiDomBlock = dfnInv.get(curSemiDom);
			semiDomInv.computeIfAbsent(curSemiDomBlock, _ -> new HashSet<>()).add(cur);
			semiDom.put(cur, curSemiDomBlock);
			curNode.data = new DominanceInfo(curSemiDom, cur);
			handleImmediateDom(cur);
			curNode.union(unionFind.get(father.get(cur)), this::mergeInfo);
		}

		private void calcImmediateDom(int curDfn, List<Pair<BasicBlock, BasicBlock>> out) {
			var cur = dfnInv.get(curDfn);
			var iDom = semiDom.get(cur);
			var iInfo = iDomInfo.get(cur);
			if (iInfo != cur) iDom = iDomInfo.get(iInfo);
			iDomInfo.put(cur, iDom);
			out.add(Pair.of(cur, iDom));
		}

		private void collectDominanceFrontier(Tree<BasicBlock> dominanceTree, BasicBlock block,
				Map<BasicBlock, Set<BasicBlock>> out) {
			var iDom = dominanceTree.getParent(block);
			var preds = predecessors.get(block);
			if (preds.size() < 2) return;
			for (var pred : preds) {
				var pDom = pred;
				while (pDom != iDom) {
					out.get(pDom).add(block);
					pDom = dominanceTree.getParent(pDom);
				}
			}
		}

		public DominanceAnalysisResult analysis() {
			dfs(entry);
			for (var dfn = dfnCnt - 1; dfn > 0; --dfn) calcSemiDom(dfn);
			handleImmediateDom(entry);
			var res = new ArrayList<Pair<BasicBlock, BasicBlock>>();
			res.add(Pair.of(entry, null));
			for (var dfn = 1; dfn < dfnCnt; ++dfn) calcImmediateDom(dfn, res);
			var tree = new Tree<>(res);

			var df = new HashMap<BasicBlock, Set<BasicBlock>>();
			for (var block : dfn.keySet()) df.put(block, new HashSet<>());
			for (var block : dfn.keySet()) collectDominanceFrontier(tree, block, df);

			return new DominanceAnalysisResult(new Tree<>(res), df);
		}

		private DominanceInfo mergeInfo(DominanceInfo to, DominanceInfo from) {
			if (from.minSemiDom < to.minSemiDom) return from;
			return to;
		}

		private record DominanceInfo(int minSemiDom, BasicBlock minBlock) {
		}
	}
}