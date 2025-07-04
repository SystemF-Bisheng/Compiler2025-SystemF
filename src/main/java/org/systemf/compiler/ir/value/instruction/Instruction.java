package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.Value;

public abstract class Instruction extends Value {
  protected Instruction(Type type, String name) {
    super(type, name);
  }

  public boolean isterminal() {
    return false;
  }

  @Override
  public String dump() {
    // TODO: implement `dump()` methods of all instructions
    System.err.println(String.format(
      "error: dump() method of definition instruction of `%s` is not implemented", name
    ));
    System.exit(-1);
    return "";
  }
}