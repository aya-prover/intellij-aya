package org.aya.intellij.inspection.info;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.AyaInspection;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.concrete.AyaPsiVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class InfoInspection extends AyaInspection {
  @Override public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getDisplayName() {
    return AyaBundle.INSTANCE.message("aya.insp.info");
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull AyaLsp lsp, @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new AyaPsiVisitor() {
      @Override public void visitFile(@NotNull PsiFile file) {
        lsp.infosInFile(file).forEach(info -> holder.registerProblem(
          file, info.brief(distillerOptions()).debugRender(),
          ProblemHighlightType.INFORMATION, JB.toRange(info.sourcePos())
        ));
      }
    };
  }
}
