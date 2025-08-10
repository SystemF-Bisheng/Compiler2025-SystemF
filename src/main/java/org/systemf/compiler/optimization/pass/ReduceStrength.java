package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.IntegerSignAnalysisResult;
import org.systemf.compiler.analysis.IntegerSignAnalysisResult.IntegerSign;
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
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.MathUtil;
import org.systemf.compiler.util.SaturationArithmetic;

import java.util.ListIterator;
import java.util.Optional;

/**
 * Depend on: IntegerSignAnalysis
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
		private final IntegerSignAnalysisResult signResult;
		private final Module module;
		private IRBuilder builder;
		private ListIterator<Instruction> iterator;

		public ReduceStrengthContext(Module module) {
			this.module = module;
			this.signResult = query.getAttribute(module, IntegerSignAnalysisResult.class);
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (iterator = block.instructions.listIterator(); iterator.hasNext(); ) {
				var instruction = iterator.next();
				if (instruction.accept(this)) {
					res = true;
					break;
				}
			}
			return res;
		}

		private boolean processFunction(Function function) {
			var res = false;
			for (var block : function.getBlocks())
				if (processBlock(block)) {
					res = true;
					break;
				}
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = false;
				for (var func : module.getFunctions().values())
					if (processFunction(func)) {
						res = true;
						break;
					}
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

		private Value calcSign(Value x, int width, boolean neg, String name) {
			var xSign = signResult.sign(x);
			Value res;
			if (neg) {
				if (xSign.subsetEq(IntegerSign.NON_NEGATIVE)) res = builder.buildFalse();
				else if (xSign.subsetEq(IntegerSign.NEGATIVE)) res = builder.buildTrue();
				else res = builder.buildOrFoldICmp(x, builder.buildConstantZero(width), name, CompareOp.LT);
			} else {
				if (xSign.subsetEq(IntegerSign.NON_POSITIVE)) res = builder.buildFalse();
				else if (xSign.subsetEq(IntegerSign.POSITIVE)) res = builder.buildTrue();
				else res = builder.buildOrFoldICmp(x, builder.buildConstantZero(width), name, CompareOp.GT);
			}
			return builder.buildOrFoldSiCast(res, width, name);
		}

		/**
		 * @param y Shall not be zero or a power of 2, no matter positive or negative
		 */
		private Value handleSDiv32Constant(Value x, long y, String name) {
			final int N = 32;
			var ySign = y < 0;
			var x64 = builder.buildOrFoldSi32ToSi64(x, "sdivTo64");
			var resSign = calcSign(x, N, !ySign, "sdivResSign");
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

				var mulValue = builder.buildOrFoldMul(x64, builder.buildConstantInt64(m), "sdivMul");
				var shrValue = builder.buildOrFoldAShr(mulValue, builder.buildConstantInt64(N), "sdivIAShr");
				var shrValue32 = builder.buildOrFoldSi64ToSi32(shrValue, "sdivI32AShr");

				Value midValue;
				if (m > 0) midValue = builder.buildOrFoldAdd(x, shrValue32, "sdivAdd");
				else midValue = builder.buildOrFoldAdd(shrValue32, x, "sdivSub");

				divValue = builder.buildOrFoldAShr(midValue, builder.buildConstantInt32(l - 1), "sdivDiv");
			} else {
				var mulValue = builder.buildOrFoldMul(x64, builder.buildConstantInt64(m), "sdivMul");
				var shrValue = builder.buildOrFoldAShr(mulValue, builder.buildConstantInt64(N - 1 + l), "sdivAShr");
				divValue = builder.buildOrFoldSi64ToSi32(shrValue, "sdivDiv");
			}

			return builder.buildOrFoldAdd(divValue, resSign, name);
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
				var xNeg = calcSign(x, width, true, "sdivSign");
				var toAdd = builder.buildOrFoldMul(xNeg, builder.buildConstantInt(yAbs - 1, width), "sdivToAdd");
				var addValue = builder.buildOrFoldAdd(x, toAdd, "sdivAdd");
				Value newValue;
				if (yVal < 0) {
					var shrValue = builder.buildOrFoldAShr(addValue, builder.buildConstantInt(yPow, width), "sdivAShr");
					newValue = builder.buildOrFoldSub(builder.buildConstantZero(width), shrValue, name);
				} else newValue = builder.buildOrFoldAShr(addValue, builder.buildConstantInt(yPow, width), name);
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
				Value newValue;
				if (signResult.sign(x).subsetEq(IntegerSign.NON_NEGATIVE))
					newValue = builder.buildOrFoldAnd(x, builder.buildConstantInt(yAbs - 1, width), name);
				else {
					var xNeg = calcSign(x, width, true, "sremSign");
					var toAdd = builder.buildOrFoldMul(xNeg, builder.buildConstantInt(yAbs - 1, width), "sremToAdd");
					var addValue = builder.buildOrFoldAdd(x, toAdd, "sremAdd");
					var andValue = builder.buildOrFoldAnd(addValue, builder.buildConstantInt(-yAbs, width), "sremAnd");
					newValue = builder.buildOrFoldSub(x, andValue, name);
				}
				inst.replaceAllUsage(newValue);
			} else {
				if (width != 32) return false;
				builder.setPosition(iterator);
				var divValue = handleSDiv32Constant(x, yVal, "sremDiv");
				var toSub = builder.buildOrFoldMul(divValue, y, "sremToSub");
				var subValue = builder.buildOrFoldSub(x, toSub, name);
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

		private Optional<Value> handleMulPowerOf2(int width, Value x, long yVal, String name) {
			var yAbs = Math.abs(yVal);
			var power = MathUtil.checkPowerOfTwo(yAbs);
			if (power != -1) {
				Value newVal = builder.buildOrFoldShl(x, builder.buildConstantInt(power, width), name + "Shl");
				if (yVal < 0) newVal = builder.buildOrFoldSub(builder.buildConstantZero(width), newVal, name);
				return Optional.of(newVal);
			}
			return Optional.empty();
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

			var x = inst.getX();
			var name = inst.getName();
			builder.setPosition(iterator);
			return handleMulPowerOf2(width, x, yVal, name)
					.or(() -> handleMulPowerOf2(width, x, yVal + 1, "mulWithAdd").map(
							withAdd -> builder.buildOrFoldSub(withAdd, x, name)))
					.or(() -> handleMulPowerOf2(width, x, yVal - 1, "mulWithSub").map(
							withSub -> builder.buildOrFoldAdd(withSub, x, name)))
					.map(newVal -> {
						inst.replaceAllUsage(newVal);
						return true;
					}).orElse(false);
		}

		@Override
		public Boolean visit(ICmp inst) {
			var x = inst.getX();
			var y = inst.getY();
			var xSign = signResult.sign(x);
			var ySign = signResult.sign(y);
			Value newValue = switch (inst.method) {
				case LT -> {
					if (xSign == IntegerSign.ZERO) {
						if (ySign.subsetEq(IntegerSign.POSITIVE)) yield builder.buildTrue();
						if (ySign.subsetEq(IntegerSign.NON_POSITIVE)) yield builder.buildFalse();
					}
					if (ySign == IntegerSign.ZERO) {
						if (xSign.subsetEq(IntegerSign.NEGATIVE)) yield builder.buildTrue();
						if (xSign.subsetEq(IntegerSign.NON_NEGATIVE)) yield builder.buildFalse();
					}
					yield null;
				}
				case GE -> {
					if (xSign == IntegerSign.ZERO) {
						if (ySign.subsetEq(IntegerSign.NON_POSITIVE)) yield builder.buildTrue();
						if (ySign.subsetEq(IntegerSign.POSITIVE)) yield builder.buildFalse();
					}
					if (ySign == IntegerSign.ZERO) {
						if (xSign.subsetEq(IntegerSign.NON_NEGATIVE)) yield builder.buildTrue();
						if (xSign.subsetEq(IntegerSign.NEGATIVE)) yield builder.buildFalse();
					}
					yield null;
				}
				default -> null;
			};
			if (newValue == null) return false;
			inst.replaceAllUsage(newValue);
			return true;
		}
	}
}
