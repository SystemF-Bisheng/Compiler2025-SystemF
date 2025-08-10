package org.systemf.compiler.ir;

import org.systemf.compiler.hir.value.instruction.nonterminal.scf.Break;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.For;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.If;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.While;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.Yield;
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
import org.systemf.compiler.lower.rv64gc.instruction.RVStoreDWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVStoreFloat;
import org.systemf.compiler.lower.rv64gc.instruction.RVStoreWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVSub;
import org.systemf.compiler.lower.rv64gc.instruction.RVSubWord;
import org.systemf.compiler.lower.rv64gc.instruction.RVTailCall;
import org.systemf.compiler.lower.rv64gc.instruction.RVXor;
import org.systemf.compiler.lower.rv64gc.instruction.RVXorImm;

public interface InstructionVisitor<T> {
	/// HIR
	T visit(Break inst);

	T visit(For inst);

	T visit(If inst);

	T visit(While inst);

	T visit(Yield inst);

	/// IR
	// integer arithmetic
	T visit(Add inst);

	T visit(Sub inst);

	T visit(Mul inst);

	T visit(SDiv inst);

	T visit(SRem inst);

	T visit(ICmp inst);

	// float arithmetic
	T visit(FAdd inst);

	T visit(FSub inst);

	T visit(FMul inst);

	T visit(FDiv inst);

	T visit(FNeg inst);

	T visit(FCmp inst);

	// bitwise
	T visit(And inst);

	T visit(Or inst);

	T visit(Xor inst);

	T visit(Shl inst);

	T visit(LShr inst);

	T visit(AShr inst);

	// conversion
	T visit(PtrCast inst);

	T visit(FpToSi32 inst);

	T visit(Si32ToFp inst);

	T visit(Si32ToSi64 inst);

	T visit(Si64ToSi32 inst);

	// call
	T visit(Call inst);

	T visit(CallVoid inst);

	// memory
	T visit(Alloca inst);

	T visit(GetPtr inst);

	T visit(Load inst);

	T visit(Store inst);

	// miscellaneous
	T visit(Unreachable inst);

	T visit(Phi inst);

	// terminal
	T visit(Br inst);

	T visit(CondBr inst);

	T visit(Ret inst);

	T visit(RetVoid inst);

	/// RV64GC
	T visit(RVAdd inst);

	T visit(RVAddImm inst);

	T visit(RVAddWord inst);

	T visit(RVAddWordImm inst);

	T visit(RVAnd inst);

	T visit(RVAndImm inst);

	T visit(RVBranchEq inst);

	T visit(RVBranchLess inst);

	T visit(RVCvtWord2Float inst);

	T visit(RVCvtDWord2Float inst);

	T visit(RVCvtFloat2Word inst);

	T visit(RVCvtFloat2DWord inst);

	T visit(RVDiv inst);

	T visit(RVDivWord inst);

	T visit(RVFloatAdd inst);

	T visit(RVFloatDiv inst);

	T visit(RVFloatEq inst);

	T visit(RVFloatLe inst);

	T visit(RVFloatLt inst);

	T visit(RVFloatMul inst);

	T visit(RVFloatMulAdd inst);

	T visit(RVFloatMulSub inst);

	T visit(RVFloatNeg inst);

	T visit(RVFloatNegMulAdd inst);

	T visit(RVFloatNegMulSub inst);

	T visit(RVFloatSub inst);

	T visit(RVLoadAddress inst);

	T visit(RVLoadDWord inst);

	T visit(RVLoadFloat inst);

	T visit(RVLoadImm inst);

	T visit(RVLoadWord inst);

	T visit(RVMoveWord2Float inst);

	T visit(RVMul inst);

	T visit(RVMulWord inst);

	T visit(RVOr inst);

	T visit(RVOrImm inst);

	T visit(RVParallelMove inst);

	T visit(RVRegPlaceholder inst);

	T visit(RVRem inst);

	T visit(RVRemWord inst);

	T visit(RVSetLessThan inst);

	T visit(RVShiftLeft inst);

	T visit(RVShiftLeftImm inst);

	T visit(RVShiftLeftWord inst);

	T visit(RVShiftLeftWordImm inst);

	T visit(RVShiftRightArith inst);

	T visit(RVShiftRightArithImm inst);

	T visit(RVShiftRightArithWord inst);

	T visit(RVShiftRightArithWordImm inst);

	T visit(RVShiftRightLogical inst);

	T visit(RVShiftRightLogicalImm inst);

	T visit(RVShiftRightLogicalWord inst);

	T visit(RVShiftRightLogicalWordImm inst);

	T visit(RVStoreDWord inst);

	T visit(RVStoreFloat inst);

	T visit(RVStoreWord inst);

	T visit(RVSub inst);

	T visit(RVSubWord inst);

	T visit(RVTailCall inst);

	T visit(RVXor inst);

	T visit(RVXorImm inst);
}