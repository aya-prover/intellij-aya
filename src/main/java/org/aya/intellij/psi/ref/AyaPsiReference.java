package org.aya.intellij.psi.ref;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiReference extends PsiReferenceBase<AyaPsiElement> {
  public AyaPsiReference(@NotNull AyaPsiElement element, TextRange rangeInElement) {
    super(element, rangeInElement);
  }

  @Override public @NotNull @NlsSafe String getCanonicalText() {
    var resolved = resolve();
    if (resolved != null) return resolved.canonicalName();
    return super.getCanonicalText();
  }

  @Override public @Nullable AyaPsiNamedElement resolve() {
    return AyaLsp.use(myElement.getProject(), lsp -> {
      var def = lsp.gotoDefinition(myElement).firstOrNull();
      return PsiTreeUtil.getParentOfType(def, AyaPsiNamedElement.class);
    }, () -> null);
  }
}
