package org.aya.intellij.inspection.warning;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.CatchAll;
import org.aya.intellij.inspection.Jobs;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

public class WarningInspection extends CatchAll {
  protected static final @NotNull Jobs JOBS = new Jobs();

  public WarningInspection() {
    super(AyaBundle.INSTANCE.message("aya.insp.warning"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
  }

  @Override protected @NotNull SeqView<Problem> catchAll(@NotNull AyaLsp lsp, @NotNull PsiFile file) {
    return JOBS.findMyJob(lsp.warningsInFile(file));
  }
}
