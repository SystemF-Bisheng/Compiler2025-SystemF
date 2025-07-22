package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;

public class ConcreteArray extends DummyArray {
	private final Constant[] content;

	public ConcreteArray(Sized elementType, Constant... content) {
		super(elementType, content.length);
		Arrays.stream(content).forEach(this::assertElement);
		this.content = Arrays.copyOf(content, content.length);
	}

	@Override
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

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		var otherArray = (ConcreteArray) other;
		for (int i = 0; i < content.length; ++i)
			if (!ValueUtil.trivialInterchangeable(content[i], otherArray.content[i])) return false;
		return true;
	}
}