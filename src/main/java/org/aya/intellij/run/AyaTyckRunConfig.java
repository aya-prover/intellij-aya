package org.aya.intellij.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaTyckRunConfig extends LocatableConfigurationBase<AyaTyckRunConfigType.AyaTraceConfigOptions> implements RefactoringListenerProvider {
  protected AyaTyckRunConfig(@NotNull Project project, @NotNull ConfigurationFactory factory, @Nullable String name) {
    super(project, factory, name);
  }

  @Override public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new AyaTyckRunSettingsEditor();
  }

  @Override protected @NotNull AyaTyckRunConfigType.AyaTraceConfigOptions getOptions() {
    return (AyaTyckRunConfigType.AyaTraceConfigOptions) super.getOptions();
  }

  @Override
  public @Nullable RunProfileState getState(
    @NotNull Executor executor,
    @NotNull ExecutionEnvironment environment
  ) throws ExecutionException {
    // TODO: call Aya tycker
    return null;
  }

  @Override public @Nullable RefactoringElementListener getRefactoringElementListener(PsiElement element) {
    // TODO: check if this configuration refers to the element
    return null;
  }

  public void moduleName(@NotNull String text) {
    var opt = getOptions();
    opt.moduleName.setValue(opt, text);
  }

  public @NotNull String moduleName() {
    var opt = getOptions();
    return opt.moduleName.getValue(opt);
  }

  public void definitionName(@NotNull String text) {
    var opt = getOptions();
    opt.definitionName.setValue(opt, text);
  }

  public @NotNull String definitionName() {
    var opt = getOptions();
    return opt.definitionName.getValue(opt);
  }
}
