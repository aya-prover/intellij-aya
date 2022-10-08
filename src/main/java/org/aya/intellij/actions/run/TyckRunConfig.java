package org.aya.intellij.actions.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.components.StoredProperty;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.concrete.AyaPsiDecl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TyckRunConfig extends LocatableConfigurationBase<TyckRunConfig.Options> implements RefactoringListenerProvider {
  protected TyckRunConfig(@NotNull Project project, @NotNull ConfigurationFactory factory, @Nullable String name) {
    super(project, factory, name);
  }

  @Override public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new TyckRunConfigEditorUI();
  }

  @Override protected @NotNull TyckRunConfig.Options getOptions() {
    return (Options) super.getOptions();
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

  public static class Options extends LocatableRunConfigurationOptions {
    final @NotNull StoredProperty<String> moduleName = string("").provideDelegate(this, "moduleName");
    final @NotNull StoredProperty<String> definitionName = string("").provideDelegate(this, "definitionName");
  }

  /** creates run configuration from gutter icons (works with {@link AyaRunLineMarkerContributor}) */
  public static class Producer extends LazyRunConfigurationProducer<TyckRunConfig> {
    @Override
    protected boolean setupConfigurationFromContext(@NotNull TyckRunConfig config, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
      var psi = sourceElement.get();
      if (!AyaRunLineMarkerContributor.TOP_LEVEL_DECL_ID.accepts(psi)) return false;
      if (psi instanceof AyaPsiFile file) {
        config.setName("TypeCheck " + file.getName());
        config.moduleName(QualifiedID.join(file.containingFileModule()));
      } else {
        var decl = (AyaPsiDecl) psi.getParent().getParent().getParent();
        var defName = decl.nameOrEmpty();
        config.setName("TypeCheck " + defName);
        config.moduleName(QualifiedID.join(decl.containingFileModule()));
        config.definitionName(QualifiedID.join(decl.containingSubModule().appended(defName)));
      }
      return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull TyckRunConfig config, @NotNull ConfigurationContext context) {
      // TODO: how to check?
      return false;
    }

    @Override public @NotNull ConfigurationFactory getConfigurationFactory() {
      return new Factory(new Type());
    }
  }

  public static class Type implements ConfigurationType {
    public static final String ID = Type.class.getSimpleName();

    @Override public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
      return "Aya Prover";
    }

    @Override public @Nls(capitalization = Nls.Capitalization.Sentence) String getConfigurationTypeDescription() {
      return "Aya Prover type-checking run configuration";
    }

    @Override public Icon getIcon() {
      return AllIcons.RunConfigurations.Application;
    }

    @Override public @NotNull @NonNls String getId() {
      return ID;
    }

    @Override public ConfigurationFactory[] getConfigurationFactories() {
      return new ConfigurationFactory[]{new Factory(this)};
    }
  }

  public static class Factory extends ConfigurationFactory {
    protected Factory(@NotNull ConfigurationType type) {
      super(type);
    }

    @Override public @NotNull @NonNls String getId() {
      return Type.ID;
    }

    @Override public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new TyckRunConfig(project, this, "Tyck");
    }

    @Override public @Nullable Class<? extends BaseState> getOptionsClass() {
      return Options.class;
    }
  }
}
