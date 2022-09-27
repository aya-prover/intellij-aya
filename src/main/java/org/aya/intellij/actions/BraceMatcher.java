package org.aya.intellij.actions;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.aya.intellij.psi.types.AyaPsiElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BraceMatcher implements PairedBraceMatcher {
  private static final BracePair @NotNull [] PAIRS = new BracePair[]{
    new BracePair(AyaPsiElementTypes.LBRACE, AyaPsiElementTypes.RBRACE, true),
    new BracePair(AyaPsiElementTypes.LPAREN, AyaPsiElementTypes.RPAREN, false),
    new BracePair(AyaPsiElementTypes.LGOAL, AyaPsiElementTypes.RGOAL, false),
    new BracePair(AyaPsiElementTypes.LARRAY, AyaPsiElementTypes.RARRAY, false),
    new BracePair(AyaPsiElementTypes.LIDIOM, AyaPsiElementTypes.RIDIOM, false),
    new BracePair(AyaPsiElementTypes.LPARTIAL, AyaPsiElementTypes.RPARTIAL, false),
    new BracePair(AyaPsiElementTypes.LPATH, AyaPsiElementTypes.RPATH, false),
  };

  @Override public BracePair @NotNull [] getPairs() {
    return PAIRS;
  }

  @Override public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbrace, @Nullable IElementType ctx) {
    return true;
  }

  @Override public int getCodeConstructStart(@NotNull PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
