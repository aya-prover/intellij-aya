package org.aya.intellij.inspection.error;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.CatchAll;
import org.aya.intellij.inspection.Jobs;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

public class ErrorInspection extends CatchAll {
  protected static final @NotNull Jobs JOBS = new Jobs();

  protected ErrorInspection() {
    super(AyaBundle.INSTANCE.message("aya.insp.error"), ProblemHighlightType.GENERIC_ERROR);
  }

  @Override
  protected @NotNull SeqView<Problem> catchAll(@NotNull AyaLsp lsp, @NotNull PsiFile file) {
    return JOBS.findMyJob(lsp.errorsInFile(file));
  }
}
