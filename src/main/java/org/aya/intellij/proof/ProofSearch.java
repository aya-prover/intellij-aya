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

  /** Aya Proof Query Language */
  sealed interface AyaPQL {
    record Ref(@NotNull String name) implements AyaPQL {}
    record App(@NotNull ImmutableSeq<AyaPQL> terms) implements AyaPQL {}
    record Licit(boolean explicit, @NotNull AyaPQL term) implements AyaPQL {}
    record CalmFace() implements AyaPQL {}
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

  private static boolean matches(@NotNull AyaPQL ps, @NotNull DefVar<?, ?> defVar) {
    if (defVar.core == null) return false;
    return matches(ps, defVar.core.result());
  }

  private static boolean matches(@NotNull AyaPQL ps, @NotNull Term term) {
    // TODO: structural comparison
    var doc = DistillerService.solution(term);
    var compiled = compile(ps);
    System.out.println("PQL compiled: " + compiled);
    var pattern = Pattern.compile(compiled);
    return pattern.matcher(doc).matches();
  }

  private static @NotNull String compile(@NotNull AyaPQL ps) {
    return switch (ps) {
      case AyaPQL.App app -> app.terms.map(ProofSearch::compile).joinToString(" ");
      case AyaPQL.CalmFace $ -> "(.+)";
      case AyaPQL.Licit licit -> licit.explicit
        ? "\\(" + compile(licit.term) + "\\)"
        : "\\{" + compile(licit.term) + "\\}";
      case AyaPQL.Ref ref -> Pattern.quote(ref.name);
    };
  }

  private static @NotNull Either<String, AyaPQL> parse(@NotNull String pattern) {
    return Either.left("PQL parsing not implemented: " + pattern);
  }
}
