package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collection;
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

	public Optional<Constant> tryFoldIntBinary(Value l, Value r, BiFunction<Long, Long, Long> fold) {
		if (l instanceof ConstantInt lC && r instanceof ConstantInt rC)
			return Optional.of(builder.buildConstantInt(fold.apply(lC.value, rC.value)));
		return Optional.empty();
	}

	public Optional<Constant> tryFoldAnd(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l & r);
	}

	@Override
	public Optional<Constant> visit(And inst) {
		return tryFoldAnd(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldAShr(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l >> r);
	}

	@Override
	public Optional<Constant> visit(AShr inst) {
		return tryFoldAShr(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldShl(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l << r);
	}

	@Override
	public Optional<Constant> visit(Shl inst) {
		return tryFoldShl(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldXor(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l ^ r);
	}

	@Override
	public Optional<Constant> visit(Xor inst) {
		return tryFoldXor(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldLShr(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l >>> r);
	}

	@Override
	public Optional<Constant> visit(LShr inst) {
		return tryFoldLShr(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldAdd(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, Long::sum);
	}

	@Override
	public Optional<Constant> visit(Add inst) {
		return tryFoldAdd(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldSub(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l - r);
	}

	@Override
	public Optional<Constant> visit(Sub inst) {
		return tryFoldSub(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldMul(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l * r);
	}

	@Override
	public Optional<Constant> visit(Mul inst) {
		return tryFoldMul(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldSDiv(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l / r);
	}

	@Override
	public Optional<Constant> visit(SDiv inst) {
		return tryFoldSDiv(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldSRem(Value lhs, Value rhs) {
		return tryFoldIntBinary(lhs, rhs, (l, r) -> l % r);
	}

	@Override
	public Optional<Constant> visit(SRem inst) {
		return tryFoldSRem(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldICmpAtom(Value l, Value r, BiFunction<Long, Long, Boolean> fold) {
		return tryFoldIntBinary(l, r, (x, y) -> fold.apply(x, y) ? 1L : 0L);
	}

	public Optional<Constant> tryFoldICmp(Value lhs, Value rhs, CompareOp code) {
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
	public Optional<Constant> visit(ICmp inst) {
		return tryFoldICmp(inst.getX(), inst.getY(), inst.method);
	}

	public Optional<Constant> tryFoldFloatBinary(Value l, Value r, BiFunction<Float, Float, Float> fold) {
		if (l instanceof ConstantFloat lC && r instanceof ConstantFloat rC)
			return Optional.of(builder.buildConstantFloat(fold.apply((float) lC.value, (float) rC.value)));
		return Optional.empty();
	}

	public Optional<Constant> tryFoldFAdd(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, Float::sum);
	}

	@Override
	public Optional<Constant> visit(FAdd inst) {
		return tryFoldFAdd(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldFMul(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l * r);
	}

	@Override
	public Optional<Constant> visit(FMul inst) {
		return tryFoldFMul(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldFSub(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l - r);
	}

	@Override
	public Optional<Constant> visit(FSub inst) {
		return tryFoldFSub(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldFDiv(Value lhs, Value rhs) {
		return tryFoldFloatBinary(lhs, rhs, (l, r) -> l / r);
	}

	@Override
	public Optional<Constant> visit(FDiv inst) {
		return tryFoldFDiv(inst.getX(), inst.getY());
	}

	public Optional<Constant> tryFoldFloatUnary(Value x, Function<Double, Double> fold) {
		if (x instanceof ConstantFloat xC) return Optional.of(builder.buildConstantFloat(fold.apply(xC.value)));
		return Optional.empty();
	}

	public Optional<Constant> tryFoldFNeg(Value op) {
		return tryFoldFloatUnary(op, x -> -x);
	}

	@Override
	public Optional<Constant> visit(FNeg inst) {
		return tryFoldFNeg(inst.getX());
	}

	public Optional<Constant> tryFoldFCmpAtom(Value l, Value r, BiFunction<Float, Float, Boolean> fold) {
		if (l instanceof ConstantFloat lC && r instanceof ConstantFloat rC)
			return Optional.of(builder.buildConstantInt(fold.apply((float) lC.value, (float) rC.value) ? 1L : 0L));
		return Optional.empty();
	}

	public Optional<Constant> tryFoldFCmp(Value lhs, Value rhs, CompareOp code) {
		return switch (code) {
			case EQ -> tryFoldFCmpAtom(lhs, rhs, Float::equals);
			case NE -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> !l.equals(r));
			case LT -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l < r);
			case GT -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l > r);
			case LE -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l <= r);
			case GE -> tryFoldFCmpAtom(lhs, rhs, (l, r) -> l >= r);
		};
	}

	@Override
	public Optional<Constant> visit(FCmp inst) {
		return tryFoldFCmp(inst.getX(), inst.getY(), inst.method);
	}

	public Optional<Constant> tryFoldFpToSi(Value op) {
		if (op instanceof ConstantFloat opC) return Optional.of(builder.buildConstantInt((long) opC.value));
		return Optional.empty();
	}

	@Override
	public Optional<Constant> visit(FpToSi inst) {
		return tryFoldFpToSi(inst.getX());
	}

	public Optional<Constant> tryFoldSiToFp(Value op) {
		if (op instanceof ConstantInt opC) return Optional.of(builder.buildConstantFloat(opC.value));
		return Optional.empty();
	}

	@Override
	public Optional<Constant> visit(SiToFp inst) {
		return tryFoldSiToFp(inst.getX());
	}

	@Override
	public Optional<?> visit(Phi inst) {
		return tryFoldPhi(inst, inst.getIncoming().values());
	}

	private Optional<Constant> tryFoldPhi(Phi self, Collection<Value> ops) {
		if (ops.isEmpty()) return Optional.empty();
		var iter = ops.iterator();
		Value first;
		do first = iter.next(); while (iter.hasNext() && first == self);
		if (first == self) return Optional.empty();
		if (!(first instanceof Constant firstConst)) return Optional.empty();

		if (ops.stream().allMatch(v -> v == self || ValueUtil.trivialInterchangeable(firstConst, v)))
			return Optional.of(firstConst);
		else return Optional.empty();
	}

	public Optional<Constant> tryFoldPhi(Collection<Value> ops) {
		return tryFoldPhi(null, ops);
	}

	public Optional<BasicBlock> tryFoldCondBr(Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
		if (trueTarget == falseTarget) return Optional.of(trueTarget);

		if (condition instanceof ConstantInt conditionC) {
			if (conditionC.value == 0) return Optional.of(falseTarget);
			else return Optional.of(trueTarget);
		} else return Optional.empty();
	}

	@Override
	public Optional<BasicBlock> visit(CondBr inst) {
		return tryFoldCondBr(inst.getCondition(), inst.getTrueTarget(), inst.getFalseTarget());
	}
}