package org.aya.intellij.psi.impl;

import com.intellij.lang.ASTNode;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AyaPsiNamedElementImpl extends AyaPsiElementImpl implements AyaPsiNamedElement {
  public AyaPsiNamedElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override public @Nullable String getName() {
    var id = getNameIdentifier();
    return id == null ? null : id.getText();
  }

  @Override public int getTextOffset() {
    var id = getNameIdentifier();
    return id == null ? super.getTextOffset() : id.getTextOffset();
  }
}
