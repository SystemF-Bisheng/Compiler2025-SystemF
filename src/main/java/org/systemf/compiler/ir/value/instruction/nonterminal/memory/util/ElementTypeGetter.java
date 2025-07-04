package org.systemf.compiler.ir.value.instruction.nonterminal.memory.util;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.type.util.TypeId;

public class ElementTypeGetter {
  static public Type get(Type type) {
    if (type.typeId != TypeId.ArrayId) {
      System.err.println(String.format(
        "error: try to get element type of `%s`", type.toString()
      ));
      System.exit(-1);
    }
    return ((Array) type).elementType;
  }
}