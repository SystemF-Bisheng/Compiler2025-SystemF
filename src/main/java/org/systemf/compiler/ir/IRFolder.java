package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IRFolder {
	public final IRBuilder builder;

	public IRFolder(IRBuilder builder) {
		this.builder = builder;
	}

	public Optional<Value> tryFoldIntBinary(Value l, Value r, BiFunction<Long, Long, Long> fold) {
		if (l instanceof ConstantInt lC && r instanceof ConstantInt rC)
			return Optional.of(builder.buildConstantInt(fold.apply(lC.value, rC.value)));
		return Optional.empty();
	}

	public Optional<Value> tryFoldAnd(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l & r);
	}

	public Optional<Value> tryFoldAShr(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l >> r);
	}

	public Optional<Value> tryFoldShl(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l << r);
	}

	public Optional<Value> tryFoldXor(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l ^ r);
	}

	public Optional<Value> tryFoldLShr(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l >>> r);
	}

	public Optional<Value> tryFoldAdd(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, Long::sum);
	}

	public Optional<Value> tryFoldSub(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l - r);
	}

	public Optional<Value> tryFoldMul(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l * r);
	}

	public Optional<Value> tryFoldSDiv(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l / r);
	}

	public Optional<Value> tryFoldSRem(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l % r);
	}

	public Optional<Value> tryFoldICmpAtom(Value l, Value r, BiFunction<Long, Long, Boolean> fold) {
		return tryFoldIntBinary(l, r, (x, y) -> fold.apply(x, y) ? 1L : 0L);
	}

	public Optional<Value> tryFoldICmp(Value lhs, Value rhs, CompareOp code) {
		return switch (code) {
			case EQ -> tryFoldICmpAtom(lhs, rhs, Long::equals);
			case NE -> tryFoldICmpAtom(lhs, rhs, (l, r) -> !l.equals(r));
			case LT -> tryFoldICmpAtom(lhs, rhs, (l, r) -> l < r);
			case GT -> tryFoldICmpAtom(lhs, rhs, (l, r) -> l > r);
			case LE -> tryFoldICmpAtom(lhs, rhs, (l, r) -> l <= r);
			case GE -> tryFoldICmpAtom(lhs, rhs, (l, r) -> l >= r);
		};
	}

	public Optional<Value> tryFoldFloatBinary(Value l, Value r, BiFunction<Double, Double, Double> fold) {
		if (l instanceof ConstantFloat lC && r instanceof ConstantFloat rC)
			return Optional.of(builder.buildConstantFloat(fold.apply(lC.value, rC.value)));
		return Optional.empty();
	}

	public Optional<Value> tryFoldFAdd(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, Double::sum);
	}

	public Optional<Value> tryFoldFMul(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l * r);
	}

	public Optional<Value> tryFoldFSub(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l - r);
	}

	public Optional<Value> tryFoldFDiv(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l / r);
	}

	public Optional<Value> tryFoldFloatUnary(Value x, Function<Double, Double> fold) {
		if (x instanceof ConstantFloat xC) return Optional.of(builder.buildConstantFloat(fold.apply(xC.value)));
		return Optional.empty();
	}

	public Optional<Value> tryFoldFNeg(Value op) {
		return tryFoldFloatUnary(op, x -> -x);
	}

	public Optional<Value> tryFoldFCmpAtom(Value l, Value r, BiFunction<Double, Double, Boolean> fold) {
		if (l instanceof ConstantFloat lC && r instanceof ConstantFloat rC)
			return Optional.of(builder.buildConstantInt(fold.apply(lC.value, rC.value) ? 1L : 0L));
		return Optional.empty();
	}

	public Optional<Value> tryFoldFCmp(Value lhs, Value rhs, CompareOp code) {
		return switch (code) {
			case EQ -> tryFoldFCmpAtom(lhs, rhs, Double::equals);
			case NE -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> !l.equals(r));
			case LT -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l < r);
			case GT -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l > r);
			case LE -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l <= r);
			case GE -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l >= r);
		};
	}

	public Optional<Value> tryFoldFpToSi(Value op) {
		if (op instanceof ConstantFloat opC) return Optional.of(builder.buildConstantInt((long) opC.value));
		return Optional.empty();
	}

	public Optional<Value> tryFoldSiToFp(Value op) {
		if (op instanceof ConstantInt opC) return Optional.of(builder.buildConstantFloat(opC.value));
		return Optional.empty();
	}

	public Optional<Terminal> tryFoldCondBr(Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
		if (condition instanceof ConstantInt conditionC) {
			if (conditionC.value == 0) return Optional.of(builder.buildBr(falseTarget));
			else return Optional.of(builder.buildBr(trueTarget));
		} else return Optional.empty();
	}
}