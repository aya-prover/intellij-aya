package org.aya.intellij.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.components.StoredProperty;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AyaTyckRunConfigType implements ConfigurationType {
  public static final String ID = AyaTyckRunConfigType.class.getSimpleName();

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
    return new ConfigurationFactory[]{
      new AyaTraceTyckConfigTypeFactory(this)
    };
  }

  public static class AyaTraceTyckConfigTypeFactory extends ConfigurationFactory {
    protected AyaTraceTyckConfigTypeFactory(@NotNull ConfigurationType type) {
      super(type);
    }

    @Override public @NotNull @NonNls String getId() {
      return ID;
    }

    @Override public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new AyaTyckRunConfig(project, this, "Tyck");
    }

    @Override public @Nullable Class<? extends BaseState> getOptionsClass() {
      return AyaTraceConfigOptions.class;
    }
  }

  public static class AyaTraceConfigOptions extends LocatableRunConfigurationOptions {
    final @NotNull StoredProperty<String> moduleName = string("").provideDelegate(this, "moduleName");
    final @NotNull StoredProperty<String> definitionName = string("").provideDelegate(this, "definitionName");
  }
}
