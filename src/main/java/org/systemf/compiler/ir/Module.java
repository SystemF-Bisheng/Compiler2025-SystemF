package org.systemf.compiler.ir;

import java.util.ArrayList;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;

public class Module {
	public Module() {
		this.IRBuilderAttached = false;
		this.declarations = new ArrayList<>();
		this.functions = new ArrayList<>();
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
		IRBuilderAttached = true;
	}

	public boolean isIRBuilderAttached() {
		return IRBuilderAttached;
	}

	private final ArrayList<GlobalDeclaration> declarations;
	private final ArrayList<Function> functions;

	private boolean IRBuilderAttached;
}