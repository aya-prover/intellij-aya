package org.aya.intellij.psi.impl;

import com.intellij.lang.ASTNode;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class AyaPsiNamedElementImpl extends AyaPsiElementImpl implements AyaPsiNamedElement {
  public AyaPsiNamedElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override public String getName() {
    var id = getNameIdentifier();
    return id == null ? null : id.getText();
  }

  @Override public int getTextOffset() {
    var id = getNameIdentifier();
    return id == null ? super.getTextOffset() : id.getTextOffset();
  }
}
