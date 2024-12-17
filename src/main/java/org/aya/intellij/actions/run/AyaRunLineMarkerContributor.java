package org.aya.intellij.actions.run;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.concrete.AyaPsiDecl;
import org.aya.intellij.psi.concrete.AyaPsiDeclNameOrInfix;
import org.aya.intellij.psi.concrete.AyaPsiPrimName;
import org.aya.intellij.ui.AyaIcons;
import org.aya.parser.AyaPsiElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaRunLineMarkerContributor extends RunLineMarkerContributor {
  public static final @NotNull ElementPattern<PsiElement> TOP_LEVEL_DECL_ID = StandardPatterns.or(
    PlatformPatterns.psiFile(AyaPsiFile.class),
    topId(AyaPsiElementTypes.ID),
    topId(AyaPsiElementTypes.REPL_COMMAND)
  );

  private static @NotNull ElementPattern<PsiElement> topId(@NotNull IElementType type) {
    return PlatformPatterns.psiElement(type)
      .withSuperParent(3, PlatformPatterns.psiElement(AyaPsiDecl.class))
      .withSuperParent(2, StandardPatterns.or(
        PlatformPatterns.psiElement(AyaPsiDeclNameOrInfix.class),
        PlatformPatterns.psiElement(AyaPsiPrimName.class)
      ));
  }

  @Override public @Nullable Info getInfo(@NotNull PsiElement psi) {
    if (!TOP_LEVEL_DECL_ID.accepts(psi)) return null;
    final var actions = ExecutorAction.getActions(Integer.MAX_VALUE);
    return new Info(AyaIcons.GUTTER_RUN, actions);
  }
}
