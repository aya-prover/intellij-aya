package org.aya.intellij.actions;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiReference;
import org.aya.intellij.psi.concrete.AyaPsiAtomBindPattern;
import org.aya.intellij.psi.concrete.AyaPsiProjFixId;
import org.aya.intellij.psi.concrete.AyaPsiRefExpr;
import org.jetbrains.annotations.NotNull;

/**
 * Traverse referring terms and collect references to {@link org.aya.syntax.ref.AnyVar}s.
 *
 * @see org.aya.syntax.concrete.stmt.StmtVisitor
 * @see AyaPsiReference#resolve()
 */
public class ReferenceContributor extends PsiReferenceContributor {
  /** @implSpec Keep sync with {@link org.aya.syntax.concrete.stmt.StmtVisitor} */
  public static final ElementPattern<AyaPsiElement> REFERRING_TERMS = StandardPatterns.or(
    PlatformPatterns.psiElement(AyaPsiProjFixId.class),
    PlatformPatterns.psiElement(AyaPsiRefExpr.class),
    PlatformPatterns.psiElement(AyaPsiAtomBindPattern.class) // If the bind resolves to a constructor.
  );

  @Override public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(REFERRING_TERMS, new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof AyaPsiElement psi)) return PsiReference.EMPTY_ARRAY;
        return pack(psi);
      }
    });
  }

  @NotNull private static PsiReference[] pack(AyaPsiElement psi) {
    return new PsiReference[]{new AyaPsiReference(psi, new TextRange(0, psi.getTextLength()))};
  }
}
