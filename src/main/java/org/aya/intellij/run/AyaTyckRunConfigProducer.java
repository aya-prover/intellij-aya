package org.aya.intellij.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.concrete.AyaPsiDecl;
import org.jetbrains.annotations.NotNull;

public class AyaTyckRunConfigProducer extends LazyRunConfigurationProducer<AyaTyckRunConfig> {
  @Override
  protected boolean setupConfigurationFromContext(@NotNull AyaTyckRunConfig config, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
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
  public boolean isConfigurationFromContext(@NotNull AyaTyckRunConfig config, @NotNull ConfigurationContext context) {
    // TODO: how to check?
    return false;
  }

  @Override
  public @NotNull ConfigurationFactory getConfigurationFactory() {
    return new AyaTyckRunConfigType.AyaTraceTyckConfigTypeFactory(new AyaTyckRunConfigType());
  }
}
