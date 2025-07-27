package org.systemf.compiler.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;

import org.systemf.compiler.machine.riscv.*;
import org.systemf.compiler.parser.SysYParser.InitializerContext;
import org.systemf.compiler.interpreter.value.ExecutionValue;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ArrayZeroInitializer;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantArray;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.DummyValueInstruction;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;

public enum RISCVGenerator implements EntityProvider<MachineCodeResult> {
	INSTANCE;

	private final int alignment = 16;
	private MachineModule machineModule;

	@Override
	public MachineCodeResult produce() {
		var module = QueryManager.getInstance().get(Module.class);
		machineModule = new MachineModule("riscv-assembly");
		// Seems that Module has no name, so use a default name
		
		// TODO: Implement RISC-V assembly generation
		// var result = new MachineCodeResult();
		// var instructionVisitor = new InstructionVisitor();
		// Considering if we need more heirarchy visitors or if we define some functions instead

		try {
			generateGlobalVariables(module);
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException("Machine: Unable to generate global variables", e);
		}

		try {
			generateFunctions(module);
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException("Machine: Unable to generate functions", e);
		}

		return new MachineCodeResult(machineModule);
	}

	private void generateGlobalVariables(Module module) {
		machineModule.clearGlobalVariables();
		for (var entry : module.getGlobalDeclarations().entrySet()) {
			GlobalVariable globalVariable = entry.getValue();
			// machineModule.addGlobalVariable(formGlobalVariable(globalVariable));

			if (globalVariable.getInitializer() instanceof ArrayZeroInitializer) {
				machineModule.addGlobalVariable(
					MachineGlobalVariable.createBssVariable(
						globalVariable.getName(),
						alignment,
						calcSize(globalVariable.getType())
					)
				);
			} else {
				List<String> initialValues = List.of();
				formInitialValues(globalVariable.getType(), globalVariable.getInitializer(), initialValues);
				machineModule.addGlobalVariable(
					MachineGlobalVariable.createDataVariable(
						globalVariable.getName(),
						alignment,
						calcSize(globalVariable.getType()),
						".word",
						initialValues
					)
				);
			}
		}
	}

	private void generateFunctions(Module module) {
		// TODO: Implement function generation
		machineModule.clearFunctions();
		for (var entry : module.getFunctions().entrySet()) {
			Function function = entry.getValue();

		}
	}

	private class FunctionProcesser {
		private final Function function;
		private MachineFunction machineFunction;

		private final Map<Value, Integer> stackOffsetMap = new HashMap<>();
		private int totalFrameSize = 0;

		public FunctionProcesser(Function function) {
			this.function = function;
		}

		public MachineFunction process() {
			calculateFrameLayout();

			generateMachineCode();

			return machineFunction;
		}

		private void calculateFrameLayout() {
			int currentOffset = 0;

			currentOffset -= 8; // for ra
			// int raOffset = currentOffset;

			currentOffset -= 8; // for fp
			// int fpOffset = currentOffset;

			for (BasicBlock block : function.getBlocks()) {
				for (Instruction instruction : block.instructions) {
					if (instruction instanceof Alloca alloca) {
						int size = calcSize(alloca.getType());
						currentOffset -= size;
						currentOffset -= currentOffset % alignment; // align to 4 bytes

						stackOffsetMap.put(alloca, currentOffset);
					} else if (instruction instanceof DummyValueInstruction dummyValueInstruction) {
						// a dummy value instruction, put the result in the stack
						int size = calcSize(dummyValueInstruction.getType());
						currentOffset -= size;
						currentOffset -= currentOffset % alignment; // align to 4 bytes

						stackOffsetMap.put(dummyValueInstruction, currentOffset);
					}
				}
			}

			this.totalFrameSize = -currentOffset;
			if (totalFrameSize % alignment != 0) {
				totalFrameSize = (totalFrameSize / alignment + 1) * alignment;
			}
		}

		private void generateMachineCode() {
			this.machineFunction = new MachineFunction(function.getName());

			generatePrologue();
			
			generateEpilogue();

			BasicBlockProcesser basicBlockProcesser = new BasicBlockProcesser(function.getEntryBlock(), stackOffsetMap, totalFrameSize, machineFunction.getEpilogueInstructions());
			machineFunction.addBasicBlock(basicBlockProcesser.process());

			for (BasicBlock block : function.getBlocks()) {
				if (block == function.getEntryBlock()) continue; // already processed
				basicBlockProcesser = new BasicBlockProcesser(block, stackOffsetMap, totalFrameSize, machineFunction.getEpilogueInstructions());
				machineFunction.addBasicBlock(basicBlockProcesser.process());
			}

		}

		private void generatePrologue() {
			var prologueInstructions = List.<MachineInstruction>of(
				// new MachineInstruction("addi", MachineRegister.SP, MachineRegister.SP, new MachineImmediate(-totalFrameSize)),
				// new MachineInstruction("sw", MachineRegister.RA, MachineRegister.SP, new MachineImmediate(totalFrameSize - 8)),
				// new MachineInstruction("sw", MachineRegister.FP, MachineRegister.SP, new MachineImmediate(totalFrameSize - 16)),
				// new MachineInstruction("addi", MachineRegister.FP, MachineRegister.SP, new MachineImmediate(totalFrameSize)),
				MachineInstruction.Addi(MachineRegister.SP, MachineRegister.SP, new MachineImmediate(-totalFrameSize)),
				MachineInstruction.Sw(MachineRegister.RA, MachineRegister.SP, new MachineImmediate(totalFrameSize - 8)),
				MachineInstruction.Sw(MachineRegister.FP, MachineRegister.SP, new MachineImmediate(totalFrameSize - 16)),
				MachineInstruction.Addi(MachineRegister.FP, MachineRegister.SP, new MachineImmediate(totalFrameSize))
			);

			machineFunction.setPrologueInstructions(prologueInstructions);
		}
		
		private void generateEpilogue() {
			var epilogueInstructions = List.<MachineInstruction>of(
				// TODO
			);

			machineFunction.setEpilogueInstructions(epilogueInstructions);
		}
	}

	private class BasicBlockProcesser {
		private final BasicBlock basicBlock;
		private MachineBasicBlock machineBasicBlock;
		private final Map<Value, Integer> stackOffsetMap = new HashMap<>();
		private int totalFrameSize = 0;
		private List<MachineInstruction> epilogueInstructions = List.of();

		public BasicBlockProcesser(BasicBlock basicBlock) {
			this.basicBlock = basicBlock;
		}

		public BasicBlockProcesser(BasicBlock basicBlock, Map<Value, Integer> stackOffsetMap, int totalFrameSize, List<MachineInstruction> epilogueInstructions) {
			this.basicBlock = basicBlock;
			this.stackOffsetMap.putAll(stackOffsetMap);
			this.totalFrameSize = totalFrameSize;
			this.epilogueInstructions = epilogueInstructions;
		}

		public MachineBasicBlock process() {
			this.machineBasicBlock = new MachineBasicBlock(basicBlock.getName());

			InstructionVisitor instructionVisitor = new InstructionVisitor(stackOffsetMap, totalFrameSize, epilogueInstructions);
			for (Instruction instruction : basicBlock.instructions) {
				List<MachineInstruction> machineInstructions = instruction.accept(instructionVisitor);
				for (MachineInstruction machineInstruction : machineInstructions) {
					this.machineBasicBlock.addInstruction(machineInstruction);
				}
			}

			return this.machineBasicBlock;
		}
	}

	private class InstructionVisitor extends InstructionVisitorBase<List<MachineInstruction>> {
		private final Map<Value, Integer> stackOffsetMap = new HashMap<>();
		private List<MachineInstruction> retEpilogue = List.of();
		private int totalFrameSize = 0;
		
		public InstructionVisitor() {
			super();
		}
		
		public InstructionVisitor(Map<Value, Integer> stackOffsetMap, int totalFrameSize, List<MachineInstruction> retEpilogue) {
			super();
			this.stackOffsetMap.putAll(stackOffsetMap);
			this.totalFrameSize = totalFrameSize;
			this.retEpilogue = retEpilogue;
		}
		
		// TODO: Implement RISC-V Instruction Translation
		@Override
		public List<MachineInstruction> visit(DummyBinary dummyBinary) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(FNeg fNeg) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(FpToSi fpToSi) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(SiToFp siToFp) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Alloca alloca) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Load load) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(GetPtr getPtr) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Store store) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Ret ret) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(RetVoid retVoid) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(CondBr condBr) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(Br br) {
			return List.of(new MachineInstruction("nop"));
		}

		@Override
		public List<MachineInstruction> visit(AbstractCall abstractCall) {
			return List.of(new MachineInstruction("nop"));
		}
	}

	private void formInitialValues(Type type, Constant initializer, List<String> initialValues) {
		if (type instanceof Array) {
			flattenArrayValues((ConstantArray) initializer, initialValues);
			return;
		}
		switch (initializer) {
			case ConstantInt intValue -> initialValues.add(String.format("%d", (int) intValue.value));
			case ConstantFloat floatValue -> initialValues.add(String.format("0x%08X", java.lang.Float.floatToRawIntBits((float) floatValue.value)));
			default -> throw new IllegalArgumentException("Unsupported type for initial value: " + type);
		}
	}

	private void flattenArrayValues(ConstantArray array, List<String> values) {
		for (int i = 0; i < array.getSize(); i++) {
			if (array.getContent(i) instanceof ConstantArray nestedArray) {
				flattenArrayValues(nestedArray, values);
			} else {
				// must be I32 or Float
				formInitialValues(array.getType(), array.getContent(i), values);
			}
		}
	}

	private int calcSize(Type type) {
		int length = 1;
		while (type instanceof Array array) {
			length *= array.length;
			type = array.getElementType();
		}
		return length * switch (type) {
			case I32 ignored -> 4;
			case Float ignored -> 4;
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		};
	}
}