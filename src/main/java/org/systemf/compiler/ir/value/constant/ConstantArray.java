package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

import java.util.Arrays;

public class ConstantArray extends DummyConstant {
	public final Sized elementType;
	public final int size;
	private final Constant[] content;

	public ConstantArray(Sized elementType, Constant... content) {
		super(new Array(content.length, elementType));
		this.elementType = elementType;
		this.size = content.length;
		Arrays.stream(content).forEach(this::assertElement);
		this.content = Arrays.copyOf(content, content.length);
	}

	public Constant[] getContent() {
		return Arrays.copyOf(content, content.length);
	}

	public Constant getContent(int index) {
		return content[index];
	}

	private void assertElement(Value value) {
		TypeUtil.assertConvertible(value.getType(), elementType, "Illegal element");
	}

	@Override
	public String toString() {
		return String.format("{%s}",
				String.join(", ", Arrays.stream(content).map(Object::toString).toArray(String[]::new)));
	}
}