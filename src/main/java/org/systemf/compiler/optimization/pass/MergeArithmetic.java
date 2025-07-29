package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyFloatBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyIntBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;

import java.util.Comparator;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Depend on: No
 * <p>
 * Applicable to: IR
 */
public enum MergeArithmetic implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MergeArithmeticContext(module).run();
	}

	private static class MergeArithmeticContext extends InstructionVisitorBase<Boolean> {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private IRBuilder builder;
		private ListIterator<Instruction> iterator;

		public MergeArithmeticContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (iterator = block.instructions.listIterator(); iterator.hasNext(); ) {
				var inst = iterator.next();
				res |= inst.accept(this);
			}
			return res;
		}

		private boolean processFunction(Function function) {
			var domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			var res = function.getBlocks().stream().sorted(Comparator.comparingInt(domTree::getDfn))
					.map(this::processBlock).reduce(false, (a, b) -> a || b);
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

		@Override
		protected Boolean defaultValue() {
			return false;
		}

		private boolean handleBinary(DummyIntBinary inst, BiFunction<Long, Long, Long> func) {
			var x = inst.getX();
			var selfY = inst.getY();
			if (!ValueUtil.isConstantInt(selfY)) return false;
			if (inst.getClass() != x.getClass()) return false;
			var binaryX = (DummyBinary) x;
			var otherY = binaryX.getY();
			if (!ValueUtil.isConstantInt(otherY)) return false;
			var width = ValueUtil.getWidth(x);
			var newValue = func.apply(ValueUtil.getConstantInt(otherY), ValueUtil.getConstantInt(selfY));
			inst.setX(binaryX.getX());
			inst.setY(builder.buildConstantInt(newValue, width));
			return true;
		}

		private Optional<Value> checkIntNeg(Value value) {
			if (value instanceof Sub sub) {
				var subX = sub.getX();
				if (!ValueUtil.isConstantInt(subX)) return Optional.empty();
				if (ValueUtil.getConstantInt(subX) != 0) return Optional.empty();
				return Optional.of(sub.getY());
			}
			return Optional.empty();
		}

		@Override
		public Boolean visit(Add inst) {
			if (handleBinary(inst, Long::sum)) return true;

			var x = inst.getX();
			var y = inst.getY();
			return checkIntNeg(y).map(negY -> {
				builder.setPosition(iterator);
				var newInst = builder.buildSub(x, negY, inst.getName());
				inst.replaceAllUsage(newInst);
				return true;
			}).or(() -> checkIntNeg(x).map(negX -> {
				builder.setPosition(iterator);
				var newInst = builder.buildSub(y, negX, inst.getName());
				inst.replaceAllUsage(newInst);
				return true;
			})).orElse(false);
		}

		@Override
		public Boolean visit(Sub inst) {
			if (handleBinary(inst, Long::sum)) return true;

			var x = inst.getX();
			var y = inst.getY();
			return checkIntNeg(y).map(negY -> {
				builder.setPosition(iterator);
				var newInst = builder.buildAdd(x, negY, inst.getName());
				inst.replaceAllUsage(newInst);
				return true;
			}).orElse(false);
		}

		@Override
		public Boolean visit(Mul inst) {
			if (handleBinary(inst, (x, y) -> x * y)) return true;

			var x = inst.getX();
			var y = inst.getY();
			var checkX = checkIntNeg(x);
			var checkY = checkIntNeg(y);
			if (checkX.isEmpty() && checkY.isEmpty()) return false;

			checkX.ifPresent(inst::setX);
			checkY.ifPresent(inst::setY);
			if (checkY.isPresent() && checkX.isPresent()) return true;
			builder.setPosition(iterator);
			var newInst = builder.buildSub(builder.buildConstantInt(0, ValueUtil.getWidth(inst)), inst,
					inst.getName() + "Neg");
			inst.replaceAllUsage(newInst);
			return true;
		}

		private boolean intExtractFirstNeg(DummyIntBinary inst) {
			var x = inst.getX();
			return checkIntNeg(x).map(negX -> {
				inst.setX(negX);
				builder.setPosition(iterator);
				var newInst = builder.buildSub(builder.buildConstantInt(0, ValueUtil.getWidth(inst)), inst,
						inst.getName() + "Neg");
				inst.replaceAllUsage(newInst);
				return true;
			}).orElse(false);
		}

		@Override
		public Boolean visit(SDiv inst) {
			return intExtractFirstNeg(inst);
		}

		@Override
		public Boolean visit(SRem inst) {
			return intExtractFirstNeg(inst);
		}

		@Override
		public Boolean visit(And inst) {
			return handleBinary(inst, (x, y) -> x & y);
		}

		@Override
		public Boolean visit(Or inst) {
			return handleBinary(inst, (x, y) -> x | y);
		}

		@Override
		public Boolean visit(Xor inst) {
			return handleBinary(inst, (x, y) -> x ^ y);
		}

		@Override
		public Boolean visit(Shl inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(LShr inst) {
			return handleBinary(inst, Long::sum);
		}

		@Override
		public Boolean visit(AShr inst) {
			return handleBinary(inst, Long::sum);
		}

		private Optional<Value> checkFloatNeg(Value value) {
			if (value instanceof FNeg neg) return Optional.of(neg.getX());
			return Optional.empty();
		}

		@Override
		public Boolean visit(FAdd inst) {
			var x = inst.getX();
			var y = inst.getY();
			return checkFloatNeg(y).map(negY -> {
				builder.setPosition(iterator);
				var newInst = builder.buildFSub(x, negY, inst.getName());
				inst.replaceAllUsage(newInst);
				return true;
			}).or(() -> checkFloatNeg(x).map(negX -> {
				builder.setPosition(iterator);
				var newInst = builder.buildFSub(y, negX, inst.getName());
				inst.replaceAllUsage(newInst);
				return true;
			})).orElse(false);
		}

		@Override
		public Boolean visit(FSub inst) {
			var x = inst.getX();
			var y = inst.getY();
			return checkFloatNeg(y).map(negY -> {
				builder.setPosition(iterator);
				var newInst = builder.buildFAdd(x, negY, inst.getName());
				inst.replaceAllUsage(newInst);
				return true;
			}).orElse(false);
		}

		private boolean floatExtractTwoNeg(DummyFloatBinary inst) {
			var x = inst.getX();
			var y = inst.getY();
			return checkFloatNeg(x).flatMap(negX -> checkFloatNeg(y).map(negY -> {
				inst.setX(negX);
				inst.setY(negY);
				return true;
			})).orElse(false);
		}

		@Override
		public Boolean visit(FMul inst) {
			return floatExtractTwoNeg(inst);
		}

		@Override
		public Boolean visit(FDiv inst) {
			return floatExtractTwoNeg(inst);
		}

		@Override
		public Boolean visit(FNeg inst) {
			return checkFloatNeg(inst.getX()).map(negX -> {
				inst.replaceAllUsage(negX);
				return true;
			}).orElse(false);
		}
	}
}
