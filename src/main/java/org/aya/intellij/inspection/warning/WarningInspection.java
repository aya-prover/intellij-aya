package org.aya.intellij.inspection.warning;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiFile;
import com.siyeh.ig.fixes.RenameFix;
import kala.collection.SeqView;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.actions.lsp.AyaLsp;
import org.aya.intellij.actions.lsp.JB;
import org.aya.intellij.inspection.CatchAll;
import org.aya.intellij.inspection.Jobs;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.service.DistillerService;
import org.aya.tyck.error.PatternProblem;
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

  @Override
  protected void registerProblem(@NotNull AyaLsp lsp, @NotNull PsiFile file, @NotNull ProblemsHolder holder, @NotNull Problem problem) {
    var problemDesc = DistillerService.plainBrief(problem);

    switch (problem) {
      case PatternProblem.UnimportedConName(var pat) -> {
        var bindPos = pat.sourcePos();
        var element = JB.elementAt(holder.getFile(), bindPos, AyaPsiNamedElement.class);
        if (element == null) {
          super.registerProblem(lsp, file, holder, problem);
        } else {
          // TODO: import unimported con
          holder.registerProblem(element, problemDesc, highlightType, null, new LocalQuickFix[]{
            new RenameFix()
          });
        }
      }
      default -> super.registerProblem(lsp, file, holder, problem);
    }
  }
}
