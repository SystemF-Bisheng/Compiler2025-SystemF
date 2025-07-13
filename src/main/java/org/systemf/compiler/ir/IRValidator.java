package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
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
		for (var func : module.getFunctions().values()) valid &= check(func);
		return valid;
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

	@Override
	public Boolean visit(AbstractCall inst) {
		boolean valid = true;
		var parameterTypes = TypeUtil.getParameterTypes(inst.getFunction().getType());
		var args = inst.getArgs();
		if (parameterTypes.length != args.length) {
			addErrorInfo(String.format("Call \"%s\" has an illegal number of parameters, expected %d, given %d.", inst,
					parameterTypes.length, args.length));
			valid = false;
		}
		for (int i = 0; i < parameterTypes.length; ++i) {
			if (!args[i].getType().convertibleTo(parameterTypes[i])) {
				addErrorInfo(
						String.format("The %d th arg of call \"%s\" has an illegal type, expected %s, given %s.", i + 1,
								inst, parameterTypes[i], args[i].getType()));
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public Boolean visit(Store inst) {
		var srcType = inst.getSrc().getType();
		var destType = TypeUtil.getElementType(inst.getDest().getType());
		if (!(srcType.convertibleTo(destType))) {
			addErrorInfo(String.format("Store: Src type %s isn't convertible to the element type %s of dest", srcType,
					destType));
			return false;
		}
		return true;
	}
}