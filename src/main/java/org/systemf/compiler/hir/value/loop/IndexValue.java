package org.systemf.compiler.hir.value.loop;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;

public class IndexValue extends DummyLoopValue implements INamed {
  public IndexValue(Type type, String name, Value start, Value end, Value step) {
    super(type);
    this.name = name;
    this.start = start;
    this.end = end;
    this.step = step;
  }

  public Value getStart() {
    return start;
  }

  public Value getEnd() {
    return end;
  }

  public Value getStep() {
    return step;
  }

  public void setStart(Value start) {
    this.start = start;
  }

  public void setEnd(Value end) {
    this.end = end;
  }

  public void setStep(Value step) {
    this.step = step;
  }

  final private String name;
  private Value start, end, step;

  @Override
  public String getName() {
    return name;
  }
}
