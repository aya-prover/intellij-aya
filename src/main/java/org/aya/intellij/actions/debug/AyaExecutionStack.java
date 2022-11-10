package org.aya.intellij.actions.debug;

import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class AyaExecutionStack extends XExecutionStack {
  protected AyaExecutionStack(@NotNull String displayName, @Nullable Icon icon) {
    super(displayName, icon);
  }

  @Override public @Nullable AyaStackFrame getTopFrame() {
    return new AyaStackFrame();
  }

  @Override public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
    container.addStackFrames(List.of(new AyaStackFrame(), new AyaStackFrame()), false);
  }

  public static final class AyaStackFrame extends XStackFrame {
    @Override public void customizePresentation(@NotNull ColoredTextContainer component) {
      component.append("Aya stack frame", SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
  }
}
