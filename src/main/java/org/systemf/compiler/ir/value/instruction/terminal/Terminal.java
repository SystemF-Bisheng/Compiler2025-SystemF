package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;

public class Terminal extends Instruction {
  public Terminal(Type type, String name) {
    super(type, name);
  }

  @Override
  public boolean isterminal() {
    return true;
  }
}