package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantInt64;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.lower.rv64gc.instruction.RVMul;
import org.systemf.compiler.lower.rv64gc.instruction.RVMulWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftLeft;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.MathUtil;

import java.util.ListIterator;
import java.util.Optional;

public enum RVReduceStrength implements RVOptPass {
	INSTANCE;

	@Override
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		return new RVReduceStrengthContext(module).run();
	}

	private static class RVReduceStrengthContext extends InstructionVisitorBase<Optional<Value>> {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private ListIterator<Instruction> iterator;

		private RVReduceStrengthContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (iterator = block.instructions.listIterator(); iterator.hasNext(); ) {
				var inst = iterator.next();
				if (!(inst instanceof Value val)) continue;

				var reduced = inst.accept(this);
				if (reduced.isEmpty()) continue;

				res = true;
				val.replaceAllUsage(reduced.get());
			}
			return res;
		}

		private boolean processFunction(Function function) {
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			var res = module.getFunctions().values().stream().map(this::processFunction)
					.reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(module);
			return res;
		}

		@Override
		protected Optional<Value> defaultValue() {
			return Optional.empty();
		}

		private Optional<Value> handleMul(DummyBinary inst) {
			var y = inst.getY();
			if (!ValueUtil.isConstantInt(y)) return Optional.empty();

			var yVal = ValueUtil.getConstantInt(y);
			if (yVal == 1) return Optional.of(inst.getX());

			var yPow = MathUtil.checkPowerOfTwo(yVal);
			if (yPow == -1) return Optional.empty();

			var shl = new RVShiftLeft(module.getNonConflictName(inst.getName() + "Shl"), inst.getX(),
					ConstantInt64.valueOf(yPow));
			iterator.add(shl);
			return Optional.of(shl);
		}

		@Override
		public Optional<Value> visit(RVMul inst) {
			return handleMul(inst);
		}

		@Override
		public Optional<Value> visit(RVMulWord inst) {
			return handleMul(inst);
		}
	}
}
