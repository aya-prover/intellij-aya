package org.aya.intellij.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.aya.intellij.lsp.AyaLsp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiReference extends PsiReferenceBase<AyaPsiElement> {
  public AyaPsiReference(@NotNull AyaPsiElement element, TextRange rangeInElement) {
    super(element, rangeInElement);
  }

  @Override public @Nullable PsiElement resolve() {
    var lsp = AyaLsp.of(myElement.getProject());
    if (lsp == null) return null;
    var def = lsp.gotoDefinition(myElement).firstOrNull();
    return PsiTreeUtil.getParentOfType(def, AyaPsiNamedElement.class);
  }
}
