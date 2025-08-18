package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FAdd;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FMul;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FNeg;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FSub;
import org.systemf.compiler.query.QueryManager;

import java.util.Optional;

public enum MergeFMA implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MergeFMAContext(module).run();
	}

	private static class MergeFMAContext extends InstructionVisitorBase<Boolean> {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private IRBuilder builder;

		public MergeFMAContext(Module module) {
			this.module = module;
		}

		private Optional<FloatContractInfo> checkContract(Value a, Value b) {
			boolean neg = false;
			while (a instanceof FNeg aNeg) {
				neg = !neg;
				a = aNeg.getX();
			}
			if (!(a instanceof FMul aMul)) return Optional.empty();
			var x = aMul.getX();
			var y = aMul.getY();
			while (x instanceof FNeg xNeg) {
				neg = !neg;
				x = xNeg.getX();
			}
			while (y instanceof FNeg yNeg) {
				neg = !neg;
				y = yNeg.getX();
			}
			return Optional.of(new FloatContractInfo(x, y, b, neg));
		}

		@Override
		protected Boolean defaultValue() {
			return false;
		}

		@Override
		public Boolean visit(FAdd inst) {
			var name = inst.getName();
			var x = inst.getX();
			var y = inst.getY();
			return checkContract(x, y).or(() -> checkContract(y, x)).map(contract -> {
				Value newVal;
				if (contract.neg()) newVal = builder.buildFNegMulSub(contract.x, contract.y, contract.z, name);
				else newVal = builder.buildFMulAdd(contract.x, contract.y, contract.z, name);
				inst.replaceAllUsage(newVal);
				return true;
			}).orElse(false);
		}

		@Override
		public Boolean visit(FSub inst) {
			var name = inst.getName();
			var x = inst.getX();
			var y = inst.getY();
			return checkContract(x, y).map(contract -> {
				Value newVal;
				if (contract.neg()) newVal = builder.buildFNegMulAdd(contract.x, contract.y, contract.z, name);
				else newVal = builder.buildFMulSub(contract.x, contract.y, contract.z, name);
				inst.replaceAllUsage(newVal);
				return true;
			}).or(() -> checkContract(y, x).map(contract -> {
				Value newVal;
				if (contract.neg()) newVal = builder.buildFMulAdd(contract.x, contract.y, contract.z, name);
				else newVal = builder.buildFNegMulSub(contract.x, contract.y, contract.z, name);
				inst.replaceAllUsage(newVal);
				return true;
			})).orElse(false);
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (var iter = block.instructions.listIterator(); iter.hasNext(); ) {
				var inst = iter.next();
				builder.setPosition(iter);
				res |= inst.accept(this);
			}
			return res;
		}

		private boolean processFunction(Function function) {
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = module.getFunctions().values().stream().map(this::processFunction)
						.reduce(false, (a, b) -> a || b);
				if (res) query.invalidateAllAttributes(module);
				return res;
			}
		}

		private record FloatContractInfo(Value x, Value y, Value z, boolean neg) {
		}
	}
}
