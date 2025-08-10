package org.systemf.compiler.ir;

import org.systemf.compiler.hir.value.instruction.nonterminal.scf.Break;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.For;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.If;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.While;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.Yield;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyFloatBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyIntBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyTriple;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.AShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.LShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Or;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Shl;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Xor;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi32;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.PtrCast;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si32ToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si32ToSi64;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si64ToSi32;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FAdd;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FCmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FMul;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FNeg;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FSub;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.ICmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SRem;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Sub;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.ir.value.instruction.terminal.Unreachable;
import org.systemf.compiler.lower.rv64gc.instruction.RVAdd;
import org.systemf.compiler.lower.rv64gc.instruction.RVAddImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVAddWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVAddWordImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVAnd;
import org.systemf.compiler.lower.rv64gc.instruction.RVAndImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVBranchEq;
import org.systemf.compiler.lower.rv64gc.instruction.RVBranchLess;
import org.systemf.compiler.lower.rv64gc.instruction.RVCompBranch;
import org.systemf.compiler.lower.rv64gc.instruction.RVCvtDWord2Float;
import org.systemf.compiler.lower.rv64gc.instruction.RVCvtFloat2DWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVCvtFloat2Word;
import org.systemf.compiler.lower.rv64gc.instruction.RVCvtWord2Float;
import org.systemf.compiler.lower.rv64gc.instruction.RVDiv;
import org.systemf.compiler.lower.rv64gc.instruction.RVDivWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatAdd;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatDiv;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatEq;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatLe;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatLt;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatMul;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatMulAdd;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatMulSub;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatNeg;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatNegMulAdd;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatNegMulSub;
import org.systemf.compiler.lower.rv64gc.instruction.RVFloatSub;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoad;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadAddress;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadDWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadFloat;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVMoveWord2Float;
import org.systemf.compiler.lower.rv64gc.instruction.RVMul;
import org.systemf.compiler.lower.rv64gc.instruction.RVMulWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVOr;
import org.systemf.compiler.lower.rv64gc.instruction.RVOrImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVParallelMove;
import org.systemf.compiler.lower.rv64gc.instruction.RVRegPlaceholder;
import org.systemf.compiler.lower.rv64gc.instruction.RVRem;
import org.systemf.compiler.lower.rv64gc.instruction.RVRemWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVSetLessThan;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftLeft;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftLeftImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftLeftWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftLeftWordImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightArith;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightArithImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightArithWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightArithWordImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightLogical;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightLogicalImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightLogicalWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVShiftRightLogicalWordImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVStore;
import org.systemf.compiler.lower.rv64gc.instruction.RVStoreDWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVStoreFloat;
import org.systemf.compiler.lower.rv64gc.instruction.RVStoreWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVSub;
import org.systemf.compiler.lower.rv64gc.instruction.RVSubWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVTailCall;
import org.systemf.compiler.lower.rv64gc.instruction.RVXor;
import org.systemf.compiler.lower.rv64gc.instruction.RVXorImm;

public class InstructionVisitorBase<T> implements InstructionVisitor<T> {
	protected T defaultValue() {
		return null;
	}

	/// Instruction template
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

	public T visit(DummyTriple inst) {
		return defaultValue();
	}

	/// HIR
	public T visit(Break inst) {
		return defaultValue();
	}

	public T visit(For inst) {
		return defaultValue();
	}

	public T visit(If inst) {
		return defaultValue();
	}

	public T visit(While inst) {
		return defaultValue();
	}

	public T visit(Yield inst) {
		return defaultValue();
	}

	/// IR
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
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVAddImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVAddWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVAddWordImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVAnd inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVAndImm inst) {
		return visit((DummyUnary) inst);
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
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVCvtDWord2Float inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVCvtFloat2Word inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVCvtFloat2DWord inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVDiv inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVDivWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatAdd inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatDiv inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatEq inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatLe inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatLt inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatMul inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVFloatMulAdd inst) {
		return visit((DummyTriple) inst);
	}

	@Override
	public T visit(RVFloatMulSub inst) {
		return visit((DummyTriple) inst);
	}

	@Override
	public T visit(RVFloatNeg inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVFloatNegMulAdd inst) {
		return visit((DummyTriple) inst);
	}

	@Override
	public T visit(RVFloatNegMulSub inst) {
		return visit((DummyTriple) inst);
	}

	@Override
	public T visit(RVFloatSub inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVLoadAddress inst) {
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
	public T visit(RVLoadImm inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVLoadWord inst) {
		return visit((RVLoad) inst);
	}

	@Override
	public T visit(RVMul inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVMulWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVOr inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVOrImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVRem inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVRemWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVSetLessThan inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftLeft inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftLeftImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVShiftLeftWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftLeftWordImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVShiftRightArith inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftRightArithImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVShiftRightArithWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftRightArithWordImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVShiftRightLogical inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftRightLogicalImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVShiftRightLogicalWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVShiftRightLogicalWordImm inst) {
		return visit((DummyUnary) inst);
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
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVSubWord inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVTailCall inst) {
		return visit((AbstractCall) inst);
	}

	@Override
	public T visit(RVXor inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(RVParallelMove inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVRegPlaceholder inst) {
		return defaultValue();
	}

	@Override
	public T visit(RVXorImm inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(RVMoveWord2Float inst) {
		return visit((DummyUnary) inst);
	}
}