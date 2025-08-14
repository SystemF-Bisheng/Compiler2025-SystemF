package org.systemf.compiler.analysis.util;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.optimization.pass.util.PositionInfo;

import java.util.HashMap;
import java.util.Map;

public class BelongingHelper {
	public static Map<Instruction, Function> getBelonging(Module module) {
		var res = new HashMap<Instruction, Function>();
		module.getFunctions().values().forEach(func -> func.allInstructions().forEach(inst -> res.put(inst, func)));
		return res;
	}

	public static Map<Instruction, BasicBlock> getBelonging(Function function) {
		var res = new HashMap<Instruction, BasicBlock>();
		function.getBlocks().forEach(block -> block.instructions.forEach(inst -> res.put(inst, block)));
		return res;
	}

	public static Map<Instruction, PositionInfo> getPositions(Function function) {
		var res = new HashMap<Instruction, PositionInfo>();
		for (var block : function.getBlocks()) {
			var index = 0;
			for (var inst : block.instructions) res.put(inst, new PositionInfo(block, index++));
		}
		return res;
	}
}
