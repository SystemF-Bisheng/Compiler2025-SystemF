package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Sub;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.SaturationArithmetic;

import java.util.Comparator;
import java.util.function.BiFunction;

public enum MergeArithmetic implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MergeArithmeticContext(module).run();
	}

	private static class MergeArithmeticContext extends InstructionVisitorBase<Boolean> {
		private final Module module;
		private IRBuilder builder;

		public MergeArithmeticContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			return block.instructions.stream().map(inst -> inst.accept(this)).reduce(false, (a, b) -> a || b);
		}

		private boolean processFunction(Function function) {
			var query = QueryManager.getInstance();
			var domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			var res = function.getBlocks().stream().sorted(Comparator.comparingInt(domTree::getDfn))
					.map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = module.getFunctions().values().stream().map(this::processFunction)
						.reduce(false, (a, b) -> a || b);
				if (res) QueryManager.getInstance().invalidateAllAttributes(module);
				return res;
			}
		}

		@Override
		protected Boolean defaultValue() {
			return false;
		}

		private boolean handleBinary(DummyBinary inst, BiFunction<Long, Long, Long> func) {
			var x = inst.getX();
			if (!(inst.getY() instanceof ConstantInt selfY)) return false;
			if (inst.getClass() != x.getClass()) return false;
			var binaryX = (DummyBinary) x;
			if (!(binaryX.getY() instanceof ConstantInt otherY)) return false;
			var newValue = func.apply(otherY.value, selfY.value);
			if (SaturationArithmetic.isOverflow(newValue)) return false;
			inst.setX(binaryX.getX());
			inst.setY(builder.buildConstantInt(newValue));
			return true;
		}

		@Override
		public Boolean visit(Add inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(Mul inst) {
			return handleBinary(inst, (x, y) -> x * y);
		}

		@Override
		public Boolean visit(Sub inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(And inst) {
			return handleBinary(inst, (x, y) -> x & y);
		}

		@Override
		public Boolean visit(Or inst) {
			return handleBinary(inst, (x, y) -> x | y);
		}

		@Override
		public Boolean visit(Xor inst) {
			return handleBinary(inst, (x, y) -> x ^ y);
		}

		@Override
		public Boolean visit(Shl inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(LShr inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(AShr inst) {
			return handleBinary(inst, Long::sum);
		}
	}
}
