package org.aya.intellij.inspection.info;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.CatchAll;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

public class InfoInspection extends CatchAll {
  public InfoInspection() {
    super(AyaBundle.INSTANCE.message("aya.insp.info"), ProblemHighlightType.WEAK_WARNING);
  }

  @Override protected @NotNull SeqView<Problem> catchAll(@NotNull AyaLsp lsp, @NotNull PsiFile file) {
    return lsp.infosInFile(file);
  }
}
