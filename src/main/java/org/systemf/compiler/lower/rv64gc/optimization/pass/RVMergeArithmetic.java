package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.lower.rv64gc.instruction.*;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Pair;
import org.systemf.compiler.util.SaturationArithmetic;

import java.util.Optional;

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
		private IRBuilder builder;

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
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = module.getFunctions().values().stream().map(this::processFunction)
						.reduce(false, (a, b) -> a || b);
				if (res) query.invalidateAllAttributes(module);
				return res;
			}
		}

		@Override
		protected Boolean defaultValue() {
			return false;
		}

		@Override
		public Boolean visit(RVAdd inst) {
			return MergeHelper.mergeIntBinary(builder, inst, SaturationArithmetic::checkedAdd);
		}

		@Override
		public Boolean visit(RVAddWord inst) {
			return MergeHelper.mergeIntBinary(builder, inst, SaturationArithmetic::checkedAdd);
		}

		@Override
		public Boolean visit(RVMul inst) {
			return MergeHelper.mergeIntBinary(builder, inst, SaturationArithmetic::checkedMul);
		}

		@Override
		public Boolean visit(RVMulWord inst) {
			return MergeHelper.mergeIntBinary(builder, inst, SaturationArithmetic::checkedMul);
		}

		@Override
		public Boolean visit(RVShiftLeft inst) {
			return MergeHelper.mergeIntBinary(builder, inst, SaturationArithmetic::checkedAdd);
		}

		@Override
		public Boolean visit(RVShiftLeftWord inst) {
			return MergeHelper.mergeIntBinary(builder, inst, SaturationArithmetic::checkedAdd);
		}

		private Optional<Pair<Value, Long>> extractOffset(Value ptr) {
			if (!(ptr instanceof RVAdd ptrAdd)) return Optional.empty();
			var addOffset = ptrAdd.getY();
			if (!ValueUtil.isConstantInt(addOffset)) return Optional.empty();
			var addOffsetVal = ValueUtil.getConstantInt(addOffset);
			return Optional.of(Pair.of(ptrAdd.getX(), addOffsetVal));
		}

		private Optional<Pair<Value, Long>> extractAndApplyOffset(Value ptr, long oldOffset) {
			return extractOffset(ptr).flatMap(extracted -> SaturationArithmetic.checkedAdd(oldOffset, extracted.right())
					.map(newOffset -> Pair.of(extracted.left(), newOffset)));
		}

		@Override
		public Boolean visit(RVLoad inst) {
			return extractAndApplyOffset(inst.getPointer(), inst.offset).map(applied -> {
				inst.setPointer(applied.left());
				inst.offset = applied.right();
				return true;
			}).orElse(false);
		}

		@Override
		public Boolean visit(RVStore inst) {
			return extractAndApplyOffset(inst.getDest(), inst.offset).map(applied -> {
				inst.setDest(applied.left());
				inst.offset = applied.right();
				return true;
			}).orElse(false);
		}
	}
}
