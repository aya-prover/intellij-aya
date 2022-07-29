package org.aya.intellij.actions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.resolve.error.*;
import org.aya.tyck.error.FieldProblem;
import org.aya.tyck.error.ProjIxError;
import org.aya.tyck.pat.PatternProblem;
import org.jetbrains.annotations.NotNull;

public class ErrorAnnotator implements AyaAnnotator {
  @Override public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof AyaPsiFile file)) return;
    annotate(file, holder, lsp -> lsp.errorsInFile(file), ((problem, builder) -> {
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
      return builder;
    }));
  }
}
