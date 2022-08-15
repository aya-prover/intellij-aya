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

import java.util.regex.Pattern;

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

  sealed interface ProofShape {
    record Ref(@NotNull String name) implements ProofShape {}
    record App(@NotNull ImmutableSeq<ProofShape> terms) implements ProofShape {}
    record Licit(boolean explicit, @NotNull ProofShape term) implements ProofShape {}
    record CalmFace() implements ProofShape {}
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

  private static boolean matches(@NotNull ProofShape ps, @NotNull DefVar<?, ?> defVar) {
    if (defVar.core == null) return false;
    return matches(ps, defVar.core.result());
  }

  private static boolean matches(@NotNull ProofShape ps, @NotNull Term term) {
    // TODO: structural comparison
    var doc = DistillerService.solution(term);
    var compiled = compile(ps);
    System.out.println("PQL compiled: " + compiled);
    var pattern = Pattern.compile(compiled);
    return pattern.matcher(doc).matches();
  }

  private static @NotNull String compile(@NotNull ProofShape ps) {
    return switch (ps) {
      case ProofShape.App app -> app.terms.map(ProofSearch::compile).joinToString(" ");
      case ProofShape.CalmFace $ -> "(.+)";
      case ProofShape.Licit licit -> licit.explicit
        ? "\\(" + compile(licit.term) + "\\)"
        : "\\{" + compile(licit.term) + "\\}";
      case ProofShape.Ref ref -> Pattern.quote(ref.name);
    };
  }

  private static @NotNull Either<String, ProofShape> parse(@NotNull String pattern) {
    return Either.left("ProofShape parsing not implemented: " + pattern);
  }
}
