package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.nonterminal.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.*;
import org.systemf.compiler.lower.rv64gc.instruction.*;

public class InstructionVisitorBase<T> implements InstructionVisitor<T> {
	protected T defaultValue() {
		return null;
	}

	/// IR
	public T visit(DummyBinary inst) {
		return defaultValue();
	}

	public T visit(DummyIntBinary inst) {
		return visit((DummyBinary) inst);
	}

	public T visit(DummyFloatBinary inst) {
		return visit((DummyBinary) inst);
	}

	public T visit(DummyCompare inst) {
		return visit((DummyBinary) inst);
	}

	public T visit(DummyUnary inst) {
		return defaultValue();
	}

	@Override
	public T visit(Add inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(Sub inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(Mul inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(SDiv inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(SRem inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(ICmp inst) {
		return visit((DummyCompare) inst);
	}

	@Override
	public T visit(FAdd inst) {
		return visit((DummyFloatBinary) inst);
	}

	@Override
	public T visit(FSub inst) {
		return visit((DummyFloatBinary) inst);
	}

	@Override
	public T visit(FMul inst) {
		return visit((DummyFloatBinary) inst);
	}

	@Override
	public T visit(FDiv inst) {
		return visit((DummyFloatBinary) inst);
	}

	@Override
	public T visit(FNeg inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(FCmp inst) {
		return visit((DummyCompare) inst);
	}

	@Override
	public T visit(And inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(Or inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(Xor inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(Shl inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(LShr inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(AShr inst) {
		return visit((DummyIntBinary) inst);
	}

	@Override
	public T visit(FpToSi32 inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(PtrCast inst) {
		return defaultValue();
	}

	@Override
	public T visit(Si32ToFp inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(Si32ToSi64 inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(Si64ToSi32 inst) {
		return visit((DummyUnary) inst);
	}

	public T visit(AbstractCall inst) {
		return defaultValue();
	}

	@Override
	public T visit(Call inst) {
		return visit((AbstractCall) inst);
	}

	@Override
	public T visit(CallVoid inst) {
		return visit((AbstractCall) inst);
	}

	@Override
	public T visit(Alloca inst) {
		return defaultValue();
	}

	@Override
	public T visit(GetPtr inst) {
		return defaultValue();
	}

	@Override
	public T visit(Load inst) {
		return defaultValue();
	}

	@Override
	public T visit(Store inst) {
		return defaultValue();
	}

	@Override
	public T visit(Unreachable inst) {
		return defaultValue();
	}

	@Override
	public T visit(Phi inst) {
		return defaultValue();
	}

	@Override
	public T visit(Br inst) {
		return defaultValue();
	}

	@Override
	public T visit(CondBr inst) {
		return defaultValue();
	}

	@Override
	public T visit(Ret inst) {
		return defaultValue();
	}

	@Override
	public T visit(RetVoid inst) {
		return defaultValue();
	}

	/// RV64GC
	@Override
	public T visit(RVAdd inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVAddImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVAddWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVAddWordImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVAnd inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVAndImm inst) {
		return defaultValue();
	}

	public T visit(RVCompBranch inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVBranchEq inst) {
		return visit((RVCompBranch) inst);
	}

	@Override
	public T visit(RVBranchLess inst) {
		return visit((RVCompBranch) inst);
	}

	@Override
	public T visit(RVCvtWord2Float inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVCvtDWord2Float inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVCvtFloat2Word inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVCvtFloat2DWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVDiv inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVDivWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatAdd inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatDiv inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatEq inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatLe inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatLt inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatMul inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatMulAdd inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatMulSub inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatNeg inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatNegMulAdd inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatNegMulSub inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVFloatSub inst) {
		return defaultValue();
	}

	public T visit(RVLoad inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVLoadDWord inst) {
		return visit((RVLoad) inst);
	}

	@Override
	public T visit(RVLoadFloat inst) {
		return visit((RVLoad) inst);
	}

	@Override
	public T visit(RVLoadWord inst) {
		return visit((RVLoad) inst);
	}

	@Override
	public T visit(RVMul inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVMulWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVOr inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVOrImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVRem inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVRemWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVSetLessThan inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftLeft inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftLeftImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftLeftWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftLeftWordImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightArith inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightArithImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightArithWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightArithWordImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightLogical inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightLogicalImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightLogicalWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVShiftRightLogicalWordImm inst) {
		return defaultValue();
	}

	public T visit(RVStore inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVStoreDWord inst) {
		return visit((RVStore) inst);
	}

	@Override
	public T visit(RVStoreFloat inst) {
		return visit((RVStore) inst);
	}

	@Override
	public T visit(RVStoreWord inst) {
		return visit((RVStore) inst);
	}

	@Override
	public T visit(RVSub inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVSubWord inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVXor inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVParallelMove inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVRegPlaceholder inst) {
		return defaultValue();
	}
}