package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantInt64;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.lower.rv64gc.instruction.*;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Pair;

import java.util.Optional;
import java.util.function.BiFunction;

public enum RVMergeArithmetic implements RVOptPass {
	INSTANCE;

	@Override
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		return new RVMergeArithmeticContext(module).run();
	}

	private static class RVMergeArithmeticContext extends InstructionVisitorBase<Boolean> {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;

		private RVMergeArithmeticContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			return block.instructions.stream().map(inst -> inst.accept(this)).reduce(false, (a, b) -> a || b);
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
		protected Boolean defaultValue() {
			return false;
		}

		private boolean handleBinary(DummyBinary inst, BiFunction<Long, Long, Long> func) {
			var x = inst.getX();
			var selfY = inst.getY();
			if (!ValueUtil.isConstantInt(selfY)) return false;
			if (inst.getClass() != x.getClass()) return false;
			var binaryX = (DummyBinary) x;
			var otherY = binaryX.getY();
			if (!ValueUtil.isConstantInt(otherY)) return false;
			var newValue = func.apply(ValueUtil.getConstantInt(otherY), ValueUtil.getConstantInt(selfY));
			inst.setX(binaryX.getX());
			inst.setY(ConstantInt64.valueOf(newValue));
			return true;
		}

		@Override
		public Boolean visit(RVAdd inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(RVAddWord inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(RVMul inst) {
			return handleBinary(inst, (x, y) -> x * y);
		}

		@Override
		public Boolean visit(RVMulWord inst) {
			return handleBinary(inst, (x, y) -> x * y);
		}

		@Override
		public Boolean visit(RVShiftLeft inst) {
			return handleBinary(inst, Long::sum);
		}

		private Optional<Pair<Value, Long>> extractOffset(Value ptr) {
			if (!(ptr instanceof RVAdd ptrAdd)) return Optional.empty();
			var addOffset = ptrAdd.getY();
			if (!ValueUtil.isConstantInt(addOffset)) return Optional.empty();
			var addOffsetVal = ValueUtil.getConstantInt(addOffset);
			return Optional.of(Pair.of(ptrAdd.getX(), addOffsetVal));
		}

		@Override
		public Boolean visit(RVLoad inst) {
			var ptr = inst.getPointer();
			var extract = extractOffset(ptr);
			if (extract.isEmpty()) return false;
			var extracted = extract.get();
			inst.setPointer(extracted.left());
			inst.offset += extracted.right();
			return true;
		}

		@Override
		public Boolean visit(RVStore inst) {
			var ptr = inst.getDest();
			var extract = extractOffset(ptr);
			if (extract.isEmpty()) return false;
			var extracted = extract.get();
			inst.setDest(extracted.left());
			inst.offset += extracted.right();
			return true;
		}
	}
}
