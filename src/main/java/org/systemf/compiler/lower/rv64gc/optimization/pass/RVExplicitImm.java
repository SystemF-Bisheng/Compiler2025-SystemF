package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt32;
import org.systemf.compiler.ir.value.constant.ConstantInt64;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyTriple;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.lower.rv64gc.instruction.*;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.value.RVZero;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.SaturationArithmetic;
import org.systemf.compiler.util.TriFunction;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Optional;

public enum RVExplicitImm implements RVOptPass {
	INSTANCE;

	@Override
	public boolean run(RVModule rvModule) {
		return new RVExplicitImmContext(rvModule.module()).run();
	}

	private static class RVExplicitImmContext extends InstructionVisitorBase<Boolean> {
		private static final int IMM_WIDTH = 12;
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private ListIterator<Instruction> iterator;

		public RVExplicitImmContext(Module module) {
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
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			var res = module.getFunctions().values().stream().map(this::processFunction)
					.reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(module);
			return res;
		}

		@Override
		protected Boolean defaultValue() {
			return false;
		}

		private Instruction insertBefore(Instruction... newInst) {
			return CodeMotionHelper.insertBefore(iterator, newInst);
		}

		private String newName(String name) {
			return module.getNonConflictName(name);
		}

		private Value loadImm(Constant constant) {
			return switch (constant) {
				case ConstantInt32 constI32 -> {
					var value = (int) constI32.value;
					if (value == 0) yield RVZero.INSTANCE;
					yield (Value) insertBefore(new RVLoadImm(value, newName("imm")));
				}
				case ConstantInt64 constantI64 -> {
					var value = constantI64.value;
					if (value == 0) yield RVZero.INSTANCE;
					yield (Value) insertBefore(new RVLoadImm(constantI64.value, newName("imm")));
				}
				case ConstantFloat constantFloat -> {
					var intRep = new RVLoadImm(Float.floatToIntBits((float) constantFloat.value), newName("fImmInt"));
					yield (Value) insertBefore(intRep, new RVMoveWord2Float(newName("fImm"), intRep));
				}
				case null, default -> throw new UnsupportedOperationException();
			};
		}

		private Optional<Value> handleValue(Value value) {
			if (value instanceof Constant constant) return Optional.of(loadImm(constant));
			return Optional.empty();
		}

		@Override
		public Boolean visit(DummyBinary inst) {
			var xOpt = handleValue(inst.getX());
			xOpt.ifPresent(inst::setX);
			var yOpt = handleValue(inst.getY());
			yOpt.ifPresent(inst::setY);
			return xOpt.isPresent() || yOpt.isPresent();
		}

		@Override
		public Boolean visit(DummyUnary inst) {
			var xOpt = handleValue(inst.getX());
			xOpt.ifPresent(inst::setX);
			return xOpt.isPresent();
		}

		@Override
		public Boolean visit(DummyTriple inst) {
			var xOpt = handleValue(inst.getX());
			xOpt.ifPresent(inst::setX);
			var yOpt = handleValue(inst.getY());
			yOpt.ifPresent(inst::setY);
			var zOpt = handleValue(inst.getZ());
			zOpt.ifPresent(inst::setZ);
			return xOpt.isPresent() || yOpt.isPresent() || zOpt.isPresent();
		}

		private void replaceInstruction(Instruction oldInst, Instruction newInst) {
			insertBefore(newInst);
			iterator.remove();
			if (oldInst instanceof Value oldValue) oldValue.replaceAllUsage((Value) newInst);
			oldInst.unregister();
		}

		private boolean handleImm(DummyBinary inst, TriFunction<String, Value, Long, Instruction> immProducer) {
			var y = inst.getY();
			if (!ValueUtil.isConstantInt(y)) return false;
			var yVal = ValueUtil.getConstantInt(y);
			if (SaturationArithmetic.isOverflow(yVal, IMM_WIDTH)) return false;
			var newInst = immProducer.apply(inst.getName(), inst.getX(), yVal);
			replaceInstruction(inst, newInst);
			return true;
		}

		@Override
		public Boolean visit(RVAdd inst) {
			if (handleImm(inst, RVAddImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVAddWord inst) {
			if (handleImm(inst, RVAddWordImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVAnd inst) {
			if (handleImm(inst, RVAndImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVOr inst) {
			if (handleImm(inst, RVOrImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVShiftLeft inst) {
			if (handleImm(inst, RVShiftLeftImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVShiftLeftWord inst) {
			if (handleImm(inst, RVShiftLeftWordImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVShiftRightArith inst) {
			if (handleImm(inst, RVShiftRightArithImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVShiftRightArithWord inst) {
			if (handleImm(inst, RVShiftRightArithWordImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVShiftRightLogical inst) {
			if (handleImm(inst, RVShiftRightLogicalImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVShiftRightLogicalWord inst) {
			if (handleImm(inst, RVShiftRightLogicalWordImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVXor inst) {
			if (handleImm(inst, RVXorImm::new)) return true;
			return super.visit(inst);
		}

		@Override
		public Boolean visit(RVStore inst) {
			var offset = inst.offset;
			if (!SaturationArithmetic.isOverflow(offset, IMM_WIDTH)) return false;
			var offsetVal = new RVLoadImm(offset, newName("storeOffset"));
			var newDest = new RVAdd(newName("storeDest"), inst.getDest(), offsetVal);
			insertBefore(offsetVal, newDest);
			inst.setDest(newDest);
			inst.offset = 0;
			return true;
		}

		@Override
		public Boolean visit(RVLoad inst) {
			var offset = inst.offset;
			if (!SaturationArithmetic.isOverflow(offset, IMM_WIDTH)) return false;
			var offsetVal = new RVLoadImm(offset, newName("loadOffset"));
			var newPtr = new RVAdd(newName("loadPtr"), inst.getPointer(), offsetVal);
			insertBefore(offsetVal, newPtr);
			inst.setPointer(newPtr);
			inst.offset = 0;
			return true;
		}

		@Override
		public Boolean visit(RVParallelMove inst) {
			var oldMoves = new HashMap<>(inst.getMoves());
			var res = false;
			for (var entry : oldMoves.entrySet()) {
				var to = entry.getKey();
				var from = entry.getValue();
				var newOpt = handleValue(from);
				newOpt.ifPresent(newFrom -> inst.setMove(to, newFrom));
				res |= newOpt.isPresent();
			}
			return res;
		}

		@Override
		public Boolean visit(AbstractCall inst) {
			var args = inst.getArgs();
			var res = false;
			for (int i = 0; i < args.length; i++) {
				var newArgOpt = handleValue(args[i]);
				var finalI = i;
				newArgOpt.ifPresent(newArg -> args[finalI] = newArg);
				res |= newArgOpt.isPresent();
			}
			if (res) inst.setArgs(args);
			return res;
		}

		@Override
		public Boolean visit(Ret inst) {
			var newRetOpt = handleValue(inst.getReturnValue());
			newRetOpt.ifPresent(inst::setReturnValue);
			return newRetOpt.isPresent();
		}
	}
}
