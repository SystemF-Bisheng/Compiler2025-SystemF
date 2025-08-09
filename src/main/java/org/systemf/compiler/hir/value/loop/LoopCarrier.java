package org.systemf.compiler.hir.value.loop;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;

public class LoopCarrier extends DummyLoopValue implements INamed {
  public LoopCarrier(Type type, String name, Value initializer) {
    super(type);
    this.name = name;
    this.initializer = initializer;
  }

  public Value getInitializer() {
    return initializer;
  }

  public void setInitializer(Value initializer) {
    this.initializer = initializer;
  }

  final private String name;
  private Value initializer;

  @Override
  public String getName() {
    return name;
  }
}
