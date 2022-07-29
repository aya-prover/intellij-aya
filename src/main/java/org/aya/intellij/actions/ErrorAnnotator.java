package org.aya.intellij.actions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.resolve.error.*;
import org.aya.tyck.error.FieldProblem;
import org.aya.tyck.error.ProjIxError;
import org.aya.tyck.pat.PatternProblem;
import org.aya.util.distill.DistillerOptions;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

public class ErrorAnnotator implements Annotator {
  @Override public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof AyaPsiFile file)) return;
    AyaLsp.use(element.getProject(), lsp -> lsp.errorsInFile(file).forEach(problem -> {
      var options = DistillerOptions.informative();
      var message = problem.describe(options).debugRender();
      var tooltip = problem.brief(options).debugRender();
      var range = JB.toRange(problem.sourcePos());
      var builder = holder.newAnnotation(severityOf(problem), message)
        .range(range)
        .tooltip(tooltip);
      if (problem instanceof UnqualifiedNameNotFoundError
        || problem instanceof QualifiedNameNotFoundError
        || problem instanceof ModNotFoundError
        || problem instanceof ModNameNotFoundError
        || problem instanceof UnknownPrimError
        || problem instanceof UnknownOperatorError
        || problem instanceof FieldProblem.UnknownField
        || problem instanceof PatternProblem.UnknownCtor
        || problem instanceof FieldProblem.NoSuchFieldError
        || problem instanceof ProjIxError) {
        builder = builder.highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
      }
      builder.create();
    }));
  }

  private @NotNull HighlightSeverity severityOf(@NotNull Problem problem) {
    return switch (problem.level()) {
      case ERROR -> HighlightSeverity.ERROR;
      case GOAL -> HighlightSeverity.WEAK_WARNING;
      case WARN -> HighlightSeverity.WARNING;
      case INFO -> HighlightSeverity.INFORMATION;
    };
  }
}
