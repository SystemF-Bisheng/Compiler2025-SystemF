package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.AllocationSite;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record IndexVarianceAnalysisResult(Map<AllocationSite, List<Integer>> variance) {
	public List<Integer> variance(AllocationSite alloc) {
		return Collections.unmodifiableList(variance.getOrDefault(alloc, Collections.emptyList()));
	}
}
