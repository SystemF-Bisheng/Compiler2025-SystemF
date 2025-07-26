package org.systemf.compiler.machine.riscv;

import java.util.List;

public class MachineGlobalVariable {
	public enum Section {
		DATA,
		BSS
	}

	private final String name;
	private final Section section;
	private final int alignment;
	private final int totalSize;

	private final String dataDirective; // .word or .float.
	private final List<String> initialValues;

	private MachineGlobalVariable(String name, Section section, int alignment, int totalSize, String dataDirective, List<String> initialValues) {
		this.name = name;
		this.section = section;
		this.alignment = alignment;
		this.totalSize = totalSize;
		this.dataDirective = dataDirective;
		this.initialValues = initialValues;
	}

	public static MachineGlobalVariable createDataVariable(String name, int alignment, int totalSize, String dataDirective, List<String> initialValues) {
		return new MachineGlobalVariable(name, Section.DATA, alignment, totalSize, dataDirective, initialValues);
	}

	public static MachineGlobalVariable createBssVariable(String name, int alignment, int totalSize) {
		return new MachineGlobalVariable(name, Section.BSS, alignment, totalSize, ".space", List.of());
	}

	public String render() {
		StringBuilder sb = new StringBuilder();
		sb.append(".globl ").append(name).append("\n");
		sb.append("\t.align ").append(alignment).append("\n");
		sb.append(name).append(":\n");

		if (section == Section.DATA) {
			for (String value : initialValues) {
				sb.append("\t").append(dataDirective).append(" ").append(value).append("\n");
			}
		} else {
			sb.append("\t.space ").append(totalSize).append("\n");
		}

		return sb.toString();
	}

}
