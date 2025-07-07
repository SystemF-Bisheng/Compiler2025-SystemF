package org.systemf.compiler.ir;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Module {
	private final ArrayList<GlobalDeclaration> declarations;
	private final ArrayList<Function> functions;
	private final Set<String> occupiedNames;
	private boolean irBuilderAttached;

	public Module() {
		this.irBuilderAttached = false;
		this.declarations = new ArrayList<>();
		this.functions = new ArrayList<>();
		this.occupiedNames = new HashSet<>();
	}

	public String getNonConflictName(String originalName) {
		if (!occupiedNames.contains(originalName)) {
			occupiedNames.add(originalName);
			return originalName;
		}

		int suffix = 0;
		while (true) {
			String newName = originalName + suffix;
			if (!occupiedNames.contains(newName)) {
				occupiedNames.add(newName);
				return newName;
			}
			++suffix;
		}
	}

	public void addGlobalDeclaration(GlobalDeclaration declaration) {
		declarations.add(declaration);
	}

	public void removeGlobalDeclaration(GlobalDeclaration declaration) {
		declarations.remove(declaration);
	}

	public void removeGlobalDeclaration(int index) {
		declarations.remove(index);
	}

	public int getGlobalDeclarationCount() {
		return declarations.size();
	}

	public GlobalDeclaration getGlobalDeclaration(int index) {
		return declarations.get(index);
	}

	public void addFunction(Function declaration) {
		functions.add(declaration);
	}

	public void removeFunction(Function declaration) {
		functions.remove(declaration);
	}

	public void removeFunction(int index) {
		functions.remove(index);
	}

	public int getFunctionCount() {
		return functions.size();
	}

	public Function getFunction(int index) {
		return functions.get(index);
	}

	public void attachIRBuilder() {
		irBuilderAttached = true;
	}

	public void detachIRBuilder() {
		irBuilderAttached = false;
	}

	public boolean isIRBuilderAttached() {
		return irBuilderAttached;
	}

	public void dumpToConsole() {
		System.out.println("; ModuleId = '" + "'");
		System.out.println("source_filename = \"" + "\"");
		System.out.println();

		for (GlobalDeclaration declaration : declarations) {
			System.out.println(declaration);
		}

		System.out.println();

		for (Function function : functions) {
			System.out.println(function);
		}
	}

	public void dumpToFile(String path) {
		try (PrintWriter writer = new PrintWriter(new java.io.FileWriter(path))) {
			writer.println("; ModuleId = '" + "'");
			writer.println("source_filename = \"" + "\"");
			writer.println();

			for (GlobalDeclaration declaration : declarations) {
				writer.println(declaration);
			}

			writer.println();

			for (Function function : functions) {
				writer.println(function);
			}

		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}