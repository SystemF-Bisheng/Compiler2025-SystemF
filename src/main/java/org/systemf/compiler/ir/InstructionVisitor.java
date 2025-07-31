package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.*;
import org.systemf.compiler.lower.rv64gc.instruction.*;

public interface InstructionVisitor<T> {
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

	T visit(RVAddWord inst);

	T visit(RVAnd inst);

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

	T visit(RVLoadDWord inst);

	T visit(RVLoadFloat inst);

	T visit(RVLoadWord inst);

	T visit(RVMul inst);

	T visit(RVMulWord inst);

	T visit(RVOr inst);

	T visit(RVParallelMove inst);

	T visit(RVRegPlaceholder inst);

	T visit(RVRem inst);

	T visit(RVRemWord inst);

	T visit(RVSetLessThan inst);

	T visit(RVShiftLeft inst);

	T visit(RVShiftLeftWord inst);

	T visit(RVShiftRightArith inst);

	T visit(RVShiftRightArithWord inst);

	T visit(RVShiftRightLogical inst);

	T visit(RVShiftRightLogicalWord inst);

	T visit(RVStoreDWord inst);

	T visit(RVStoreFloat inst);

	T visit(RVStoreWord inst);

	T visit(RVSub inst);

	T visit(RVSubWord inst);

	T visit(RVXor inst);
}