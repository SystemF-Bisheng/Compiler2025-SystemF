package org.systemf.compiler.ir.global;

import org.systemf.compiler.ir.AllocationSite;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.constant.Constant;

public class GlobalVariable extends DummyValue implements IGlobal, AllocationSite {
	public final Sized valueType;
	private final String name;
	private Constant initializer;

	public GlobalVariable(String name, Sized type, Constant initializer) {
		super(new Pointer(type));
		this.name = name;
		this.valueType = type;
		setInitializer(initializer);
	}

	public GlobalVariable(String name, Sized type, Constant initializer, Type ptrType) {
		super(ptrType);
		this.name = name;
		this.valueType = type;
		setInitializer(initializer);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("@%s = global %s %s", name, valueType.getName(), initializer.toString());
	}

	public Constant getInitializer() {
		return initializer;
	}

	public void setInitializer(Constant initializer) {
		TypeUtil.assertConvertible(initializer.getType(), valueType, "Illegal initializer");
		this.initializer = initializer;
	}

	@Override
	public Sized valueType() {
		return valueType;
	}
}