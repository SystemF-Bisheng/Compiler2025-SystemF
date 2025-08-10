package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public enum RVRegBackupAnalysis implements AttributeProvider<RVModule, RVRegBackupAnalysisResult> {
	INSTANCE;

	@Override
	public RVRegBackupAnalysisResult getAttribute(RVModule entity) {
		return new RVRegBackupAnalysisContext(entity).run();
	}

	private static class RVRegBackupAnalysisContext {
		private final QueryManager query = QueryManager.getInstance();
		private final RVModule rvModule;
		private final Map<AbstractCall, SortedSet<RVRegister>> toSave = new HashMap<>();
		private final Map<Function, Integer> saveSize = new HashMap<>();
		private RVLiveRangeAnalysisResult liveRange;

		public RVRegBackupAnalysisContext(RVModule rvModule) {
			this.rvModule = rvModule;
		}

		private SortedSet<RVRegister> collect(AbstractCall call) {
			var aliveBefore = liveRange.aliveBefore(call);
			var aliveAfter = liveRange.aliveAfter(call);
			return aliveBefore.stream().filter(aliveAfter::contains).map(value -> RVRegUtil.positionOf(rvModule, value))
					.filter(pos -> pos instanceof RVRegister).map(pos -> (RVRegister) pos).filter(RVRegUtil::isNonSaved)
					.collect(Collectors.toCollection(TreeSet::new));
		}

		private void collect(Function function) {
			this.liveRange = query.getAttribute(function, RVLiveRangeAnalysisResult.class);
			saveSize.put(function, function.allInstructions().filter(inst -> inst instanceof AbstractCall)
					.map(inst -> (AbstractCall) inst).mapToInt(call -> {
						var save = collect(call);
						toSave.put(call, save);
						return save.size();
					}).max().orElse(0));
		}

		public RVRegBackupAnalysisResult run() {
			rvModule.module().getFunctions().values().forEach(this::collect);
			return new RVRegBackupAnalysisResult(toSave, saveSize);
		}
	}
}
