package org.aya.intellij.inspection.error;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import kala.collection.mutable.MutableSet;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.inspection.AyaInspection;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.concrete.AyaPsiVisitor;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ErrorInspection extends AyaInspection {
  private static final @NotNull MutableSet<Class<? extends Problem>> NOT_MY_JOB = MutableSet.create();

  public static void take(@NotNull Class<? extends Problem> problemType) {
    synchronized (ErrorInspection.class) {
      NOT_MY_JOB.add(problemType);
    }
  }

  private static @NotNull SeqView<Problem> findMyJob(@NotNull AyaLsp lsp, @NotNull PsiFile file) {
    var errors = lsp.errorsInFile(file);
    synchronized (ErrorInspection.class) {
      return errors.filterNot(problem -> NOT_MY_JOB.contains(problem.getClass()));
    }
  }

  @Override public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getDisplayName() {
    return AyaBundle.INSTANCE.message("aya.insp.error");
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull AyaLsp lsp, @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new AyaPsiVisitor() {
      @Override public void visitFile(@NotNull PsiFile file) {
        findMyJob(lsp, file).forEach(problem -> holder.registerProblem(
          file, problem.brief(distillerOptions()).debugRender(),
          ProblemHighlightType.ERROR, JB.toRange(problem.sourcePos())
        ));
      }
    };
  }
}
