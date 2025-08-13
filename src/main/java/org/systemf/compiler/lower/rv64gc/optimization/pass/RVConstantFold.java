package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantInt32;
import org.systemf.compiler.ir.value.constant.ConstantInt64;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.lower.rv64gc.instruction.*;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.query.QueryManager;

import java.util.Optional;
import java.util.function.BiFunction;

public enum RVConstantFold implements RVOptPass {
	INSTANCE;

	private static final RVConstantFolder folder = new RVConstantFolder();

	private boolean processFunction(Function function) {
		var res = function.allInstructions().filter(inst -> inst instanceof Value).map(inst -> {
			var folded = inst.accept(folder);
			if (folded.isEmpty()) return false;
			((Value) inst).replaceAllUsage(folded.get());
			return true;
		}).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) {
			var query = QueryManager.getInstance();
			query.invalidateAllAttributes(module);
			query.invalidateAllAttributes(rvModule);
		}
		return res;
	}

	private static class RVConstantFolder extends InstructionVisitorBase<Optional<Constant>> {
		@Override
		protected Optional<Constant> defaultValue() {
			return Optional.empty();
		}

		private Optional<Constant> handleBinary64(DummyBinary inst, BiFunction<Long, Long, Long> func) {
			var x = inst.getX();
			var y = inst.getY();
			if (ValueUtil.isConstantInt(x) && ValueUtil.isConstantInt(y)) return Optional.of(
					ConstantInt64.valueOf(func.apply(ValueUtil.getConstantInt(x), ValueUtil.getConstantInt(y))));
			return Optional.empty();
		}

		private Optional<Constant> handleBinary32(DummyBinary inst, BiFunction<Long, Long, Long> func) {
			var x = inst.getX();
			var y = inst.getY();
			if (ValueUtil.isConstantInt(x) && ValueUtil.isConstantInt(y)) return Optional.of(ConstantInt32.valueOf(
					func.apply(ValueUtil.getConstantInt(x), ValueUtil.getConstantInt(y)).intValue()));
			return Optional.empty();
		}

		@Override
		public Optional<Constant> visit(RVAdd inst) {
			return handleBinary64(inst, Long::sum);
		}

		@Override
		public Optional<Constant> visit(RVAddWord inst) {
			return handleBinary32(inst, Long::sum);
		}

		@Override
		public Optional<Constant> visit(RVMul inst) {
			return handleBinary64(inst, (x, y) -> x * y);
		}

		@Override
		public Optional<Constant> visit(RVMulWord inst) {
			return handleBinary32(inst, (x, y) -> x * y);
		}

		@Override
		public Optional<Constant> visit(RVShiftLeft inst) {
			return handleBinary64(inst, (x, y) -> x << y);
		}

		@Override
		public Optional<Constant> visit(RVShiftLeftWord inst) {
			return handleBinary32(inst, (x, y) -> x << y);
		}
	}
}
