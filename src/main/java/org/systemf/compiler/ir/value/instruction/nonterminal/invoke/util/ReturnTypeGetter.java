package org.systemf.compiler.ir.value.instruction.nonterminal.invoke.util;

import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.type.util.TypeId;

public class ReturnTypeGetter {
  static public Type get(Type type) {
    if (type.typeId != TypeId.FunctionType) {
      System.err.printf("error: try to get return type of `%s`%n", type);
    }
    return ((FunctionType) type).returnType;
  }
}