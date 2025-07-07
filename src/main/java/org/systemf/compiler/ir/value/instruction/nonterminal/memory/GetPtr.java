package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class GetPtr extends DummyValueNonTerminal {
  public final Value array, index;

  public GetPtr(String name, Value array, Value index) {
    super(new Pointer(TypeUtil.getElementType(TypeUtil.getElementType(array.getType()))), name);
    this.array = array;
    this.index = index;
  }
}