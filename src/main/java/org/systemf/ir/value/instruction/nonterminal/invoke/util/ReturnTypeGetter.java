package org.systemf.ir.value.instruction.nonterminal.invoke.util;

import org.systemf.ir.type.FunctionType;
import org.systemf.ir.type.Type;
import org.systemf.ir.type.util.TypeId;

public class ReturnTypeGetter {
  static public Type get(Type type) {
    if (type.typeId != TypeId.FunctionType) {
      System.err.println(String.format(
        "error: try to get return type of `%s`", type.toString()
      ));
    }
    return ((FunctionType) type).returnType;
  }
}
