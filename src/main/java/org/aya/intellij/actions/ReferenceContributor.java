package org.aya.intellij.actions;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiReference;
import org.aya.intellij.psi.concrete.AyaPsiAtomPattern;
import org.aya.intellij.psi.concrete.AyaPsiNewExpr;
import org.aya.intellij.psi.concrete.AyaPsiProjExpr;
import org.aya.intellij.psi.concrete.AyaPsiRefExpr;
import org.jetbrains.annotations.NotNull;

/**
 * Traverse referring terms and collect references to {@link org.aya.ref.Var}s.
 *
 * @see org.aya.lsp.utils.Resolver.ReferringResolver
 * @see AyaPsiReference#resolve()
 */
public class ReferenceContributor extends PsiReferenceContributor {
  private static final @NotNull ImmutableSeq<Class<? extends AyaPsiElement>> REFERRING_TERMS = ImmutableSeq.of(
    AyaPsiRefExpr.class,
    AyaPsiProjExpr.class,
    AyaPsiNewExpr.class,
    AyaPsiAtomPattern.class
  );

  private final @NotNull Provider provider = new Provider();

  @Override public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    REFERRING_TERMS.forEach(pat -> registrar.registerReferenceProvider(
      PlatformPatterns.psiElement(pat),
      provider
    ));
  }

  private static final class Provider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
      if (!(element instanceof AyaPsiElement psi)) return PsiReference.EMPTY_ARRAY;
      return new PsiReference[]{new AyaPsiReference(psi, new TextRange(0, psi.getTextLength()))};
    }
  }
}
