package org.aya.intellij.inspection.warning

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.AyaPsiElement
import org.aya.intellij.psi.concrete.AyaPsiBareClause
import org.aya.intellij.psi.concrete.AyaPsiBarredClause
import org.aya.intellij.psi.concrete.AyaPsiClause
import org.aya.intellij.psi.concrete.AyaPsiVisitor
import org.aya.tyck.error.ClausesProblem
import org.aya.tyck.error.ClausesProblem.Domination
import org.aya.tyck.error.ClausesProblem.FMDomination

class DominationInspection : WarningInspection() {
  companion object {
    init {
      JOBS.passMe(FMDomination::class.java)
      JOBS.passMe(Domination::class.java)
    }
  }

  override fun getDisplayName() = AyaBundle.message("aya.insp.dom")

  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    override fun visitBarredClause(c: AyaPsiBarredClause) = test(c, c.clause)
    override fun visitBareClause(c: AyaPsiBareClause) = test(c, c.clause)

    // Warnings in ClausesProblem are all about domination.
    fun test(whole: AyaPsiElement, clause: AyaPsiClause) = lsp.warningsAt(clause, ClausesProblem::class.java).forEach { _ ->
      holder.registerProblem(
        holder.manager.createProblemDescriptor(
          whole, whole,
          AyaBundle.message("aya.insp.dom"),
          ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
          object : LocalQuickFix {
            override fun getFamilyName() = CommonQuickFixBundle.message("fix.remove.redundant", whole.text)
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) = descriptor.psiElement.delete()
          },
        ),
      )
    }
  }
}
