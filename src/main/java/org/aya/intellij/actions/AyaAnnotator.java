package org.aya.intellij.actions;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import kala.collection.SeqLike;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.util.distill.DistillerOptions;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface AyaAnnotator extends Annotator {
  default <T extends Problem> void annotate(
    @NotNull AyaPsiFile file,
    @NotNull AnnotationHolder holder,
    @NotNull Function<@NotNull AyaLsp, SeqLike<T>> problems,
    @NotNull BiFunction<T, AnnotationBuilder, AnnotationBuilder> annotator
  ) {
    AyaLsp.use(file.getProject(), lsp -> problems.apply(lsp).forEach(problem -> {
      var options = DistillerOptions.informative();
      var message = problem.describe(options).debugRender();
      var tooltip = problem.brief(options).debugRender();
      var range = JB.toRange(problem.sourcePos());
      var builder = holder.newAnnotation(severityOf(problem), message)
        .range(range)
        .tooltip(tooltip);
      annotator.apply(problem, builder).create();
    }));
  }

  private static @NotNull HighlightSeverity severityOf(@NotNull Problem problem) {
    return switch (problem.level()) {
      case ERROR -> HighlightSeverity.ERROR;
      case GOAL -> HighlightSeverity.WEAK_WARNING;
      case WARN -> HighlightSeverity.WARNING;
      case INFO -> HighlightSeverity.INFORMATION;
    };
  }
}
