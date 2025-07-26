package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Or;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Xor;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FCmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.ICmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.query.QueryManager;

public enum CanonicalizeValue implements OptPass {
	INSTANCE;

	private static final CanonicalVisitor visitor = new CanonicalVisitor();

	private boolean processBlock(BasicBlock block) {
		return block.instructions.stream().map(inst -> inst.accept(visitor)).reduce(false, (a, b) -> a || b);
	}

	private boolean processFunction(Function function) {
		var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(Module module) {
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}

	private static class CanonicalVisitor extends InstructionVisitorBase<Boolean> {
		@Override
		protected Boolean defaultValue() {
			return false;
		}

		private void switchOperand(DummyBinary inst) {
			var tmp = inst.getX();
			inst.setX(inst.getY());
			inst.setY(tmp);
		}

		private boolean handleBinary(DummyBinary inst) {
			if (inst.getX() instanceof Constant && (!(inst.getY() instanceof Constant))) {
				switchOperand(inst);
				return true;
			}
			return false;
		}

		@Override
		public Boolean visit(Add inst) {
			return handleBinary(inst);
		}

		@Override
		public Boolean visit(Mul inst) {
			return handleBinary(inst);
		}

		private boolean handleCompare(DummyCompare inst) {
			return switch (inst.method) {
				case NE -> {
					if (!(inst.getDependant().stream().allMatch(dep -> dep instanceof CondBr))) yield false;
					inst.method = CompareOp.EQ;
					inst.getDependant().stream().map(dep -> (CondBr) dep).forEach(condBr -> {
						var tmp = condBr.getTrueTarget();
						condBr.setTrueTarget(condBr.getFalseTarget());
						condBr.setFalseTarget(tmp);
					});
					yield true;
				}
				case GT -> {
					inst.method = CompareOp.LT;
					switchOperand(inst);
					yield true;
				}
				case LE -> {
					inst.method = CompareOp.GE;
					switchOperand(inst);
					yield true;
				}
				default -> false;
			};
		}

		@Override
		public Boolean visit(ICmp inst) {
			return handleCompare(inst);
		}

		@Override
		public Boolean visit(FCmp inst) {
			return handleCompare(inst);
		}

		@Override
		public Boolean visit(And inst) {
			return handleBinary(inst);
		}

		@Override
		public Boolean visit(Or inst) {
			return handleBinary(inst);
		}

		@Override
		public Boolean visit(Xor inst) {
			return handleBinary(inst);
		}
	}
}
