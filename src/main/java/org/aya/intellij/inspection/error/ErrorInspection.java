package org.aya.intellij.inspection.error;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.CatchAll;
import org.aya.intellij.inspection.Jobs;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.service.DistillerService;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

public class ErrorInspection extends CatchAll {
  protected static final @NotNull Jobs JOBS = new Jobs();

  protected ErrorInspection() {
    super(AyaBundle.INSTANCE.message("aya.insp.error"), ProblemHighlightType.GENERIC_ERROR);
  }

  @Override
  protected @NotNull SeqView<Problem> catchAll(@NotNull AyaLsp lsp, @NotNull PsiFile file) {
    return SeqView.empty();
  }

  public static class ErrorAnnotator implements Annotator {
    @Override public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
      if (!(element instanceof AyaPsiFile file)) return;
      AyaLsp.use(file.getProject(), lsp -> JOBS.findMyJob(lsp.errorsInFile(file)).forEach(p -> {
        var range = JB.toRange(p.sourcePos());
        var msg = DistillerService.plainDescribe(p);
        var tooltip = DistillerService.escapedBrief(p);
        holder.newAnnotation(HighlightSeverity.ERROR, msg)
          .tooltip(tooltip)
          .range(range).create();
      }));
    }
  }
}
