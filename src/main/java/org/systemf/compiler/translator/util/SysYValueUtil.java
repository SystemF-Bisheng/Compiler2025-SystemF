package org.systemf.compiler.translator.util;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.semantic.type.SysYArray;
import org.systemf.compiler.semantic.type.SysYFloat;
import org.systemf.compiler.semantic.type.SysYInt;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.util.Either;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SysYValueUtil {
	private final IRBuilder builder;

	public SysYValueUtil(IRBuilder builder) {
		this.builder = builder;
	}

	public Value convertTo(Value v, SysYType from, SysYType to) {
		if (Objects.equals(from, to)) return v;
		if (!from.convertibleTo(to)) throw new IllegalArgumentException("Cannot convert " + from + " to " + to);
		if (from == SysYInt.INT && to == SysYFloat.FLOAT) return builder.buildOrFoldSiToFp(v, "iToF");
		if (from == SysYFloat.FLOAT && to == SysYInt.INT) return builder.buildOrFoldFpToSi(v, "fToI");
		return v;
	}

	public Constant constConvertTo(Constant v, SysYType from, SysYType to) {
		if (Objects.equals(from, to)) return v;
		if (!from.convertibleTo(to)) throw new IllegalArgumentException("Cannot convert " + from + " to " + to);
		if (from == SysYInt.INT && to == SysYFloat.FLOAT) return builder.folder.tryFoldSiToFp(v).orElseThrow();
		if (from == SysYFloat.FLOAT && to == SysYInt.INT) return builder.folder.tryFoldFpToSi(v).orElseThrow();
		return v;
	}

	private Sized memberIRType(SysYType type) {
		if (type == SysYInt.INT) return builder.buildI32Type();
		if (type == SysYFloat.FLOAT) return builder.buildFloatType();
		if (type instanceof SysYArray(SysYType element, long length))
			return builder.buildArrayType(memberIRType(element), Math.toIntExact(length));
		throw new IllegalArgumentException("Invalid member type " + type);
	}

	public Constant aggregateDefaultValue(SysYType type) {
		if (type == SysYInt.INT) return builder.buildConstantInt(0);
		if (type == SysYFloat.FLOAT) return builder.buildConstantFloat(0);
		if (type instanceof SysYArray(SysYType element, long length))
			return builder.buildConstantArray(memberIRType(element), Math.toIntExact(length));
		throw new IllegalArgumentException("Invalid aggregate type " + type);
	}

	public Constant aggregateConstant(SysYType type, List<Constant> content) {
		if (type == SysYInt.INT) return content.getFirst();
		if (type == SysYFloat.FLOAT) return content.getFirst();
		if (type instanceof SysYArray)
			return builder.buildConstantArray((Sized) content.getFirst().getType(), content.toArray(new Constant[0]));
		throw new IllegalArgumentException("Invalid aggregate type " + type);
	}

	public Consumer<Value> toConsumer(Either<Constant, Consumer<Value>> init) {
		if (init.isA()) {
			var constant = init.getA();
			return v -> builder.buildStore(constant, v);
		} else return init.getB();
	}
}