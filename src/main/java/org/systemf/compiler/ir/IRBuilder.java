package org.systemf.compiler.ir;

import java.util.HashSet;
import java.util.Set;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;
import org.systemf.compiler.ir.global.initializer.IGlobalInitializer;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;

/**
 * external interface. all write operations are available with only Module and IRBuilder
 */
public class IRBuilder {
	public IRBuilder(Module module) {
		if (module.isIRBuilderAttached()) {
			System.err.println(
				"warning: multiple IRBuilder attaching to one Module may cause unexpected behavior"
			);
		}
		module.attachIRBuilder();

		this.module = module;
		occupiedNames = new HashSet<>();
	}

	public GlobalDeclaration buildGlobalDeclaration(/* ... */) {
		// TODO
	}

	public IGlobalInitializer buildGlobalInitializer(/* ... */) {
		// TODO
	}

	public Function buildFunction(/* ... */) {
		// TODO
	}

	public BasicBlock buildBasicBlock(/* ... */) {
		// TODO
	}

	public Void buildVoidType(/* ... */) {
		// TODO
	}

	public I32 buildI32Type(/* ... */) {
		// TODO
	}

	public Float buildFloatType(/* ... */) {
		// TODO
	}

	public FunctionType buildFunctionType(/* ... */) {
		// TODO
	}

	public Pointer buildePointerType(/* ... */) {
		// TODO
	}

	public Array buildArrayType(/* ... */) {
		// TODO
	}

	public ConstantInt buildConstantInt(/* ... */) {
		// TODO
	}

	public ConstantFloat buildConstantFloat(/* ... */) {
		// TODO
	}

	public And buildAnd(/* ... */) {
		// TODO
	}

	// TODO: other instruction building method ...

	public void attachToBlockTail(BasicBlock block) {
		currentBlock = block;
	}

	private final Module module;
	private BasicBlock currentBlock;
	private final Set<String> occupiedNames;

	private String getNonConflictName(String originalName) {
		if (!occupiedNames.contains(originalName)) {
			occupiedNames.add(originalName);
			return originalName;
		}

		int suffix = 0;
		while (true) {
			String newName = originalName + suffix;
			if (!occupiedNames.contains(newName)) {
				occupiedNames.add(newName);
				return newName;
			}
			suffix += 1;
		}
	}
}