package org.aya.intellij.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public abstract class AyaPsiElementImpl extends ASTWrapperPsiElement {
  public AyaPsiElementImpl(@NotNull ASTNode node) {
    super(node);
  }
}
