package org.systemf.compiler.lower.rv64gc.allocate.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.PotentialSequential;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.lower.rv64gc.allocate.util.RVRegUtil;
import org.systemf.compiler.lower.rv64gc.analysis.RVLiveRangeAnalysisResult;
import org.systemf.compiler.lower.rv64gc.instruction.RVParallelMove;
import org.systemf.compiler.lower.rv64gc.instruction.RVRegPlaceholder;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;
import org.systemf.compiler.query.QueryManager;

import java.util.*;

public enum RVInBlockSchedule {
	INSTANCE;

	public void run(RVModule rvModule) {
		new RVInBlockScheduleContext(rvModule.module()).run();
	}

	private static class RVInBlockScheduleContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private RVLiveRangeAnalysisResult liveRange;

		public RVInBlockScheduleContext(Module module) {
			this.module = module;
		}

		public void processBlock(BasicBlock block) {
			var input = new HashSet<>(liveRange.aliveBefore(block.getFirstInstruction()));
			var endInst = block.instructions.reversed().stream()
					.takeWhile(inst -> inst instanceof Terminal || inst instanceof RVParallelMove).reduce((_, y) -> y)
					.orElseThrow();
			var output = liveRange.aliveBefore(endInst);

			var head = new ArrayList<Instruction>();
			var body = new ArrayList<Instruction>();
			List<Instruction> tail;
			var instructions = block.instructions;
			while (true) {
				var inst = instructions.getFirst();
				if (inst == endInst) {
					tail = new ArrayList<>(instructions);
					instructions.clear();
					break;
				}

				instructions.removeFirst();
				if (inst instanceof RVRegPlaceholder reg) {
					head.add(inst);
					input.add(reg);
				} else body.add(inst);
			}

			var newBody = new InstructionScheduler(input, output, body, RVRegUtil.AVAILABLE_CNT).schedule();
			instructions.addAll(head);
			instructions.addAll(newBody);
			instructions.addAll(tail);
		}

		public void processFunction(Function function) {
			liveRange = query.getAttribute(function, RVLiveRangeAnalysisResult.class);
			function.getBlocks().forEach(this::processBlock);
			query.invalidateAllAttributes(function);
		}

		public void run() {
			module.getFunctions().values().forEach(this::processFunction);
			query.invalidateAllAttributes(module);
		}
	}

	private static class InstructionScheduler {
		private final List<Instruction> instructions;
		private final Map<Instruction, Integer> dfn = new HashMap<>();
		private final Map<Instruction, Set<Instruction>> in = new HashMap<>();
		private final Map<Instruction, Set<Instruction>> out = new HashMap<>();
		private final Map<Value, Integer> dependants = new HashMap<>();
		private final Map<RVRegisterType, Integer> pressure;
		private final Map<RVRegisterType, Integer> bound;
		private int dfnCnt = 0;

		public InstructionScheduler(Set<Value> input, Set<Value> output, List<Instruction> instructions,
				Map<RVRegisterType, Integer> bound) {
			this.instructions = new ArrayList<>(instructions);
			this.pressure = computePressure(input);
			this.bound = bound;
			var canElim = computeCanElim(input, output);
			for (var val : canElim) dependants.put(val, 0);
			for (var inst : instructions) {
				in.put(inst, new HashSet<>());
				out.put(inst, new HashSet<>());
			}
			init();
		}

		private static Map<RVRegisterType, Integer> computePressure(Set<Value> input) {
			var res = new EnumMap<RVRegisterType, Integer>(RVRegisterType.class);
			for (var type : RVRegisterType.values()) res.put(type, 0);
			for (var val : input) res.computeIfPresent(RVRegUtil.regType(val), (_, x) -> x + 1);
			return res;
		}

		private Set<Value> computeCanElim(Set<Value> input, Set<Value> output) {
			var res = new HashSet<Value>();
			for (var val : input) if (!output.contains(val)) res.add(val);
			for (var inst : instructions) {
				if (!(inst instanceof Value val)) continue;
				if (output.contains(val)) continue;
				res.add(val);
			}
			return res;
		}

		private void addEdge(Instruction from, Instruction to) {
			in.get(to).add(from);
			out.get(from).add(to);
		}

		private void dfs(Instruction inst) {
			var worklist = new ArrayDeque<Instruction>();
			worklist.add(inst);
			while (!worklist.isEmpty()) {
				var head = worklist.pop();
				if (dfn.containsKey(head)) continue;
				dfn.put(head, ++dfnCnt);
				in.get(head).forEach(worklist::push);
			}
		}

		private void init() {
			var local = new HashSet<>(instructions);
			Instruction lastSeq = null;
			for (var inst : instructions) {
				for (var dep : inst.getDependency()) {
					if (dep instanceof Instruction depInst && local.contains(depInst)) addEdge(depInst, inst);
					if (dep instanceof Value val) dependants.computeIfPresent(val, (_, x) -> x + 1);
				}
				if (inst instanceof PotentialSequential instSeq) {
					if (lastSeq != null) addEdge(lastSeq, instSeq);
					lastSeq = instSeq;
				}
			}

			instructions.stream().sorted(Comparator.comparingInt(inst -> out.get(inst).size())).forEach(this::dfs);
			instructions.sort(Comparator.comparingInt(dfn::get));
		}

		private boolean checkReady(Instruction inst) {
			return in.get(inst).isEmpty();
		}

		private Instruction findReduce(LinkedList<Instruction> ready, RVRegisterType type) {
			for (var iter = ready.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				var flag = false;
				for (var dep : inst.getDependency()) {
					if (!(dep instanceof Value depVal)) continue;
					if (RVRegUtil.regType(depVal) != type) continue;
					var cnt = dependants.get(depVal);
					if (cnt == null) continue;
					if (cnt == 1) {
						flag = true;
						break;
					}
				}

				if (flag) {
					iter.remove();
					return inst;
				}
			}
			return null;
		}

		private void completeInst(LinkedList<Instruction> ready, Instruction inst) {
			out.get(inst).stream().sorted(Comparator.comparingInt(dfn::get)).forEach(nxt -> {
				in.get(nxt).remove(inst);
				if (checkReady(nxt)) ready.add(nxt);
			});
			out.remove(inst);

			for (var dep : inst.getDependency()) {
				if (!(dep instanceof Value depVal)) continue;
				if (!dependants.containsKey(depVal)) continue;
				if (dependants.computeIfPresent(depVal, (_, x) -> x == 1 ? null : x - 1) == null)
					pressure.computeIfPresent(RVRegUtil.regType(depVal), (_, x) -> x - 1);
			}
		}

		public List<Instruction> schedule() {
			var res = new ArrayList<Instruction>();
			var ready = new LinkedList<Instruction>();
			for (var inst : instructions) if (checkReady(inst)) ready.add(inst);
			while (!ready.isEmpty()) {
				Instruction inst = null;
				for (var entry : pressure.entrySet()) {
					var regType = entry.getKey();
					if (bound.get(regType) - entry.getValue() < 3) {
						var toReduce = findReduce(ready, regType);
						if (toReduce != null) {
							inst = toReduce;
							break;
						}
					}
				}
				if (inst == null) inst = ready.removeFirst();

				res.add(inst);
				completeInst(ready, inst);
			}

			if (res.size() != instructions.size())
				throw new IllegalStateException("Not all instructions have been scheduled");
			return res;
		}
	}
}
