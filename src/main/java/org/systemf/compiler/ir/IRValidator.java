package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.ir.value.instruction.terminal.Unreachable;


public class IRValidator extends InstructionVisitorBase<Boolean> {
	private StringBuilder errorMessage = new StringBuilder();
	private Type retType;

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
		clearErrorMessage();
		boolean valid = true;
		if (module.getFunction("main") == null) {
			addErrorInfo("Module must have a main function.");
			valid = false;
		}
		for (var func : module.getFunctions().values()) valid &= check(func);
		return valid;
	}

	public boolean check(Function function) {
		boolean valid = true;
		var entry = function.getEntryBlock();
		if (function.getEntryBlock() == null) {
			addErrorInfo("Function " + function.getName() + " must have an entry block.");
			valid = false;
		}
		var blocks = function.getBlocks();
		if (!blocks.contains(entry)) {
			addErrorInfo("Function " + function.getName() + " have an entry block that doesn't belong to it.");
			valid = false;
		}
		retType = function.getReturnType();
		for (var block : blocks) valid &= check(block);
		return valid;
	}

	public boolean check(BasicBlock block) {
		boolean valid = true;

		var instructions = block.instructions;
		if (instructions.isEmpty()) {
			addErrorInfo("Block " + block.getName() + " must have at least one instruction.");
			valid = false;
		}

		if (block.getTerminator() == null && !(block.getLastInstruction() instanceof Unreachable)) {
			addErrorInfo("Block " + block.getName() + " must have a terminator.");
			valid = false;
		}

		int terminatorCnt = 0;
		for (var inst : instructions) {
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

	@Override
	public Boolean visit(RetVoid inst) {
		if (Void.INSTANCE != retType) {
			addErrorInfo(String.format("RetVoid: Return type %s is not void", retType));
			return false;
		}
		return true;
	}

	@Override
	public Boolean visit(Ret inst) {
		var valueType = inst.getReturnValue().getType();
		if (!valueType.convertibleTo(retType)) {
			addErrorInfo(String.format("Ret: Return value of type %s is not convertible to return type %s", valueType,
					retType));
			return false;
		}
		return true;
	}
}