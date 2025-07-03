package org.systemf.ir.value.instruction.terminal;

import org.systemf.ir.type.Type;
import org.systemf.ir.value.instruction.Instruction;

public class Terminal extends Instruction {
  public Terminal(Type type, String name) {
    super(type, name);
  }

  @Override
  public boolean isterminal() {
    return true;
  }
}
