package org.aya.intellij.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.aya.intellij.psi.AyaPsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class AyaPsiElementImpl extends ASTWrapperPsiElement implements AyaPsiElement {
  public AyaPsiElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  /** https://intellij-support.jetbrains.com/hc/en-us/community/posts/206117609-Problems-to-add-PsiReferenceContributor */
  @Override public PsiReference @NotNull [] getReferences() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }
}
