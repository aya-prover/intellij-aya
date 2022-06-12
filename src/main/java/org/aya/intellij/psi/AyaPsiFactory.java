package org.aya.intellij.psi;

import com.intellij.lang.DefaultASTFactoryImpl;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.psi.ANTLRPsiLeafNode;
import org.jetbrains.annotations.NotNull;

public class AyaPsiFactory extends DefaultASTFactoryImpl {
  @Override public @NotNull LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
    return new ANTLRPsiLeafNode(type, text);
  }
}
