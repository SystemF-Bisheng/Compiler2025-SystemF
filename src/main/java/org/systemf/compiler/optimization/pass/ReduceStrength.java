package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.AShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.LShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Shl;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SRem;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.MathUtil;
import org.systemf.compiler.util.SaturationArithmetic;

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

		/**
		 * @param y Shall not be zero or a power of 2, no matter positive or negative
		 */
		private Value handleSDiv32Constant(Value x, long y, String name) {
			final int N = 32;
			var ySign = y < 0;
			var x64 = builder.buildSi32ToSi64(x, "sdivTo64");
			var resSign = builder.buildICmp(x, builder.buildConstantZero(N), "sdivResSign",
					ySign ? CompareOp.GT : CompareOp.LT);
			var yAbs = Math.abs(y);

			int l = 0;
			long m;
			for (; ; ++l) {
				long tmp = 1L << (N - 1 + l);
				m = tmp / yAbs + 1; // + 1 for ceiling
				if (m * yAbs <= tmp + (1L << l)) break;
			}
			if (ySign) m = -m;

			Value divValue;
			if (SaturationArithmetic.isOverflow(m, N)) {
				if (m > 0) m -= 1L << N;
				else m += 1L << N;

				var mulValue = builder.buildMul(x64, builder.buildConstantInt64(m), "sdivMul");
				var shrValue = builder.buildAShr(mulValue, builder.buildConstantInt64(N), "sdivIAShr");
				var shrValue32 = builder.buildSi64ToSi32(shrValue, "sdivI32AShr");

				Value midValue;
				if (m > 0) midValue = builder.buildAdd(x, shrValue32, "sdivAdd");
				else midValue = builder.buildAdd(shrValue32, x, "sdivSub");

				divValue = builder.buildAShr(midValue, builder.buildConstantInt32(l - 1), "sdivDiv");
			} else {
				var mulValue = builder.buildMul(x64, builder.buildConstantInt64(m), "sdivMul");
				var shrValue = builder.buildAShr(mulValue, builder.buildConstantInt64(N - 1 + l), "sdivAShr");
				divValue = builder.buildSi64ToSi32(shrValue, "sdivDiv");
			}

			return builder.buildAdd(divValue, resSign, name);
		}

		@Override
		public Boolean visit(SDiv inst) {
			if (checkIdentity(inst, 1)) return true;

			var y = inst.getY();
			if (!ValueUtil.isConstantInt(y)) return false;

			var yVal = ValueUtil.getConstantInt(y);
			if (yVal == 0) return false;
			var yAbs = Math.abs(yVal);
			var yPow = MathUtil.checkPowerOfTwo(yAbs);
			var x = inst.getX();
			var width = ValueUtil.getWidth(inst);
			var name = inst.getName();

			if (yPow != -1) {
				builder.setPosition(iterator);
				var xNeg = builder.buildICmp(x, builder.buildConstantZero(width), "sdivSign", CompareOp.LT);
				var toAdd = builder.buildMul(xNeg, builder.buildConstantInt(yAbs - 1, width), "sdivToAdd");
				var addValue = builder.buildAdd(x, toAdd, "sdivAdd");
				Value newValue;
				if (yVal < 0) {
					var shrValue = builder.buildAShr(addValue, builder.buildConstantInt(yPow, width), "sdivAShr");
					newValue = builder.buildSub(builder.buildConstantZero(width), shrValue, name);
				} else newValue = builder.buildAShr(addValue, builder.buildConstantInt(yPow, width), name);
				inst.replaceAllUsage(newValue);
			} else {
				if (width != 32) return false;
				builder.setPosition(iterator);
				inst.replaceAllUsage(handleSDiv32Constant(x, yVal, name));
			}
			return true;
		}

		@Override
		public Boolean visit(SRem inst) {
			var y = inst.getY();
			if (!ValueUtil.isConstantInt(y)) return false;

			var yVal = ValueUtil.getConstantInt(y);
			if (yVal == 0) return false;
			var yAbs = Math.abs(yVal);
			var yPow = MathUtil.checkPowerOfTwo(yAbs);
			var x = inst.getX();
			var width = ValueUtil.getWidth(inst);
			var name = inst.getName();

			if (yPow != -1) {
				builder.setPosition(iterator);
				var xNeg = builder.buildICmp(x, builder.buildConstantZero(width), "sremSign", CompareOp.LT);
				var toAdd = builder.buildMul(xNeg, builder.buildConstantInt(yAbs - 1, width), "sremToAdd");
				var addValue = builder.buildAdd(x, toAdd, "sremAdd");
				var andValue = builder.buildAnd(addValue, builder.buildConstantInt(-yAbs, width), "sremAnd");
				var subValue = builder.buildSub(x, andValue, name);
				inst.replaceAllUsage(subValue);
			} else {
				if (width != 32) return false;
				builder.setPosition(iterator);
				var divValue = handleSDiv32Constant(x, yVal, "sremDiv");
				var toSub = builder.buildMul(divValue, y, "sremToSub");
				var subValue = builder.buildSub(x, toSub, name);
				inst.replaceAllUsage(subValue);
			}
			return true;
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
				inst.replaceAllUsage(builder.buildConstantZero(width));
				return true;
			}

			var yAbs = Math.abs(yVal);
			var power = MathUtil.checkPowerOfTwo(yAbs);
			if (power == -1) return false;

			var name = inst.getName();
			builder.setPosition(iterator);
			Value newVal = builder.buildShl(inst.getX(), builder.buildConstantInt(power, width), name + "Shl");
			if (yVal < 0) newVal = builder.buildSub(builder.buildConstantZero(width), newVal, name);
			inst.replaceAllUsage(newVal);
			return true;
		}
	}
}
