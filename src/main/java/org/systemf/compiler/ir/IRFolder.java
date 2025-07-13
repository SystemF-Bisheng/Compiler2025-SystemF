package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IRFolder extends InstructionVisitorBase<Optional<?>> {
	public final IRBuilder builder;

	public IRFolder(IRBuilder builder) {
		this.builder = builder;
	}

	@Override
	protected Optional<?> defaultValue() {
		return Optional.empty();
	}

	public Optional<Value> tryFoldIntBinary(Value l, Value r, BiFunction<Long, Long, Long> fold) {
		if (l instanceof ConstantInt lC && r instanceof ConstantInt rC)
			return Optional.of(builder.buildConstantInt(fold.apply(lC.value, rC.value)));
		return Optional.empty();
	}

	public Optional<Value> tryFoldAnd(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l & r);
	}

	@Override
	public Optional<Value> visit(And inst) {
		return tryFoldAnd(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldAShr(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l >> r);
	}

	@Override
	public Optional<Value> visit(AShr inst) {
		return tryFoldAShr(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldShl(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l << r);
	}

	@Override
	public Optional<Value> visit(Shl inst) {
		return tryFoldShl(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldXor(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l ^ r);
	}

	@Override
	public Optional<Value> visit(Xor inst) {
		return tryFoldXor(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldLShr(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l >>> r);
	}

	@Override
	public Optional<Value> visit(LShr inst) {
		return tryFoldLShr(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldAdd(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, Long::sum);
	}

	@Override
	public Optional<Value> visit(Add inst) {
		return tryFoldAdd(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldSub(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l - r);
	}

	@Override
	public Optional<Value> visit(Sub inst) {
		return tryFoldSub(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldMul(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l * r);
	}

	@Override
	public Optional<Value> visit(Mul inst) {
		return tryFoldMul(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldSDiv(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l / r);
	}

	@Override
	public Optional<Value> visit(SDiv inst) {
		return tryFoldSDiv(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldSRem(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l % r);
	}

	@Override
	public Optional<Value> visit(SRem inst) {
		return tryFoldSRem(inst.getX(), inst.getY());
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

	@Override
	public Optional<Value> visit(ICmp inst) {
		return tryFoldICmp(inst.getX(), inst.getY(), inst.method);
	}

	public Optional<Value> tryFoldFloatBinary(Value l, Value r, BiFunction<Double, Double, Double> fold) {
		if (l instanceof ConstantFloat lC && r instanceof ConstantFloat rC)
			return Optional.of(builder.buildConstantFloat(fold.apply(lC.value, rC.value)));
		return Optional.empty();
	}

	public Optional<Value> tryFoldFAdd(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, Double::sum);
	}

	@Override
	public Optional<Value> visit(FAdd inst) {
		return tryFoldFAdd(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldFMul(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l * r);
	}

	@Override
	public Optional<Value> visit(FMul inst) {
		return tryFoldFMul(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldFSub(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l - r);
	}

	@Override
	public Optional<Value> visit(FSub inst) {
		return tryFoldFSub(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldFDiv(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l / r);
	}

	@Override
	public Optional<Value> visit(FDiv inst) {
		return tryFoldFDiv(inst.getX(), inst.getY());
	}

	public Optional<Value> tryFoldFloatUnary(Value x, Function<Double, Double> fold) {
		if (x instanceof ConstantFloat xC) return Optional.of(builder.buildConstantFloat(fold.apply(xC.value)));
		return Optional.empty();
	}

	public Optional<Value> tryFoldFNeg(Value op) {
		return tryFoldFloatUnary(op, x -> -x);
	}

	@Override
	public Optional<Value> visit(FNeg inst) {
		return tryFoldFNeg(inst.getX());
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

	@Override
	public Optional<Value> visit(FCmp inst) {
		return tryFoldFCmp(inst.getX(), inst.getY(), inst.method);
	}

	public Optional<Value> tryFoldFpToSi(Value op) {
		if (op instanceof ConstantFloat opC) return Optional.of(builder.buildConstantInt((long) opC.value));
		return Optional.empty();
	}

	@Override
	public Optional<Value> visit(FpToSi inst) {
		return tryFoldFpToSi(inst.getX());
	}

	public Optional<Value> tryFoldSiToFp(Value op) {
		if (op instanceof ConstantInt opC) return Optional.of(builder.buildConstantFloat(opC.value));
		return Optional.empty();
	}

	@Override
	public Optional<Value> visit(SiToFp inst) {
		return tryFoldSiToFp(inst.getX());
	}

	public Optional<Terminal> tryFoldCondBr(Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
		if (condition instanceof ConstantInt conditionC) {
			if (conditionC.value == 0) return Optional.of(builder.buildBr(falseTarget));
			else return Optional.of(builder.buildBr(trueTarget));
		} else return Optional.empty();
	}

	@Override
	public Optional<Terminal> visit(CondBr inst) {
		return tryFoldCondBr(inst.getCondition(), inst.getTrueTarget(), inst.getFalseTarget());
	}
}