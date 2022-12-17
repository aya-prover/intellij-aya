package org.aya.intellij.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.aya.intellij.psi.AyaPsiNamedWeakId;
import org.aya.intellij.psi.utils.AyaPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiNamedWeakIdImpl extends AyaPsiNamedElementImpl implements AyaPsiNamedWeakId {
  public AyaPsiNamedWeakIdImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override public @Nullable PsiElement getNameIdentifier() {
    return AyaPsiUtils.getNameIdFromWeakIdChild(this);
  }

  @Override public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
    var nameId = getNameIdentifier();
    return AyaPsiUtils.setNameIdToWeakIdChild(this, name, nameId);
  }
}
