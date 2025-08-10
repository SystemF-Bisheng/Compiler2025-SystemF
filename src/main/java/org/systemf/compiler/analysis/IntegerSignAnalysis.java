package org.systemf.compiler.analysis;

import org.systemf.compiler.analysis.IntegerSignAnalysisResult.IntegerSign;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi32;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si32ToSi64;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si64ToSi32;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayDeque;
import java.util.HashMap;

import static org.systemf.compiler.analysis.IntegerSignAnalysisResult.IntegerSign.*;

/**
 * Depend on: PointerAnalysis
 * <p>
 * Applicable to: IR
 */
public enum IntegerSignAnalysis implements AttributeProvider<Module, IntegerSignAnalysisResult> {
	INSTANCE;

	@Override
	public IntegerSignAnalysisResult getAttribute(Module entity) {
		return new IntegerSignAnalysisContext(entity).run();
	}

	private static class IntegerSignAnalysisContext extends InstructionVisitorBase<Void> {
		private final Module module;
		private final PointerAnalysisResult ptrResult;
		private final HashMap<Ret, Function> retBelong = new HashMap<>();
		private final ArrayDeque<Instruction> worklist = new ArrayDeque<>();
		private final IntegerSignAnalysisResult result = new IntegerSignAnalysisResult(new HashMap<>(),
				new HashMap<>(), new HashMap<>());

		public IntegerSignAnalysisContext(Module module) {
			this.module = module;
			this.ptrResult = QueryManager.getInstance().getAttribute(module, PointerAnalysisResult.class);
		}

		private void collectRet(Function function) {
			function.allInstructions().filter(inst -> inst instanceof Ret)
					.forEach(inst -> retBelong.put((Ret) inst, function));
		}

		private void collectRet() {
			module.getFunctions().values().forEach(this::collectRet);
		}

		private void collectGlobalInitializer(GlobalVariable global) {
			addAllocSign(global, result.sign(global.getInitializer()));
		}

		private void collectGlobalInitializer() {
			module.getGlobalDeclarations().values().forEach(this::collectGlobalInitializer);
		}

		public IntegerSignAnalysisResult run() {
			collectRet();
			collectGlobalInitializer();

			module.getFunctions().values().stream().flatMap(Function::allInstructions).forEach(worklist::offer);
			while (!worklist.isEmpty()) {
				var inst = worklist.poll();
				inst.accept(this);
			}
			return result;
		}

		private void putSign(Value value, IntegerSign sign) {
			var oldSign = result.sign(value);
			if (oldSign == sign) return;
			result.sign().put(value, sign);
			worklist.addAll(value.getDependant());
		}

		private void addSign(Value value, IntegerSign sign) {
			var oldSign = result.sign(value);
			var newSign = oldSign.meet(sign);
			if (oldSign == newSign) return;
			result.sign().put(value, newSign);
			worklist.addAll(value.getDependant());
		}

		private void addAllocSign(Value alloc, IntegerSign sign) {
			var oldSign = result.allocSign(alloc);
			var newSign = oldSign.meet(sign);
			if (oldSign == newSign) return;
			result.allocSign().put(alloc, newSign);
			ptrResult.pointedBy(alloc).stream().flatMap(ptr -> ptr.getDependant().stream()).forEach(worklist::offer);
		}

		private void addFuncSign(Function function, IntegerSign sign) {
			var oldSign = result.funcSign(function);
			var newSign = oldSign.meet(sign);
			if (oldSign == newSign) return;
			result.funcSign().put(function, newSign);
			worklist.addAll(function.getDependant());
		}

		public IntegerSign signOfPlus(IntegerSign xSign, IntegerSign ySign) {
			if (xSign.ordinal() > ySign.ordinal()) return signOfPlus(ySign, xSign);
			return switch (xSign) {
				case UNDEFINED -> UNDEFINED;
				case NEGATIVE -> switch (ySign) {
					case NEGATIVE, ZERO, NON_POSITIVE -> NEGATIVE;
					case POSITIVE, NON_NEGATIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case POSITIVE -> switch (ySign) {
					case POSITIVE, ZERO, NON_NEGATIVE -> POSITIVE;
					case NON_POSITIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case ZERO, NON_NEGATIVE -> ySign;
				case NON_POSITIVE -> switch (ySign) {
					case NON_POSITIVE -> NON_POSITIVE;
					case NON_NEGATIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case ALL -> ALL;
			};
		}

		public IntegerSign signOfMul(IntegerSign xSign, IntegerSign ySign) {
			if (xSign.ordinal() > ySign.ordinal()) return signOfMul(ySign, xSign);
			return switch (xSign) {
				case UNDEFINED, ALL, ZERO -> xSign;
				case NEGATIVE -> switch (ySign) {
					case NEGATIVE -> POSITIVE;
					case POSITIVE -> NEGATIVE;
					case ZERO, ALL -> ySign;
					case NON_POSITIVE -> NON_NEGATIVE;
					case NON_NEGATIVE -> NON_POSITIVE;
					default -> throw new AssertionError();
				};
				case POSITIVE, NON_NEGATIVE -> ySign;
				case NON_POSITIVE -> switch (ySign) {
					case NON_POSITIVE -> NON_NEGATIVE;
					case NON_NEGATIVE -> NON_POSITIVE;
					case ALL -> ySign;
					default -> throw new AssertionError();
				};
			};
		}

		@Override
		public Void visit(Add inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfPlus(xSign, ySign);
			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(Sub inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfPlus(xSign, ySign.neg());
			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(Mul inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfMul(xSign, ySign);
			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(SDiv inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfMul(xSign, ySign.inv());
			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(SRem inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			IntegerSign sign;
			if (xSign == UNDEFINED || ySign == UNDEFINED || ySign == ZERO) sign = UNDEFINED;
			else sign = switch (xSign) {
				case NEGATIVE, NON_POSITIVE -> NON_POSITIVE;
				case POSITIVE, NON_NEGATIVE -> NON_NEGATIVE;
				case ZERO, ALL -> xSign;
				default -> throw new AssertionError();
			};
			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(ICmp inst) {
			putSign(inst, NON_NEGATIVE);
			return null;
		}

		public IntegerSign signOfAnd(IntegerSign xSign, IntegerSign ySign) {
			if (xSign.ordinal() > ySign.ordinal()) return signOfAnd(ySign, xSign);
			return switch (xSign) {
				case UNDEFINED, ZERO, NON_NEGATIVE, ALL -> xSign;
				case NEGATIVE -> switch (ySign) {
					case NEGATIVE, ZERO, NON_POSITIVE, NON_NEGATIVE, ALL -> ySign;
					case POSITIVE -> NON_NEGATIVE;
					default -> throw new AssertionError();
				};
				case POSITIVE -> switch (ySign) {
					case POSITIVE, NON_POSITIVE, NON_NEGATIVE, ALL -> NON_NEGATIVE;
					case ZERO -> ZERO;
					default -> throw new AssertionError();
				};
				case NON_POSITIVE -> ySign;
			};
		}

		@Override
		public Void visit(And inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfAnd(xSign, ySign);
			putSign(inst, sign);
			return null;
		}

		public IntegerSign signOfOr(IntegerSign xSign, IntegerSign ySign) {
			if (xSign.ordinal() > ySign.ordinal()) return signOfOr(ySign, xSign);
			return switch (xSign) {
				case UNDEFINED, NEGATIVE, ALL -> xSign;
				case POSITIVE -> switch (ySign) {
					case POSITIVE, ZERO, NON_NEGATIVE -> POSITIVE;
					case NON_POSITIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case ZERO, NON_NEGATIVE -> ySign;
				case NON_POSITIVE -> switch (ySign) {
					case NON_POSITIVE -> NON_POSITIVE;
					case NON_NEGATIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
			};
		}

		@Override
		public Void visit(Or inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfOr(xSign, ySign);
			putSign(inst, sign);
			return null;
		}

		public IntegerSign signOfXor(IntegerSign xSign, IntegerSign ySign) {
			if (xSign.ordinal() > ySign.ordinal()) return signOfOr(ySign, xSign);
			return switch (xSign) {
				case UNDEFINED, ALL -> xSign;
				case NEGATIVE -> switch (ySign) {
					case NEGATIVE -> NON_NEGATIVE;
					case POSITIVE, NON_NEGATIVE, ZERO -> NEGATIVE;
					case NON_POSITIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case POSITIVE -> switch (ySign) {
					case POSITIVE, NON_NEGATIVE -> NON_NEGATIVE;
					case ZERO -> POSITIVE;
					case NON_POSITIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case ZERO, NON_NEGATIVE -> ySign;
				case NON_POSITIVE -> ALL;
			};
		}

		@Override
		public Void visit(Xor inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());
			var sign = signOfXor(xSign, ySign);
			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(Shl inst) {
			putSign(inst, result.sign(inst.getX())); // Assume: Overflowing doesn't matter
			return null;
		}

		@Override
		public Void visit(LShr inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());

			IntegerSign sign;
			if (xSign == UNDEFINED || ySign == UNDEFINED) sign = UNDEFINED;
			else if (xSign == ZERO) sign = ZERO;
			else sign = NON_NEGATIVE;
			if (ZERO.subsetEq(ySign)) sign = sign.meet(xSign);

			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(AShr inst) {
			var xSign = result.sign(inst.getX());
			var ySign = result.sign(inst.getY());

			IntegerSign sign;
			if (xSign == UNDEFINED || ySign == UNDEFINED) sign = UNDEFINED;
			else sign = xSign;
			if (ySign != ZERO && POSITIVE.subsetEq(xSign)) sign = sign.meet(ZERO);

			putSign(inst, sign);
			return null;
		}

		@Override
		public Void visit(FpToSi32 inst) {
			putSign(inst, ALL);
			return null;
		}

		@Override
		public Void visit(Si32ToSi64 inst) {
			putSign(inst, result.sign(inst.getX()));
			return null;
		}

		@Override
		public Void visit(Si64ToSi32 inst) {
			putSign(inst, result.sign(inst.getX()));
			return null;
		}

		private IntegerSign handleCall(AbstractCall inst) {
			var func = inst.getFunction();
			var args = inst.getArgs();
			return switch (func) {
				case Function userFunc -> {
					var params = userFunc.getFormalArgs();
					for (int i = 0; i < params.length; ++i) addSign(params[i], result.sign(args[i]));
					yield result.funcSign(userFunc);
				}
				case ExternalFunction _ -> {
					for (var arg : args) ptrResult.pointTo(arg).forEach(alloc -> addAllocSign(alloc, ALL));
					yield ALL;
				}
				default -> throw new UnsupportedOperationException();
			};
		}

		@Override
		public Void visit(Call inst) {
			putSign(inst, handleCall(inst));
			return null;
		}

		@Override
		public Void visit(CallVoid inst) {
			handleCall(inst);
			return null;
		}

		@Override
		public Void visit(Load inst) {
			putSign(inst, ptrResult.pointTo(inst.getPointer()).stream().map(result::allocSign)
					.reduce(UNDEFINED, IntegerSign::meet));
			return null;
		}

		@Override
		public Void visit(Store inst) {
			var srcSign = result.sign(inst.getSrc());
			ptrResult.pointTo(inst.getDest()).forEach(alloc -> addAllocSign(alloc, srcSign));
			return null;
		}

		@Override
		public Void visit(Phi inst) {
			putSign(inst, inst.getIncoming().values().stream().map(result::sign).reduce(UNDEFINED, IntegerSign::meet));
			return null;
		}

		@Override
		public Void visit(Ret inst) {
			addFuncSign(retBelong.get(inst), result.sign(inst.getReturnValue()));
			return null;
		}
	}
}
