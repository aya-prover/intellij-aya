package org.aya.intellij.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.concrete.AyaPsiVisitor;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * the catch-all problem reporter
 */
public abstract class CatchAll extends AyaInspection {
  private final @NotNull String displayName;
  private final @NotNull ProblemHighlightType highlightType;

  protected CatchAll(@NotNull String displayName, @NotNull ProblemHighlightType highlightType) {
    this.displayName = displayName;
    this.highlightType = highlightType;
  }

  @Override
  public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDisplayName() {
    return displayName;
  }

  protected abstract @NotNull SeqView<Problem> catchAll(@NotNull AyaLsp lsp, @NotNull PsiFile file);

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull AyaLsp lsp, @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new AyaPsiVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile file) {
        catchAll(lsp, file).forEach(problem -> holder.registerProblem(
          file, problem.brief(distillerOptions()).debugRender(),
          highlightType, JB.toRange(problem.sourcePos())
        ));
      }
    };
  }
}
