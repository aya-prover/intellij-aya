package org.aya.intellij.actions.debug;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragmentFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.aya.intellij.actions.run.AyaProgramRunner;
import org.aya.intellij.language.AyaFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaDebugProcess extends XDebugProcess {
  public AyaDebugProcess(@NotNull XDebugSession session, @NotNull AyaProgramRunner.AyaRunState state) {
    super(session);
  }

  @Override public void sessionInitialized() {
    getSession().positionReached(new AyaSuspendState());
  }

  @Override public void resume(@Nullable XSuspendContext context) {}

  @Override public void stop() {}

  @Override public void startStepOut(@Nullable XSuspendContext context) {
    getSession().positionReached(new AyaSuspendState());
  }

  @Override public void startStepInto(@Nullable XSuspendContext context) {
    getSession().positionReached(new AyaSuspendState());
  }

  @Override public void startStepOver(@Nullable XSuspendContext context) {
    getSession().positionReached(new AyaSuspendState());
  }

  @Override public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
    getSession().positionReached(new AyaSuspendState());
  }

  @Override public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
    return new AyaEditorsProvider();
  }

  private static final class AyaEditorsProvider extends XDebuggerEditorsProviderBase {
    @Override public @NotNull FileType getFileType() {
      return AyaFileType.INSTANCE;
    }

    @Override
    protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @Nullable PsiElement context, boolean isPhysical) {
      // TODO: aya code fragment?
      return JavaCodeFragmentFactory.getInstance(project).createExpressionCodeFragment(text, context, null, isPhysical);
    }
  }
}
