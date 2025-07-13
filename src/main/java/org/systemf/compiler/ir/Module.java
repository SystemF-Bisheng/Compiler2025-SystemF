package org.systemf.compiler.ir;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;

import java.io.PrintStream;
import java.util.*;

public class Module {
	private final HashMap<String, GlobalVariable> declarations;
	private final HashMap<String, Function> functions;
	private final Set<String> occupiedNames;
	private boolean irBuilderAttached;

	public Module() {
		this.irBuilderAttached = false;
		this.declarations = new HashMap<>();
		this.functions = new HashMap<>();
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

	public void addGlobalVariable(GlobalVariable declaration) {
		var name = declaration.getName();
		if (declarations.containsKey(name)) throw new IllegalStateException("Duplicate declaration: " + name);
		declarations.put(name, declaration);
	}

	public void removeGlobalVariable(GlobalVariable declaration) {
		declarations.remove(declaration.getName());
	}

	public GlobalVariable getGlobalVariable(String name) {
		return declarations.get(name);
	}

	public Map<String, GlobalVariable> getGlobalDeclarations() {
		return Collections.unmodifiableMap(declarations);
	}

	public void addFunction(Function declaration) {
		var name = declaration.getName();
		if (functions.containsKey(name)) throw new IllegalStateException("Duplicate function: " + name);
		functions.put(name, declaration);
	}

	public void removeFunction(Function declaration) {
		functions.remove(declaration.getName());
	}

	public Map<String, Function> getFunctions() {
		return Collections.unmodifiableMap(functions);
	}

	public Function getFunction(String name) {
		return functions.get(name);
	}

	public Function getNamedFunction(String name) {
		if (!functions.containsKey(name)) {
			throw new NoSuchElementException("Function not found: " + name);
		}
		return functions.get(name);
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

	public void dump(PrintStream out) {
		declarations.values().forEach(out::println);

		out.println();

		functions.values().forEach(out::println);
	}
}