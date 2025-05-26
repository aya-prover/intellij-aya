package org.aya.intellij.psi;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.aya.intellij.actions.ReferenceContributor;
import org.aya.intellij.actions.lsp.AyaLsp;
import org.aya.intellij.psi.concrete.AyaPsiAtomBindPattern;
import org.aya.intellij.psi.concrete.AyaPsiProjFixId;
import org.aya.intellij.psi.concrete.AyaPsiRefExpr;
import org.aya.intellij.psi.utils.AyaPsiFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiReference extends PsiReferenceBase<AyaPsiElement> {
  public AyaPsiReference(@NotNull AyaPsiElement element, TextRange rangeInElement) {
    super(element, rangeInElement);
  }

  @Override public @NotNull @NlsSafe String getCanonicalText() {
    var resolved = resolve();
    if (resolved != null) return resolved.canonicalName();
    return super.getCanonicalText();
  }

  @Override public @Nullable AyaPsiNamedElement resolve() {
    return AyaLsp.use(myElement.getProject(),
      () -> null,
      lsp -> lsp.gotoDefinition(myElement).getFirstOrNull());
  }

  @Override
  public Object @NotNull [] getVariants() {
    // TODO: thread-safe
    return AyaLsp.use(myElement.getProject(), super::getVariants, lsp ->
      lsp.collectCompletionItem(myElement).toArray(LookupElement.class));
  }

  @Override public PsiElement handleElementRename(@NotNull String newName) throws IncorrectOperationException {
    // Only renaming the referring terms is possible.
    if (!ReferenceContributor.REFERRING_TERMS.accepts(myElement))
      throw new IncorrectOperationException("Cannot rename " + myElement);
    var project = myElement.getProject();
    return switch (myElement) {
      case AyaPsiProjFixId fix -> fix.getQualifiedId().replace(AyaPsiFactory.qualifiedId(project, newName));
      case AyaPsiRefExpr ref -> ref.getQualifiedId().replace(AyaPsiFactory.qualifiedId(project, newName));
      case AyaPsiAtomBindPattern pat -> pat.setName(newName);
      default -> throw new IllegalStateException("unreachable");
    };
  }
}
