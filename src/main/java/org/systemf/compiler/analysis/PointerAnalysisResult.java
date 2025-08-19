package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.AllocationSite;
import org.systemf.compiler.ir.value.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record PointerAnalysisResult(Map<Value, Set<AllocationSite>> pointTo, Map<AllocationSite, Set<Value>> pointed) {
	public Set<AllocationSite> pointTo(Value value) {
		var res = pointTo.get(value);
		if (res == null) return Collections.emptySet();
		return Collections.unmodifiableSet(res);
	}

	public Set<Value> pointedBy(AllocationSite value) {
		var res = pointed.get(value);
		if (res == null) return Collections.emptySet();
		return Collections.unmodifiableSet(res);
	}
}