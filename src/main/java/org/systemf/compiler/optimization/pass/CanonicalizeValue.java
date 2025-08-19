package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.UnsizedArray;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Or;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Xor;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.PtrCast;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FAdd;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FCmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FMul;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.ICmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.query.QueryManager;

/**
 * Depend on: No
 * <p>
 * Applicable to: IR
 */
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

		@Override
		public Boolean visit(FAdd inst) {
			return handleBinary(inst);
		}

		@Override
		public Boolean visit(FMul inst) {
			return handleBinary(inst);
		}

		private boolean checkCondition(Value inst) {
			return inst.getDependant().stream().allMatch(dep -> dep instanceof CondBr);
		}

		private void flipAll(Value inst) {
			inst.getDependant().stream().map(dep -> (CondBr) dep).forEach(condBr -> {
				var tmp = condBr.getTrueTarget();
				condBr.setTrueTarget(condBr.getFalseTarget());
				condBr.setFalseTarget(tmp);
			});
		}

		private boolean handleCompare(DummyCompare inst) {
			return switch (inst.method) {
				case NE -> {
					if (!checkCondition(inst)) yield false;
					inst.method = CompareOp.EQ;
					flipAll(inst);
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
				case GE -> {
					if (!checkCondition(inst)) yield false;
					inst.method = CompareOp.LT;
					flipAll(inst);
					yield true;
				}
				default -> false;
			};
		}

		@Override
		public Boolean visit(PtrCast inst) {
			if (!(inst.getType().getElementType() instanceof UnsizedArray unsized)) return false;
			if (!(((Pointer) inst.getX().getType()).getElementType() instanceof Array sized)) return false;
			if (!unsized.getElementType().equals(sized.getElementType())) return false;
			inst.replaceAllUsage(inst.getX()); // Remove simply forgetful PtrCast
			return true;
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
