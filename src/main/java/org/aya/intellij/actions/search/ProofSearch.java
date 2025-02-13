package org.aya.intellij.actions.search;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FindSymbolParameters;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Either;
import org.aya.generic.InterruptException;
import org.aya.intellij.language.AyaIJParserImpl;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.service.DistillerService;
import org.aya.intellij.ui.AyaIcons;
import org.aya.prettier.AyaPrettierOptions;
import org.aya.syntax.concrete.Expr;
import org.aya.syntax.concrete.stmt.QualifiedID;
import org.aya.syntax.core.def.TyckAnyDef;
import org.aya.syntax.core.def.TyckDef;
import org.aya.syntax.core.term.Term;
import org.aya.syntax.ref.DefVar;
import org.aya.util.position.SourcePos;
import org.aya.util.position.WithPos;
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
          var solution = TyckDef.defType(new TyckAnyDef<>(yes.defVar));
          if (solution != null) pre.setTooltip(DistillerService.solution(solution));
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
          .filter(t -> matches(ps, t.defVar()))
          .map(t -> new Proof.Yes(t.defVar(), t.decl()));
      }
    );
  }

  private static boolean matches(@NotNull ProofShape ps, @NotNull DefVar<?, ?> defVar) {
    if (defVar.core == null) return false;
    Term term = TyckDef.defType(new TyckAnyDef<>(defVar));
    if (term != null) return matches(ps, term);
    return false;
  }

  private static boolean matches(@NotNull ProofShape ps, @NotNull Term term) {
    // TODO: structural comparison
    var doc = DistillerService.solution(term);
    var pattern = Pattern.compile(compile(0, ps));
    return pattern.matcher(doc).matches();
  }

  static @NotNull String compile(int nested, @NotNull ProofShape ps) {
    return switch (ps) {
      case ProofShape.App app when app.terms.sizeEquals(1) -> compile(nested, app.terms.getFirst().shape);
      case ProofShape.App app -> paren(nested, app.terms.map(arg ->
        braced(arg.explicit(), compile(nested + 1, arg.shape))));
      case ProofShape.AnyId _ -> "((?![ (){}:]).)+";
      case ProofShape.CalmFace _ -> "(.+)";
      case ProofShape.Ref ref -> Pattern.quote(ref.name.name());
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
    if (pattern.isBlank()) return Either.left("Pattern is empty");
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

  static @NotNull ProofShape parse(@NotNull WithPos<Expr> expr) {
    return switch (expr.data()) {
      case Expr.Hole _ -> new ProofShape.CalmFace();
      case Expr.Unresolved e -> e.name().join().equals("?") ? new ProofShape.AnyId() : new ProofShape.Ref(e.name());
      case Expr.BinOpSeq (var seq) -> new ProofShape.App(seq
        .map(arg -> new ProofShape.Arg(parse(arg.term()), arg.explicit())));
      // TODO: more?
      case Expr misc -> throw new PatternNotSupported(misc);
    };
  }

  class PatternNotSupported extends RuntimeException {
    PatternNotSupported(@NotNull Expr message) {
      super(message.toDoc(AyaPrettierOptions.pretty()).debugRender());
    }
  }
}
