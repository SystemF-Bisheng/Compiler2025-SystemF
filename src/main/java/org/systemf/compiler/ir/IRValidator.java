package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;


public class IRValidator extends InstructionVisitorBase<Boolean> {
	private StringBuilder errorMessage = new StringBuilder();

	public String getErrorMessage() {
		return errorMessage.toString();
	}

	public void clearErrorMessage() {
		errorMessage = new StringBuilder();
	}

	private void addErrorInfo(String info) {
		errorMessage.append(info).append("\n");
	}

	public boolean check(Module module) {
		boolean valid = true;
		for (int i = 0; i < module.getGlobalDeclarationCount(); ++i) valid &= check(module.getGlobalDeclaration(i));
		for (int i = 0; i < module.getFunctionCount(); ++i) valid &= check(module.getFunction(i));
		return valid;
	}

	public boolean check(GlobalDeclaration declaration) {
		if (declaration.initializer == null) {
			addErrorInfo("Local declaration " + declaration.getName() + " must have an initializer.");
			return false;
		}
		return true;
	}

	public boolean check(Function function) {
		boolean valid = true;
		if (function.getBlockCount() == 0) {
			addErrorInfo("Function " + function.getName() + " must have at least one block.");
			valid = false;
		}
		for (int i = 0; i < function.getBlockCount(); ++i) valid &= check(function.getBlock(i));
		return valid;
	}

	public boolean check(BasicBlock block) {
		boolean valid = true;

		if (block.getInstructionCount() == 0) {
			addErrorInfo("Block " + block.getName() + " must have at least one instruction.");
			valid = false;
		}

		if (block.getTerminator() == null) {
			addErrorInfo("Block " + block.getName() + " must have a terminator.");
			valid = false;
		}

		int terminatorCnt = 0;
		for (int i = 0; i < block.getInstructionCount(); ++i) {
			var inst = block.getInstruction(i);
			valid &= check(inst);
			if (inst instanceof Terminal) ++terminatorCnt;
		}

		if (terminatorCnt > 1) {
			addErrorInfo("Block " + block.getName() + " have more than one terminators.");
			valid = false;
		}

		return valid;
	}

	public boolean check(Instruction instruction) {
		return instruction.accept(this);
	}

	@Override
	protected Boolean defaultValue() {
		return true;
	}
}