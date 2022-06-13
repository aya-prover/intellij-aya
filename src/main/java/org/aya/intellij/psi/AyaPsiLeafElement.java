package org.aya.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.psi.ANTLRPsiLeafNode;
import org.jetbrains.annotations.NotNull;

public class AyaPsiLeafElement extends ANTLRPsiLeafNode {
  public AyaPsiLeafElement(@NotNull IElementType type, @NotNull CharSequence text) {
    super(type, text);
  }
}
