package org.aya.intellij.psi;

import com.intellij.lang.ASTNode;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

public class AyaPsiElement extends ANTLRPsiNode {
  public AyaPsiElement(@NotNull ASTNode node) {
    super(node);
  }
}
