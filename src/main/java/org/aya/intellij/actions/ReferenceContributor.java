package org.aya.intellij.actions;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.concrete.AyaPsiAtomPattern;
import org.aya.intellij.psi.concrete.AyaPsiNewArgField;
import org.aya.intellij.psi.concrete.AyaPsiProjFixId;
import org.aya.intellij.psi.concrete.AyaPsiRefExpr;
import org.aya.intellij.psi.ref.AyaPsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Traverse referring terms and collect references to {@link org.aya.ref.Var}s.
 *
 * @see org.aya.lsp.utils.Resolver.ReferringResolver
 * @see AyaPsiReference#resolve()
 */
public class ReferenceContributor extends PsiReferenceContributor {
  private static final @NotNull ImmutableSeq<Class<? extends AyaPsiElement>> REFERRING_TERMS = ImmutableSeq.of(
    AyaPsiProjFixId.class,
    AyaPsiNewArgField.class,
    AyaPsiRefExpr.class,
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
      return switch (element) {
        case AyaPsiProjFixId fix -> pack(fix);
        case AyaPsiNewArgField field -> pack(field);
        case AyaPsiRefExpr ref -> pack(ref);
        case AyaPsiAtomPattern pat -> pack(pat);
        default -> PsiReference.EMPTY_ARRAY;
      };
    }

    private static PsiReference @NotNull [] pack(@NotNull AyaPsiElement... element) {
      return Arrays.stream(element)
        .map(e -> new AyaPsiReference(e, new TextRange(0, e.getTextLength())))
        .toArray(PsiReference[]::new);
    }
  }
}
