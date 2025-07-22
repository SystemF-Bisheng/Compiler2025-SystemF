package org.systemf.compiler.machine;

import java.util.List;

import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;

import org.systemf.compiler.machine.riscv.*;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantArray;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;

public enum RISCVGenerator implements EntityProvider<MachineCodeResult> {
	INSTANCE;

	@Override
	public MachineCodeResult produce() {
		var module = QueryManager.getInstance().get(Module.class);
		
		// TODO: Implement RISC-V assembly generation
		var result = new MachineCodeResult();
		var instructionVisitor = new InstructionVisitor();
		// Considering if we need more heirarchy visitors or if we define some functions instead

		throw new UnsupportedOperationException();
	}

	public class InstructionVisitor extends InstructionVisitorBase<List<MachineInstruction>> {
		// TODO: Implement RISC-V Instruction Visitor

		@Override
		public List<MachineInstruction> visit(DummyBinary dummyBinary) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(FNeg fNeg) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(FpToSi fpToSi) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(SiToFp siToFp) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Alloca alloca) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Load load) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(GetPtr getPtr) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Store store) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Ret ret) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(RetVoid retVoid) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(CondBr condBr) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Br br) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(AbstractCall abstractCall) {
			return List.of(new MachineInstruction("nop"));
		}
	}
}