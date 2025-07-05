package org.systemf.compiler.ir.global;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Value;

import java.util.ArrayList;

public class Function extends DummyValue implements IGlobal, INamed {
	final private String name;
	final private ArrayList<BasicBlock> blocks;

	public Function(String name, Type returnType, Value... formalArgs) {
		super(new FunctionType(returnType, buildFunctionType(returnType, formalArgs)));
		this.name = name;
		this.blocks = new ArrayList<>();
	}

	static private FunctionType buildFunctionType(Type returnType, Value[] formalArgs) {
		Type[] formalTypes = new Type[formalArgs.length];
		for (int i = 0; i < formalTypes.length; i++) {
			formalTypes[i] = formalArgs[i].getType();
		}
		return new FunctionType(returnType, formalTypes);
	}

	public void insertBlock(BasicBlock block) {
		blocks.add(block);
	}

	public void deleteBlock(BasicBlock block) {
		blocks.remove(block);
	}

	public void deleteBlock(int index) {
		blocks.remove(index);
	}

	public BasicBlock getEntryBlock() {
		return blocks.get(0);
	}

	public int getBlockCount() {
		return blocks.size();
	}

	public BasicBlock getBlock(int index) {
		return blocks.get(index);
	}

	@Override
	public String getName() {
		return name;
	}
}