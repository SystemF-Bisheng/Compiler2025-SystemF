package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.AShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.LShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Shl;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SDiv;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.MathUtil;

import java.util.ListIterator;

/**
 * Depend on: No
 * <p>
 * Applicable to: IR
 */
public enum ReduceStrength implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new ReduceStrengthContext(module).run();
	}

	private static class ReduceStrengthContext extends InstructionVisitorBase<Boolean> {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private IRBuilder builder;
		private ListIterator<Instruction> iterator;

		public ReduceStrengthContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (iterator = block.instructions.listIterator(); iterator.hasNext(); ) {
				var instruction = iterator.next();
				res |= instruction.accept(this);
			}
			return res;
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

		private boolean checkIdentity(DummyBinary inst, long identity) {
			var y = inst.getY();
			if (!ValueUtil.isConstantInt(y)) return false;
			if (ValueUtil.getConstantInt(y) == identity) {
				inst.replaceAllUsage(inst.getX());
				return true;
			}
			return false;
		}

		@Override
		public Boolean visit(Add inst) {
			return checkIdentity(inst, 0);
		}

		@Override
		public Boolean visit(SDiv inst) {
			return checkIdentity(inst, 1);
		}

		@Override
		public Boolean visit(Shl inst) {
			return checkIdentity(inst, 0);
		}

		@Override
		public Boolean visit(LShr inst) {
			return checkIdentity(inst, 0);
		}

		@Override
		public Boolean visit(AShr inst) {
			return checkIdentity(inst, 0);
		}

		@Override
		public Boolean visit(Mul inst) {
			var y = inst.getY();
			if (!ValueUtil.isConstantInt(y)) return false;
			var yVal = ValueUtil.getConstantInt(y);
			var width = ValueUtil.getWidth(inst);
			if (yVal == 0) {
				inst.replaceAllUsage(builder.buildConstantInt(0, width));
				return true;
			}

			var yAbs = Math.abs(yVal);
			var power = MathUtil.checkPowerOfTwo(yAbs);
			if (power == -1) return false;

			builder.setPosition(iterator);
			Value newVal = builder.buildShl(inst.getX(), builder.buildConstantInt(power, width),
					inst.getName() + "Shl");
			if (yVal < 0) newVal = builder.buildSub(builder.buildConstantInt(0, width), newVal, inst.getName());
			inst.replaceAllUsage(newVal);
			return true;
		}
	}
}
