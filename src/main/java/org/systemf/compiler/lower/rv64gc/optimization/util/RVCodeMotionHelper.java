package org.systemf.compiler.lower.rv64gc.optimization.util;

import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadAddress;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadImm;
import org.systemf.compiler.lower.rv64gc.instruction.RVMoveWord2Float;

public class RVCodeMotionHelper {
	public static boolean checkNonMobile(Instruction inst) {
		return !(inst instanceof RVLoadImm || inst instanceof RVLoadAddress || inst instanceof RVMoveWord2Float);
	}
}
