package org.aya.intellij.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

public interface AyaPsiNamedElement extends AyaPsiElement, PsiNameIdentifierOwner {
  default @NotNull String nameOrEmpty() {
    var name = getName();
    return name != null ? name : "";
  }
}
