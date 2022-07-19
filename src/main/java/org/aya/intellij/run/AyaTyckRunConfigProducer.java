package org.aya.intellij.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.concrete.AyaPsiDecl;
import org.jetbrains.annotations.NotNull;

public class AyaTyckRunConfigProducer extends LazyRunConfigurationProducer<AyaTyckRunConfig> {
  @Override
  protected boolean setupConfigurationFromContext(@NotNull AyaTyckRunConfig config, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
    var psi = sourceElement.get();
    if (!AyaTyckRunConfig.isTyckUnit(psi)) return false;
    switch (psi) {
      // TODO: implement some psi helpers
      case AyaPsiDecl decl -> {
        config.setName("TypeCheck <this-definition>");
        config.moduleName("TODO Module");
        config.definitionName("TODO Definition");
      }
      case AyaPsiFile file -> {
        config.setName("TypeCheck " + file.getName());
        config.moduleName("TODO Module");
      }
      default -> {}
    }
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull AyaTyckRunConfig config, @NotNull ConfigurationContext context) {
    // TODO: how to check?
    return false;
  }

  @Override
  public @NotNull ConfigurationFactory getConfigurationFactory() {
    return new AyaTyckRunConfigType.AyaTraceTyckConfigTypeFactory(new AyaTyckRunConfigType());
  }
}
