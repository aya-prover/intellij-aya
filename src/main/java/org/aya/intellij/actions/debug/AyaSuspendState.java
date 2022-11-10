package org.aya.intellij.actions.debug;

import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.Nullable;

public class AyaSuspendState extends XSuspendContext {
  @Override public @Nullable AyaExecutionStack getActiveExecutionStack() {
    return new AyaExecutionStack("Aya Execution Stack", null);
  }
}
