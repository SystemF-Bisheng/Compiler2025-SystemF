package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.value.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public record PointerAnalysisResult(HashMap<Value, HashSet<Value>> pointTo, HashMap<Value, HashSet<Value>> pointed) {
	public Set<Value> pointTo(Value value) {
		var res = pointTo.get(value);
		if (res == null) return Collections.emptySet();
		return Collections.unmodifiableSet(res);
	}

	public Set<Value> pointedBy(Value value) {
		var res = pointed.get(value);
		if (res == null) return Collections.emptySet();
		return Collections.unmodifiableSet(res);
	}
}