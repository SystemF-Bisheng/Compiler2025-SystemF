package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.position.RVPosition;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.module.register.RVBuiltInRegister;
import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;
import org.systemf.compiler.lower.rv64gc.util.RVTypeHelper;
import org.systemf.compiler.util.SaturationArithmetic;

import java.util.*;

public class RVGenerateHelper {
	public static String loadInstruction(Type type) {
		return switch (type) {
			case I32 _ -> "lw";
			case I64 _ -> "ld";
			case Float _ -> "flw";
			case null, default -> throw new UnsupportedOperationException();
		};
	}

	public static String loadInstruction(RVRegister register) {
		return switch (register.type()) {
			case INTEGER -> "ld";
			case FLOAT -> "fld";
		};
	}

	public static String storeInstruction(Type type) {
		return switch (type) {
			case I32 _ -> "sw";
			case I64 _ -> "sd";
			case Float _ -> "fsw";
			case null, default -> throw new UnsupportedOperationException();
		};
	}

	public static String storeInstruction(RVRegister register) {
		return switch (register.type()) {
			case INTEGER -> "sd";
			case FLOAT -> "fsd";
		};
	}

	public static RVRegister loadOffset(RVRegister reg, long offset, RVAsmCode out) {
		var posTmp = RVRegUtil.INTEGER_SCRATCH_REGISTER;
		out.addLine(String.format("li %s, %d", posTmp, offset));
		out.addLine(String.format("add %s, %s, %s", posTmp, reg, posTmp));
		return posTmp;
	}

	public static RVRegister loadFpOffset(long offset, RVAsmCode out) {
		return loadOffset(RVRegUtil.FRAME_POINTER, offset, out);
	}

	public static RVRegister loadSpOffset(long offset, RVAsmCode out) {
		return loadOffset(RVRegUtil.STACK_POINTER, offset, out);
	}

	public static RVPosition positionOf(RVModule rvModule, Value value) {
		if (value instanceof RVBuiltInRegister builtIn) return builtIn.position();
		return rvModule.position().get(value);
	}

	public static RVTypedPosition typedPositionOf(RVModule rvModule, Value value) {
		var pos = positionOf(rvModule, value);
		if (pos == null) return null;
		return new RVTypedPosition((Sized) value.getType(), pos);
	}

	public static void subtract(RVRegister dest, RVRegister src, long size, RVAsmCode out) {
		if (size == 0) return;
		if (SaturationArithmetic.isOverflow(-size, RVRegUtil.IMM_WIDTH)) {
			out.addLine(String.format("li %s, %d", RVRegUtil.INTEGER_SCRATCH_REGISTER, size));
			out.addLine(String.format("sub %s, %s, %s", dest, src, RVRegUtil.INTEGER_SCRATCH_REGISTER));
		} else out.addLine(String.format("addi %s, %s, %d", dest, src, -size));
	}

	public static void subtractSp(RVRegister dest, long size, RVAsmCode out) {
		subtract(dest, RVRegUtil.STACK_POINTER, size, out);
	}

	public static void subtractSp(long size, RVAsmCode out) {
		subtract(RVRegUtil.STACK_POINTER, RVRegUtil.STACK_POINTER, size, out);
	}

	public static void add(RVRegister dest, RVRegister src, long size, RVAsmCode out) {
		if (size == 0) return;
		if (SaturationArithmetic.isOverflow(size, RVRegUtil.IMM_WIDTH)) {
			out.addLine(String.format("li %s, %d", RVRegUtil.INTEGER_SCRATCH_REGISTER, size));
			out.addLine(String.format("add %s, %s, %s", dest, src, RVRegUtil.INTEGER_SCRATCH_REGISTER));
		} else out.addLine(String.format("addi %s, %s, %d", dest, src, size));
	}

	public static void addSp(RVRegister dest, long size, RVAsmCode out) {
		add(dest, RVRegUtil.STACK_POINTER, size, out);
	}

	public static void addSp(long size, RVAsmCode out) {
		add(RVRegUtil.STACK_POINTER, RVRegUtil.STACK_POINTER, size, out);
	}

	public static void performOffset(String inst, RVRegister base, RVRegister target, long offset, RVAsmCode out) {
		if (SaturationArithmetic.isOverflow(offset, RVRegUtil.IMM_WIDTH)) {
			var posTmp = RVGenerateHelper.loadOffset(base, offset, out);
			out.addLine(String.format("%s %s, 0(%s)", inst, target, posTmp));
		} else out.addLine(String.format("%s %s, %d(%s)", inst, target, offset, base));
	}

	public static void loadFrom(RVRegister base, RVTypedPosition to, long offset, RVCacheManager cacheManager,
			RVAsmCode out) {
		var storeTo = cacheManager.allocForStore(to, out);
		var loadInst = RVGenerateHelper.loadInstruction(to.type());
		performOffset(loadInst, base, storeTo, offset, out);
		cacheManager.unlock(storeTo);
	}

	public static void loadFromSp(RVTypedPosition to, long offset, RVCacheManager cacheManager, RVAsmCode out) {
		loadFrom(RVRegUtil.STACK_POINTER, to, offset, cacheManager, out);
	}

	public static void storeTo(RVRegister base, RVTypedPosition from, long offset, RVCacheManager cacheManager,
			RVAsmCode out) {
		var loadFrom = cacheManager.load(from, out);
		var storeInst = RVGenerateHelper.storeInstruction(from.type());
		performOffset(storeInst, base, loadFrom, offset, out);
		cacheManager.unlock(loadFrom);
	}

	public static void storeToSp(RVTypedPosition from, long offset, RVCacheManager cacheManager, RVAsmCode out) {
		storeTo(RVRegUtil.STACK_POINTER, from, offset, cacheManager, out);
	}

	public static void moveRegister(RVRegister to, RVRegister from, RVAsmCode out) {
		if (to.equals(from)) return;
		if (to.type() != from.type()) throw new IllegalArgumentException();
		switch (to.type()) {
			case INTEGER -> out.addLine(String.format("mv %s, %s", to, from));
			case FLOAT -> out.addLine(String.format("fsgnj.d %s, %s, %s", to, from, from));
		}
	}

	public static void move(RVTypedPosition to, RVTypedPosition from, RVCacheManager cacheManager, RVAsmCode out) {
		var loadFrom = cacheManager.load(from, out);
		cacheManager.unlock(loadFrom);
		var storeTo = cacheManager.allocForStore(to, out);
		moveRegister(storeTo, loadFrom, out);
		cacheManager.unlock(storeTo);
	}

	public static void parallelMove(Map<RVTypedPosition, RVTypedPosition> moves, RVCacheManager cacheManager,
			RVAsmCode out) {
		var inMap = new HashMap<RVPosition, RVTypedPosition>();
		var outMap = new HashMap<RVPosition, Integer>();
		for (var entry : moves.entrySet()) {
			var to = entry.getKey();
			var from = entry.getValue();
			var toPos = to.position();
			var fromPos = from.position();
			if (toPos.equals(fromPos)) { // Self-loop
				if (toPos instanceof RVRegister) continue;
				if (RVTypeHelper.sizeOf(from.type()) >= RVTypeHelper.sizeOf(to.type())) continue;
				move(to, from, cacheManager, out); // Move for bit-expansion if size(from) < size(to)
				continue;
			}
			inMap.put(toPos, to);
			outMap.compute(fromPos, (_, cnt) -> cnt == null ? 1 : cnt + 1);
		}
		while (!inMap.isEmpty()) {
			var freeOutOpt = inMap.keySet().stream().filter(pos -> !outMap.containsKey(pos)).findFirst();
			if (freeOutOpt.isPresent()) {
				var freeIn = freeOutOpt.get();
				var to = inMap.get(freeIn);
				inMap.remove(freeIn);
				var from = moves.get(to);
				move(to, from, cacheManager, out);
				var fromPos = from.position();
				if (outMap.computeIfPresent(fromPos, (_, cnt) -> cnt == 1 ? null : cnt - 1) == null &&
				    fromPos instanceof RVRegister fromReg) cacheManager.unlock(fromReg);
				moves.remove(to);
			} else { // Disconnect a loop: copy one node and redirect all out edges
				var toDis = inMap.keySet().iterator().next();
				var toDisTyped = inMap.get(toDis);
				var toDisLoad = cacheManager.load(toDisTyped, out);
				cacheManager.unlock(toDisLoad);
				var toDisCopy = cacheManager.alloc(RVRegUtil.regType(toDisTyped.type()), out);
				moveRegister(toDisCopy, toDisLoad, out);

				for (var entry : moves.entrySet()) {
					var value = entry.getValue();
					if (!toDis.equals(value.position())) continue;
					entry.setValue(value.with(toDisCopy));
				}
				outMap.put(toDisCopy, outMap.get(toDis));
				outMap.remove(toDis);
			}
		}
	}

	public static void loadArgs(RVModule rvModule, Parameter[] params, RVCacheManager cacheManager, RVAsmCode out) {
		var typeCnt = new EnumMap<RVRegisterType, Integer>(RVRegisterType.class);
		for (var type : RVRegisterType.values()) typeCnt.put(type, 0);

		var inStack = new ArrayList<Parameter>();
		var parMove = new HashMap<RVTypedPosition, RVTypedPosition>();
		for (var param : params) {
			var pos = typedPositionOf(rvModule, param);
			var regType = RVRegUtil.regType(param);
			var id = typeCnt.get(regType);
			typeCnt.computeIfPresent(regType, (_, cnt) -> cnt + 1);

			if (pos == null) continue;
			var paramRegs = RVRegUtil.PARAMETER_REGISTERS.get(regType);
			if (paramRegs.size() <= id) {
				inStack.add(param);
				continue;
			}
			parMove.put(pos, pos.with(new RVRegister(regType, paramRegs.get(id))));
		}
		parallelMove(parMove, cacheManager, out);

		long offset = 0;
		for (var param : inStack) {
			var pos = Objects.requireNonNull(typedPositionOf(rvModule, param));
			loadFromSp(pos, offset, cacheManager, out);
			offset += RVRegUtil.REG_SIZE;
		}
	}

	public static void restoreRegisters(List<RVRegister> registers, RVAsmCode out) {
		long offset = 0;
		for (var reg : registers) {
			offset -= RVRegUtil.REG_SIZE;
			performOffset(loadInstruction(reg), RVRegUtil.STACK_POINTER, reg, offset, out);
		}
	}

	public static long backupRegisters(List<RVRegister> registers, RVAsmCode out) {
		long offset = 0;
		for (var reg : registers) {
			offset -= RVRegUtil.REG_SIZE;
			performOffset(storeInstruction(reg), RVRegUtil.STACK_POINTER, reg, offset, out);
		}
		return -offset;
	}

	public static long argStackSize(Value[] args) {
		var typeCnt = new EnumMap<RVRegisterType, Integer>(RVRegisterType.class);
		for (var type : RVRegisterType.values()) typeCnt.put(type, 0);

		long size = 0;
		for (var arg : args) {
			var regType = RVRegUtil.regType(arg);
			var id = typeCnt.get(regType);
			typeCnt.computeIfPresent(regType, (_, cnt) -> cnt + 1);

			var paramRegs = RVRegUtil.PARAMETER_REGISTERS.get(regType);
			if (paramRegs.size() <= id) size += RVRegUtil.REG_SIZE;
		}

		return size;
	}

	public static void storeArgs(RVModule rvModule, Value[] args, RVCacheManager cacheManager, RVAsmCode out) {
		var typeCnt = new EnumMap<RVRegisterType, Integer>(RVRegisterType.class);
		for (var type : RVRegisterType.values()) typeCnt.put(type, 0);

		var onStack = new ArrayList<Value>();
		var parMove = new HashMap<RVTypedPosition, RVTypedPosition>();
		for (var arg : args) {
			var regType = RVRegUtil.regType(arg);
			var id = typeCnt.get(regType);
			typeCnt.computeIfPresent(regType, (_, cnt) -> cnt + 1);

			var paramRegs = RVRegUtil.PARAMETER_REGISTERS.get(regType);
			if (paramRegs.size() <= id) {
				onStack.add(arg);
				continue;
			}
			var pos = Objects.requireNonNull(typedPositionOf(rvModule, arg));
			parMove.put(pos.with(new RVRegister(regType, paramRegs.get(id))), pos);
		}

		long offset = 0;
		for (var arg : onStack) {
			var pos = Objects.requireNonNull(typedPositionOf(rvModule, arg));
			storeToSp(pos, offset, cacheManager, out);
			offset += RVRegUtil.REG_SIZE;
		}

		parallelMove(parMove, cacheManager, out);
	}

	public static void collectReturn(RVTypedPosition pos, RVCacheManager cacheManager, RVAsmCode out) {
		var regType = RVRegUtil.regType(pos.type());
		var reg = new RVRegister(regType, RVRegUtil.RETURN_REGISTER.get(regType));
		move(pos, pos.with(reg), cacheManager, out);
	}

	public static void putReturn(RVTypedPosition pos, RVCacheManager cacheManager, RVAsmCode out) {
		var regType = RVRegUtil.regType(pos.type());
		var reg = new RVRegister(regType, RVRegUtil.RETURN_REGISTER.get(regType));
		move(pos.with(reg), pos, cacheManager, out);
	}
}
