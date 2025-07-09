package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;
import org.systemf.compiler.ir.global.initializer.ArrayInitializer;
import org.systemf.compiler.ir.global.initializer.AtomicInitializer;
import org.systemf.compiler.ir.global.initializer.IGlobalInitializer;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
		Type elementType = declaration.valueType;
		while (elementType instanceof Array array) elementType = array.getElementType();
		if (!check(elementType, declaration.initializer)) {
			addErrorInfo("Local declaration " + declaration.getName() + " has an illegal initializer.");
			return false;
		}
		return true;
	}

	public boolean check(Type elementType, IGlobalInitializer initializer){
		if (initializer instanceof AtomicInitializer atomicInitializer) return elementType.equals(atomicInitializer.value().getType());
		if (initializer instanceof ArrayInitializer arrayInitializer) {
			if (arrayInitializer.elements().length == 0 || arrayInitializer.length() != arrayInitializer.elements().length){
				addErrorInfo("Initializer " + initializer + "has illegal length.");
				return false;
			}
			boolean valid = true;
			for (int i = 0; i < arrayInitializer.length(); i++) {
				valid &= check(elementType, arrayInitializer.elements()[i]);
			}
			return valid;
		}
		throw new IllegalArgumentException("Illegal initializer type.");
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
		List<Type> parameterTypes = List.of(TypeUtil.getParameterTypes(inst.getFunction().getType()));
		List<Type> args = Stream.of(inst.getArgs()).map(Value::getType).toList();
		if (!parameterTypes.equals(args)) {
			addErrorInfo("Call " + inst + " has invalid parameters.");
			return false;
		}
		return true;
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