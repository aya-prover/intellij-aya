package org.aya.intellij.inspection.goal;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.AyaInspection;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.psi.concrete.AyaPsiHoleExpr;
import org.aya.intellij.psi.concrete.AyaPsiVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class GoalInspection extends AyaInspection {
  @Override public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getDisplayName() {
    return AyaBundle.INSTANCE.message("aya.insp.goal");
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull AyaLsp lsp, @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new AyaPsiVisitor() {
      @Override public void visitHoleExpr(@NotNull AyaPsiHoleExpr hole) {
        lsp.goalsAt(hole).forEach(goal -> {
          // TODO: make a friendly message
        });
      }
    };
  }
}
