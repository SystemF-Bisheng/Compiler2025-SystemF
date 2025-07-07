package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;


public class IRValidator extends InstructionVisitorBase<Void> {
	StringBuilder errorMessage;
	boolean isValid = true;

	public IRValidator() {
		this.errorMessage = new StringBuilder();
	}

	public String getErrorMessage() {
		return errorMessage.toString();
	}

	public void addErrorInfo(String info) {
		errorMessage.append(info).append("\n");
	}

	public boolean isValid(Module module) {
		isValid = true;
		errorMessage.setLength(0);
		for (int i = 0; i < module.getGlobalDeclarationCount(); ++i) {
			 isValid(module.getGlobalDeclaration(i));
		}
		for (int i = 0; i < module.getFunctionCount(); ++i) {
			 isValid(module.getFunction(i));
		}
		return isValid;
	}

	public void isValid(GlobalDeclaration declaration) {
		if (declaration.initializer == null) {
			addErrorInfo("Local declaration " + declaration.getName() + " must have an initializer.");
			isValid = false;
		}
	}

	public void isValid(Function function) {
		if (function.getBlockCount() == 0) {
			addErrorInfo("Function " + function.getName() + " must have at least one block.");
			isValid = false;
		}
		for (int i = 0; i < function.getBlockCount(); ++i) {
			 isValid(function.getBlock(i));
		}
	}

	public void isValid(BasicBlock block) {
		if (block.getInstructionCount() == 0) {
			addErrorInfo("Block " + block.getName() + " must have at least one instruction.");
			isValid = false;
		}

		if (block.getTerminator() == null) {
			addErrorInfo("Block " + block.getName() + " must have a terminator instruction.");
			isValid = false;
		}

		for (int i = 0; i < block.getInstructionCount(); ++i) {
			 isValid(block.getInstruction(i));
		}
	}

	public void isValid(Instruction instruction) {
		instruction.accept(this);
	}

	@Override
	public Void visit(Store instruction) {
		if (!checkPointerToType(instruction.dest, instruction.src)) {
			addErrorInfo("Store instruction destination " + instruction.dest.getType().getName() +
			             " must be a pointer to the type of source " + instruction.src.getType().getName());
			isValid = false;
		}
		return null;
	}

	private boolean checkPointerToType(Value pointer, Value type) {
		if (!(pointer.getType() instanceof Pointer pointer1)) {
			return false;
		}
		return pointer1.getElementType().equals(type.getType());
	}

}