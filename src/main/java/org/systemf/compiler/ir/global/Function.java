package org.systemf.compiler.ir.global;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Function extends DummyValue implements IFunction {
	final private String name;
	final private LinkedHashSet<BasicBlock> blocks = new LinkedHashSet<>();
	private BasicBlock entryBlock;
	private final Type returnType;
	final private Parameter[] formalArgs;

	public Function(String name, Type returnType, Parameter... formalArgs) {
		super(buildFunctionType(returnType, formalArgs));
		this.name = name;
		this.returnType = returnType;
		this.formalArgs = formalArgs;
	}

	static private FunctionType buildFunctionType(Type returnType, Parameter[] formalArgs) {
		Type[] formalTypes = new Type[formalArgs.length];
		for (int i = 0; i < formalTypes.length; i++) formalTypes[i] = formalArgs[i].getType();
		return new FunctionType(returnType, formalTypes);
	}

	public void insertBlock(BasicBlock block) {
		blocks.add(block);
	}

	public void deleteBlock(BasicBlock block) {
		blocks.remove(block);
	}

	public BasicBlock getEntryBlock() {
		return entryBlock;
	}

	public void setEntryBlock(BasicBlock entryBlock) {
		this.entryBlock = entryBlock;
	}

	public Set<BasicBlock> getBlocks() {
		return Collections.unmodifiableSet(blocks);
	}

	public Parameter[] getFormalArgs() {
		return formalArgs;
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
			sb.append(" ");
			sb.append(ValueUtil.dumpIdentifier(formalArgs[i]));
		}
		sb.append(") {\n");
		for (BasicBlock block : blocks) {
			sb.append(block.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}

	public Type getReturnType() {
		return returnType;
	}
}