package org.systemf.compiler.machine.rv64gc;

public record RVAsmCode(StringBuilder builder) {
	public RVAsmCode() {
		this(new StringBuilder());
	}

	public void addLine() {
		builder.append("\n");
	}

	public void addLine(String line) {
		builder.append(line);
		builder.append("\n");
	}

	public void add(String str) {
		builder.append(str);
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}
