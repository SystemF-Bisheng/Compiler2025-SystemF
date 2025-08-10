package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.lower.rv64gc.module.position.RVPosition;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class RVBackupStorage {
	public final long size;
	private final RVRegister[] backup;

	public RVBackupStorage(int backupSize) {
		this.size = (long) backupSize * RVRegUtil.REG_SIZE;
		this.backup = new RVRegister[backupSize];
	}

	public void filter(Set<RVTypedPosition> filter) {
		var aliveRegs = filter.stream().map(RVTypedPosition::position).filter(pos -> pos instanceof RVRegister)
				.map(pos -> (RVRegister) pos).collect(Collectors.toSet());
		for (int i = 0; i < backup.length; ++i) if (!aliveRegs.contains(backup[i])) backup[i] = null;
	}

	public void discard(RVPosition pos) {
		if (pos instanceof RVRegister reg) discard(reg);
	}

	public void discard(RVRegister reg) {
		for (int i = 0; i < backup.length; ++i) if (reg.equals(backup[i])) backup[i] = null;
	}

	public void discardAll() {
		Arrays.fill(backup, null);
	}

	private long offsetOf(int i) {
		return (long) i * RVRegUtil.REG_SIZE;
	}

	public void restore(RVPosition pos, RVAsmCode out) {
		if (pos instanceof RVRegister reg) restore(reg, out);
	}

	public void restore(RVRegister reg, RVAsmCode out) {
		for (int i = 0; i < backup.length; ++i)
			if (reg.equals(backup[i])) {
				RVGenerateHelper.performOffset(RVGenerateHelper.loadInstruction(reg), RVRegUtil.STACK_POINTER, reg,
						offsetOf(i), out);
				backup[i] = null;
				break;
			}
	}

	public void restoreTemp(RVRegister reg, RVAsmCode out) {
		for (int i = 0; i < backup.length; ++i)
			if (reg.equals(backup[i])) {
				RVGenerateHelper.performOffset(RVGenerateHelper.loadInstruction(reg), RVRegUtil.STACK_POINTER, reg,
						offsetOf(i), out);
				break;
			}
	}

	public void restoreAll(RVAsmCode out) {
		for (int i = 0; i < backup.length; ++i) {
			var backed = backup[i];
			if (backed != null) {
				RVGenerateHelper.performOffset(RVGenerateHelper.loadInstruction(backed), RVRegUtil.STACK_POINTER,
						backed, offsetOf(i), out);
				backup[i] = null;
			}
		}
	}

	public void backup(SortedSet<RVRegister> regs, RVAsmCode out) {
		regs.forEach(reg -> backup(reg, out));
	}

	public void backup(RVRegister reg, RVAsmCode out) {
		for (var backed : backup) if (reg.equals(backed)) return;

		for (int i = 0; i < backup.length; ++i)
			if (backup[i] == null) {
				RVGenerateHelper.performOffset(RVGenerateHelper.storeInstruction(reg), RVRegUtil.STACK_POINTER, reg,
						offsetOf(i), out);
				backup[i] = reg;
				return;
			}

		throw new IllegalStateException("No enough space for backup");
	}
}
