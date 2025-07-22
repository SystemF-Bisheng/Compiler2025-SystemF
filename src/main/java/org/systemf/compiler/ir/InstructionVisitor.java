package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
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

public interface InstructionVisitor<T> {
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
    T visit(FpToSi inst);
    T visit(SiToFp inst);

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
}