package org.aya.intellij.actions.run;

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.aya.intellij.actions.debug.AyaDebugProcess;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

/**
 * highly inspired from {@link com.intellij.javascript.debugger.execution.DebuggableProgramRunner}
 */
public class AyaProgramRunner extends AsyncProgramRunner<GenericDebuggerRunnerSettings> {
  @Override public @NotNull @NonNls String getRunnerId() {
    return AyaProgramRunner.class.getSimpleName();
  }

  @Override public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return (executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) // make "Debug" button clickable
      || executorId.equals(DefaultRunExecutor.EXECUTOR_ID)      // make "Run" button clickable
    ) && profile instanceof TyckRunConfig;
  }

  public static @Nullable AyaRunState prepare(@NotNull Executor executor, @NotNull TyckRunConfig config) {
    // TODO: what if user just want normal typechecking (rather than traced/debugger)?
    if (executor.getId().equals(DefaultRunExecutor.EXECUTOR_ID)) return null;
    // Now user want to trace the typechecking process.
    return new AyaRunState(config, true);
  }

  @Override
  protected @NotNull Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment env, @NotNull RunProfileState state) throws ExecutionException {
    if (!(state instanceof AyaRunState aya))
      return Promises.rejectedPromise("Trying to run non-aya program with AyaProgramRunner");
    var session = startDebug(env, aya);
    return Promises.resolvedPromise(session.getRunContentDescriptor());
  }

  private static @NotNull XDebugSession startDebug(@NotNull ExecutionEnvironment env, @NotNull AyaRunState state) throws ExecutionException {
    return XDebuggerManager.getInstance(env.getProject())
      .startSession(env, new XDebugProcessStarter() {
        @Override public @NotNull XDebugProcess start(@NotNull XDebugSession session) {
          return new AyaDebugProcess(session, state);
        }
      });
  }

  /** @implNote put aya compiler cmdline arguments here */
  public record AyaRunState(@NotNull TyckRunConfig config, boolean debug) implements RunProfileState {
    @Override public @Nullable ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) {
      // We do not need to execute here. AyaProgramRunner handles everything.
      throw new IllegalStateException("unreachable");
    }
  }
}
