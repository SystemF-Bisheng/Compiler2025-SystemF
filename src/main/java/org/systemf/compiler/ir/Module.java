package org.systemf.compiler.ir;

import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.value.Value;

import java.io.PrintStream;
import java.util.*;

public class Module {
	private final HashMap<String, GlobalVariable> declarations = new HashMap<>();
	private final HashMap<String, Function> functions = new HashMap<>();
	private final HashMap<String, ExternalFunction> externalFunctions = new HashMap<>();
	private final Set<String> occupiedNames = new HashSet<>();
	private boolean irBuilderAttached = false;

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

	private void checkGlobalName(String name) {
		if (declarations.containsKey(name) || functions.containsKey(name) || externalFunctions.containsKey(name))
			throw new IllegalStateException("Duplicate declaration: " + name);
		occupiedNames.add(name);
	}

	public void addGlobalVariable(GlobalVariable declaration) {
		var name = declaration.getName();
		checkGlobalName(name);
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
		checkGlobalName(name);
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

	public void addExternalFunction(ExternalFunction externalFunction) {
		var name = externalFunction.getName();
		checkGlobalName(name);
		externalFunctions.put(name, externalFunction);
	}

	public void removeExternalFunction(ExternalFunction externalFunction) {
		externalFunctions.remove(externalFunction.getName());
	}

	public Map<String, ExternalFunction> getExternalFunctions() {
		return Collections.unmodifiableMap(externalFunctions);
	}

	public ExternalFunction getExternalFunction(String name) {
		return externalFunctions.get(name);
	}

	public Value lookupGlobal(String name) {
		var variable = getGlobalVariable(name);
		if (variable != null) return variable;
		var func = getFunction(name);
		if (func != null) return func;
		return getExternalFunction(name);
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
		externalFunctions.values().forEach(out::println);
		out.println();
		declarations.values().forEach(out::println);
		out.println();
		functions.values().forEach(out::println);
	}
}