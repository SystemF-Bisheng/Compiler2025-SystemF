package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

public record PointerAnalysisResult(HashMap<Value, HashSet<Value>> pointTo, HashMap<Value, HashSet<Value>> pointed) {
	public HashSet<Value> pointTo(Value value) {
		var res = pointTo.get(value);
		if (res == null) throw new NoSuchElementException("No such value: " + value);
		return res;
	}

	public HashSet<Value> pointedBy(Value value) {
		var res = pointed.get(value);
		if (res == null) throw new NoSuchElementException("No such value: " + value);
		return res;
	}
}