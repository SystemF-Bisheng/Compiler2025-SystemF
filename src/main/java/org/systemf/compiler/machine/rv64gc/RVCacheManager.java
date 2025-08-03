package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.lower.rv64gc.module.position.RVPosition;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;
import org.systemf.compiler.lower.rv64gc.util.RVTypeHelper;

import java.util.*;

public class RVCacheManager {
	private final Map<RVRegisterType, RVTempRegList> tempRegs = new EnumMap<>(RVRegisterType.class);
	private final Set<RVTempReg> locked = new LinkedHashSet<>();

	public RVCacheManager() {
		for (var type : RVRegisterType.values()) tempRegs.put(type, new RVTempRegList(type));
	}

	public RVRegister alloc(RVRegisterType type, RVAsmCode out) {
		var res = tempRegs.get(type).alloc(out);
		locked.add(res);
		return res.pos;
	}

	public RVRegister load(RVTypedPosition pos, RVAsmCode out) {
		if (pos.position() instanceof RVRegister reg) return reg;
		var res = tempRegs.get(RVRegUtil.regType(pos.type())).allocForLoad(pos, out);
		locked.add(res);
		return res.pos;
	}

	public RVRegister allocForStore(RVTypedPosition pos, RVAsmCode out) {
		if (pos.position() instanceof RVRegister reg) return reg;
		var res = tempRegs.get(RVRegUtil.regType(pos.type())).allocForStore(pos, out);
		locked.add(res);
		return res.pos;
	}

	public void filter(Set<RVTypedPosition> filter) {
		tempRegs.values().forEach(list -> list.filter(filter));
	}

	public void unlockAll() {
		for (var tmp : locked) tmp.locked = false;
		locked.clear();
	}

	public void unlock(RVRegister reg) {
		RVTempReg toUnlock = null;
		for (var tmp : locked)
			if (tmp.pos.equals(reg)) {
				toUnlock = tmp;
				break;
			}
		if (toUnlock == null) return;
		toUnlock.locked = false;
		locked.remove(toUnlock);
	}

	public void invalidateAll(RVAsmCode out) {
		tempRegs.values().forEach(list -> list.invalidateAll(out));
	}

	private static class RVTempRegList {
		public final List<RVTempReg> regs;
		private int clock = 0;

		public RVTempRegList(RVRegisterType type) {
			regs = new ArrayList<>();
			for (var index : RVRegUtil.AVAILABLE_TEMPORARY.get(type))
				regs.add(new RVTempReg(new RVRegister(type, index)));
		}

		public RVTempReg alloc(RVAsmCode out) {
			RVTempReg res = null;
			var oldClock = clock;
			do {
				var candidate = regs.get(clock);
				clock = (clock + 1) % regs.size();
				if (!candidate.locked) {
					res = candidate;
					break;
				}
			} while (clock != oldClock);
			if (res == null) throw new IllegalStateException("No enough temporary registers");

			res.invalidate(out);
			res.locked = true;
			return res;
		}

		public void filter(Set<RVTypedPosition> filter) {
			for (var tmp : regs) {
				if (filter.contains(tmp.cached)) continue;
				tmp.clear();
			}
		}

		public void invalidateAll(RVAsmCode out) {
			regs.forEach(tmp -> tmp.invalidate(out));
		}

		public void invalidate(RVPosition pos, RVAsmCode out) {
			for (var tmp : regs) {
				var cached = tmp.cached;
				if (cached == null) continue;
				if (pos.equals(cached.position())) tmp.invalidate(out);
			}
		}

		private RVTempReg find(RVTypedPosition pos) {
			var targetPosition = pos.position();
			var targetSize = RVTypeHelper.sizeOf(pos.type());
			for (var reg : regs) {
				var cached = reg.cached;
				if (cached == null) continue;
				if (!targetPosition.equals(cached.position())) continue;
				if (RVTypeHelper.sizeOf(cached.type()) < targetSize) continue;
				return reg;
			}
			return null;
		}

		public RVTempReg allocForLoad(RVTypedPosition pos, RVAsmCode out) {
			var res = find(pos);
			if (res == null) {
				invalidate(pos.position(), out);
				res = alloc(out);
				pos.load(res.pos, out);
			}
			res.locked = true;
			res.cached = pos;
			return res;
		}

		public RVTempReg allocForStore(RVTypedPosition pos, RVAsmCode out) {
			var res = find(pos);
			if (res == null) {
				invalidate(pos.position(), out);
				res = alloc(out);
			}
			res.locked = true;
			res.dirty = true;
			res.cached = pos;
			return res;
		}
	}
}
