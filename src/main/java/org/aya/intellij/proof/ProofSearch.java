package org.aya.intellij.proof;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FindSymbolParameters;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Either;
import org.aya.core.term.Term;
import org.aya.intellij.AyaIcons;
import org.aya.intellij.actions.SearchEverywhere;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.service.DistillerService;
import org.aya.ref.DefVar;
import org.jetbrains.annotations.NotNull;

public interface ProofSearch {
  sealed interface Proof permits Proof.Err, Proof.Yes {
    record Err(@NotNull String message) implements Proof {}
    record Yes(@NotNull DefVar<?, ?> defVar, @NotNull AyaPsiElement element) implements Proof {}

    default @NotNull PresentationData presentation() {
      return switch (this) {
        case Err err -> new PresentationData(err.message, null, AyaIcons.PROOF_SEARCH_ERROR, null);
        case Yes yes -> {
          var pre = yes.element.ayaPresentation(true);
          pre.setTooltip(DistillerService.solution(yes.defVar.core.result()));
          yield pre;
        }
      };
    }
  }

  sealed interface PSTerm {
    record Ref(@NotNull String name) implements PSTerm {}
    record App(@NotNull PSTerm head, @NotNull ImmutableSeq<PSTerm> spine) implements PSTerm {}
    record Licit(boolean explicit, @NotNull PSTerm term) implements PSTerm {}
    record CalmFace() implements PSTerm {}
  }

  static @NotNull SeqView<Proof> search(@NotNull Project project, boolean everywhere, @NotNull String pattern) {
    return parse(pattern).fold(
      err -> SeqView.of(new Proof.Err(err)),
      ps -> {
        var scope = FindSymbolParameters.searchScopeFor(project, everywhere);
        return SearchEverywhere.searchGenericDecl(project, scope)
          .filter(t -> matches(ps, t._1))
          .map(t -> new Proof.Yes(t._1, t._2));
      }
    );
  }

  private static boolean matches(@NotNull PSTerm ps, @NotNull DefVar<?, ?> defVar) {
    return true;
  }

  private static boolean matches(@NotNull PSTerm ps, @NotNull Term term) {
    return false;
  }

  private static @NotNull Either<String, PSTerm> parse(@NotNull String pattern) {
    return Either.right(new PSTerm.CalmFace());
  }
}
