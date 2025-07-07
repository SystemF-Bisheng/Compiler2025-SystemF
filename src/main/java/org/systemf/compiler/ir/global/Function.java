package org.systemf.compiler.ir.global;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.Value;

import java.util.ArrayList;

public class Function extends DummyValue implements IGlobal, INamed {
	final private String name;
	final private ArrayList<BasicBlock> blocks;
	final private Value[] formalArgs;

	public Function(String name, Type returnType, Value... formalArgs) {
		super(buildFunctionType(returnType, formalArgs));
		this.name = name;
		this.formalArgs = formalArgs;
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
		return blocks.getFirst();
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("define ");
		sb.append(TypeUtil.getReturnType(type).getName());
		sb.append(" @");
		sb.append(name);
		sb.append("(");
		for (int i = 0; i < formalArgs.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(formalArgs[i].getType().getName());
			sb.append(" %");
			sb.append(ValueUtil.getValueName(formalArgs[i]));
		}
		sb.append(") {\n");
		for (BasicBlock block : blocks) {
			sb.append(block.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}
}