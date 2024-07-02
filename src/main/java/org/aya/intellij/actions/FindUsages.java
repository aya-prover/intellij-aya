package org.aya.intellij.actions;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.aya.intellij.language.AyaParserDefinition;
import org.aya.intellij.language.AyaWordsScanner;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.psi.concrete.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FindUsages implements FindUsagesProvider {
  @Override public @Nullable WordsScanner getWordsScanner() {
    return new AyaWordsScanner(AyaParserDefinition.createIJLexer(), AyaParserDefinition.IDENTIFIERS,
      AyaParserDefinition.COMMENTS, AyaParserDefinition.STRINGS);
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psi) {
    return psi instanceof AyaPsiNamedElement;
  }

  @Override
  public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  public @Nls @NotNull String getType(@NotNull PsiElement element) {
    if (!(element instanceof AyaPsiNamedElement named)) return "";
    return switch (named) {
      case AyaPsiFnDecl _ -> "Function";
      case AyaPsiDataDecl _ -> "Data";
      case AyaPsiClassDecl _ -> "Class";
      case AyaPsiPrimDecl _ -> "Primitive";
      case AyaPsiDataBody _ -> "Data constructor";
      case AyaPsiClassMember _ -> "Class member";
      case AyaPsiTeleParamName _ -> "Parameter";
      // The PSI parser does not distinguish between a bind pattern and a constructor pattern.
      // But the following match case does not cause constructor patterns to be described as "Pattern Binding"
      // because this method is always called with the resolved results returned by `AyaPsiReference#resolve()`
      // which calls the Aya compiler who knows the truth. So constructor patterns will be described
      // by former cases like `AyaPsiFnDecl`, `AyaPsiDataDecl`, etc.
      case AyaPsiAtomBindPattern _ -> "Pattern binding";
      case AyaPsiDoBinding _ -> "do-binding";
      case AyaPsiLetBind _ -> "let-binding";
      default -> "";
    };
  }

  @Override
  public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
    if (!(element instanceof AyaPsiNamedElement named)) return "";
    return named.canonicalName();
  }

  @Override
  public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return element instanceof AyaPsiNamedElement named
      ? useFullName ? named.canonicalName() : named.nameOrEmpty()
      : "";
  }
}
