package org.aya.intellij.actions.search;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FindSymbolParameters;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Either;
import org.aya.concrete.Expr;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.core.term.Term;
import org.aya.distill.AyaDistillerOptions;
import org.aya.generic.util.InterruptException;
import org.aya.intellij.language.AyaIJParserImpl;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.service.DistillerService;
import org.aya.intellij.ui.AyaIcons;
import org.aya.ref.DefVar;
import org.aya.util.error.SourcePos;
import org.aya.util.reporter.BufferReporter;
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
    record Ref(@NotNull QualifiedID name) implements ProofShape {}
    record App(@NotNull ImmutableSeq<Arg> terms) implements ProofShape {}
    record CalmFace() implements ProofShape {}
    record AnyId() implements ProofShape {}
    record Arg(@NotNull ProofShape shape, boolean explicit) {}
  }

  static @NotNull SeqView<Proof> search(@NotNull Project project, boolean everywhere, @NotNull String pattern) {
    return parse(project, pattern).fold(
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
    var pattern = Pattern.compile(compile(0, ps));
    return pattern.matcher(doc).matches();
  }

  static @NotNull String compile(int nested, @NotNull ProofShape ps) {
    return switch (ps) {
      case ProofShape.App app && app.terms.sizeEquals(1) -> compile(nested, app.terms.first().shape);
      case ProofShape.App app -> paren(nested, app.terms.map(arg ->
        braced(arg.explicit(), compile(nested + 1, arg.shape))));
      case ProofShape.AnyId $ -> "((?![ (){}:]).)+";
      case ProofShape.CalmFace $ -> "(.+)";
      case ProofShape.Ref ref -> Pattern.quote(ref.name.justName());
    };
  }

  private static @NotNull String braced(boolean explicit, @NotNull String s) {
    return explicit ? s : "\\{" + s + "\\}";
  }

  private static @NotNull String paren(int nested, @NotNull ImmutableSeq<String> args) {
    var app = args.joinToString(" ");
    return nested == 0 ? app : "\\(" + app + "\\)";
  }

  static @NotNull Either<String, ProofShape> parse(@NotNull Project project, @NotNull String pattern) {
    var reporter = new BufferReporter();
    try {
      var parser = new AyaIJParserImpl(project, reporter);
      return Either.right(parse(parser.expr(pattern, SourcePos.NONE)));
    } catch (PatternNotSupported e) {
      return Either.left("Pattern `%s` was not supported".formatted(e.getMessage()));
    } catch (InterruptException ignored) {
      return Either.left(reporter.problems().view()
        .map(DistillerService::plainBrief)
        .joinToString(";"));
    }
  }

  static @NotNull ProofShape parse(@NotNull Expr expr) {
    return switch (expr) {
      case Expr.Hole hole -> new ProofShape.CalmFace();
      case Expr.Unresolved e -> e.name().join().equals("?") ? new ProofShape.AnyId() : new ProofShape.Ref(e.name());
      case Expr.BinOpSeq seq -> new ProofShape.App(seq.seq().view()
        .map(arg -> new ProofShape.Arg(parse(arg.term()), arg.explicit()))
        .toImmutableSeq());
      // TODO: more?
      case Expr misc -> throw new PatternNotSupported(misc);
    };
  }

  class PatternNotSupported extends RuntimeException {
    PatternNotSupported(@NotNull Expr message) {
      super(message.toDoc(AyaDistillerOptions.pretty()).debugRender());
    }
  }
}
