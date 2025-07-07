package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.*;
import org.systemf.compiler.ir.value.instruction.terminal.*;

public interface InstructionVisitor<T> {
    // iarithmetic
    T visit(Add inst);
    T visit(Sub inst);
    T visit(Mul inst);
    T visit(SDiv inst);
    T visit(SRem inst);
    T visit(ICmp inst);

    // farithmetic
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