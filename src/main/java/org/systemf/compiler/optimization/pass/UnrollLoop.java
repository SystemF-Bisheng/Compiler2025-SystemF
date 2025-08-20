package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.SimpleLoopAnalysisResult;
import org.systemf.compiler.analysis.SimpleLoopAnalysisResult.SimpleLoop;
import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.optimization.pass.util.CodeGenHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public enum UnrollLoop implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new UnrollLoopContext(module).run();
	}

	private static class UnrollLoopContext {
		private static final int UNROLL_THRESHOLD = 64;
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private IRBuilder builder;
		private Function curFunction;

		private BasicBlock loopHead;
		private BasicBlock loopBody;
		private BasicBlock loopMerge;
		private BasicBlock lastBody;

		public UnrollLoopContext(Module module) {
			this.module = module;
		}

		private void handleNewHeadPhi(BasicBlock newHead, Map<ITracked, ITracked> substitute,
				Map<ITracked, ITracked> newSubstitute, Map<Value, Phi> mergePhis) {
			mergePhis.forEach(
					(oldValue, mergePhi) -> mergePhi.addIncoming(newHead, (Value) newSubstitute.get(oldValue)));
			loopHead.allPhis().forEach(oldPhi -> {
				var newPhi = (Phi) newSubstitute.get(oldPhi);
				var oldValue = oldPhi.getIncoming(loopBody);
				var newValue = (Value) substitute.getOrDefault(oldValue, oldValue);
				newPhi.replaceAllUsage(newValue);
				newSubstitute.put(oldPhi, newValue);
			});
			newHead.instructions.removeIf(inst -> inst instanceof Phi);
		}

		private void handleOldHeadPhi(Map<ITracked, ITracked> substitute) {
			loopHead.allPhis().forEach(oldPhi -> {
				var oldValue = oldPhi.getIncoming(loopBody);
				var newValue = (Value) substitute.getOrDefault(oldValue, oldValue);
				oldPhi.removeIncoming(loopBody);
				oldPhi.addIncoming(lastBody, newValue);
			});
		}

		private void replaceByMergePhi(Set<BasicBlock> loopBlocks, Map<Value, Phi> mergePhis) {
			curFunction.getBlocks().stream().filter(block -> !loopBlocks.contains(block))
					.forEach(block -> CodeGenHelper.replaceAll(block, mergePhis));
			loopHead.allPhis().forEach(headPhi -> {
				var snapshot = new LinkedHashMap<>(headPhi.getIncoming());
				snapshot.forEach((block, oldValue) -> {
					if (block == loopBody) return;
					if (!mergePhis.containsKey(oldValue)) return;
					headPhi.replaceIncoming(block, mergePhis.get(oldValue));
				});
			});
		}

		private Map<Value, Phi> buildNewMerge(SimpleLoop loop) {
			var oldMerge = loop.merge();
			loopMerge = builder.buildBasicBlock(curFunction, loopHead.getName() + "Merge");
			loopHead.getTerminator().replaceAll(oldMerge, loopMerge);
			oldMerge.allPhis().forEach(oldMergePhi -> {
				var val = oldMergePhi.getIncoming(loopHead);
				oldMergePhi.removeIncoming(loopHead);
				oldMergePhi.addIncoming(loopMerge, val);
			});

			builder.attachToBlockTail(loopMerge);
			var mergePhis = new HashMap<Value, Phi>();
			loopHead.instructions.stream().filter(inst -> inst instanceof Value).map(inst -> (Value) inst)
					.forEach(oldVal -> {
						var newPhi = builder.buildPhi(oldVal.getType(), ((INamed) oldVal).getName());
						mergePhis.put(oldVal, newPhi);
					});
			replaceByMergePhi(Set.of(loopHead, loopBody), mergePhis);
			mergePhis.forEach((oldVal, newPhi) -> newPhi.addIncoming(loopHead, oldVal));
			builder.buildBr(oldMerge);
			return mergePhis;
		}

		private void unroll(SimpleLoop loop, long multiplier) {
			var substitute = new HashMap<ITracked, ITracked>();
			loopHead = loop.head();
			loopBody = loop.body();
			var mergePhis = buildNewMerge(loop);

			--multiplier;
			lastBody = loopBody;
			for (; multiplier > 0; --multiplier) {
				var newSubstitute = new HashMap<ITracked, ITracked>();
				var newHead = builder.buildBasicBlock(curFunction, loopHead.getName());
				CodeGenHelper.cloneBlock(builder, loopHead, newHead, newSubstitute);
				var newBody = builder.buildBasicBlock(curFunction, loopBody.getName());
				CodeGenHelper.cloneBlock(builder, loopBody, newBody, newSubstitute);
				CodeGenHelper.replaceAll(newHead, newSubstitute);
				CodeGenHelper.replaceAll(newBody, newSubstitute);

				handleNewHeadPhi(newHead, substitute, newSubstitute, mergePhis);

				substitute = newSubstitute;
				((Br) lastBody.getTerminator()).setTarget(newHead);
				lastBody = newBody;
			}
			((Br) lastBody.getTerminator()).setTarget(loopHead);
			handleOldHeadPhi(substitute);
		}

		private boolean processLoop(SimpleLoop loop) {
			var loopSize = loop.head().instructions.size() + loop.body().instructions.size();
			if (loopSize >= UNROLL_THRESHOLD) return false;
			var multiplier = 4;
			while (loopSize * multiplier < UNROLL_THRESHOLD) multiplier *= 2;

			unroll(loop, multiplier);
			return true;
		}

		private boolean processFunction(Function function) {
			curFunction = function;
			var loops = query.getAttribute(function, SimpleLoopAnalysisResult.class);
			var res = loops.loops().stream().map(this::processLoop).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = module.getFunctions().values().stream().map(this::processFunction)
						.reduce(false, (a, b) -> a || b);
				if (res) query.invalidateAllAttributes(module);
				return res;
			}
		}
	}
}
