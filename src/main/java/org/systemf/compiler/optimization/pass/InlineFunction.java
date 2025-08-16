package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.analysis.CallGraphAnalysisResult;
import org.systemf.compiler.analysis.FrequencyAnalysisResult;
import org.systemf.compiler.analysis.FunctionRecursionAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.optimization.pass.util.CodeGenHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Depend on: CFGAnalysis, FunctionRecursionAnalysis, FrequencyAnalysis, CallGraphAnalysis
 * <p>
 * Applicable to: IR
 */
public enum InlineFunction implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new InlineFunctionContext(module).run();
	}

	private static class InlineFunctionContext {
		private static final int FREQ_THRESHOLD = 128;
		private static final int SIZE_THRESHOLD = 256;

		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final FunctionRecursionAnalysisResult recursion;
		private final Map<ITracked, ITracked> substitute = new HashMap<>();
		private final CallGraphAnalysisResult callGraph;
		private IRBuilder builder;
		private Function curFunction;
		private CFGAnalysisResult cfg;
		private FrequencyAnalysisResult frequency;
		private BasicBlock retBlock;
		private Phi retPhi;

		public InlineFunctionContext(Module module) {
			this.module = module;
			this.recursion = query.getAttribute(module, FunctionRecursionAnalysisResult.class);
			this.callGraph = query.getAttribute(module, CallGraphAnalysisResult.class);
		}

		private BasicBlock cloneFunction(Function function) {
			var newBlocks = new ArrayList<BasicBlock>();
			for (var block : function.getBlocks()) {
				var newBlock = builder.buildBasicBlock(curFunction, block.getName());
				newBlocks.add(newBlock);
				CodeGenHelper.cloneBlock(builder, block, newBlock, substitute);
			}

			for (var block : newBlocks) CodeGenHelper.replaceAll(block, substitute);

			for (var block : newBlocks) {
				var term = block.getTerminator();
				if (term instanceof Ret ret) {
					block.instructions.removeLast();
					var value = ret.getReturnValue();
					ret.unregister();
					builder.attachToBlockTail(block);
					builder.buildBr(retBlock);
					retPhi.addIncoming(block, value);
				} else if (term instanceof RetVoid retVoid) {
					block.instructions.removeLast();
					retVoid.unregister();
					builder.attachToBlockTail(block);
					builder.buildBr(retBlock);
				}
			}
			return (BasicBlock) substitute.get(function.getEntryBlock());
		}

		private boolean processBlock(BasicBlock block) {
			var curFreq = frequency.frequency(block);
			for (var iter = block.instructions.listIterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (!(inst instanceof AbstractCall call)) continue;
				var target = call.getFunction();
				if (!(target instanceof Function function)) continue;
				if (recursion.recursive(function)) continue;
				if (function.allInstructions().count() > SIZE_THRESHOLD) continue;
				var allConst = Arrays.stream(call.getArgs()).allMatch(arg -> arg instanceof Constant);
				var callOnce = callGraph.callSiteCnt(target) == 1;
				if (curFreq < FREQ_THRESHOLD && !(allConst || callOnce)) continue;

				iter.remove();
				substitute.clear();
				builder.setPosition(iter);
				var formal = function.getFormalArgs();
				var actual = call.getArgs();
				for (int i = 0; i < formal.length; ++i) {
					var param = formal[i];
					var paramType = param.getType();
					var arg = actual[i];
					if (paramType.equals(arg.getType())) substitute.put(param, arg);
					else substitute.put(param, builder.buildPtrCast(arg, paramType, "inlineCast"));
				}

				retBlock = builder.buildBasicBlock(curFunction, "inlineMerge");
				builder.attachToBlockTail(retBlock);
				if (call instanceof Call valuedCall) {
					retPhi = builder.buildPhi(valuedCall.getType(), "inlineRet");
					valuedCall.replaceAllUsage(retPhi);
				}

				var inlineEntry = cloneFunction(function);
				builder.setPosition(iter);
				builder.buildBr(inlineEntry);

				while (iter.hasNext()) {
					var toMove = iter.next();
					iter.remove();
					retBlock.insertInstruction(toMove);
				}

				cfg.successors(block).stream()
						.flatMap(succ -> succ.instructions.stream().takeWhile(phi -> phi instanceof Phi))
						.map(phi -> (Phi) phi).forEach(phi -> phi.replaceAll(block, retBlock));

				inst.unregister();
				return true;
			}
			return false;
		}

		private boolean processFunction(Function function) {
			this.curFunction = function;
			this.cfg = query.getAttribute(function, CFGAnalysisResult.class);
			this.frequency = query.getAttribute(function, FrequencyAnalysisResult.class);
			var blocks = new ArrayList<>(function.getBlocks());
			var res = blocks.stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
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
