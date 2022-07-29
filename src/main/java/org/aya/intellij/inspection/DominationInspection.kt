package org.aya.intellij.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaBundle
import org.aya.intellij.lsp.AyaLsp
import org.aya.intellij.psi.AyaPsiElement
import org.aya.intellij.psi.concrete.AyaPsiBareClause
import org.aya.intellij.psi.concrete.AyaPsiBarredClause
import org.aya.intellij.psi.concrete.AyaPsiClause
import org.aya.intellij.psi.concrete.AyaPsiVisitor
import org.aya.tyck.pat.ClausesProblem

class DominationInspection : AyaInspection() {
  override fun getDisplayName() = AyaBundle.message("aya.insp.dom")

  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    override fun visitBarredClause(c: AyaPsiBarredClause) = test(c, c.clause)
    override fun visitBareClause(c: AyaPsiBareClause) = test(c, c.clause)

    // Warnings in ClausesProblem are all about domination.
    fun test(whole: AyaPsiElement, clause: AyaPsiClause) = lsp.warningsAt(clause, ClausesProblem::class.java).forEach { _ ->
      holder.registerProblem(
        holder.manager.createProblemDescriptor(
          whole, whole,
          CommonQuickFixBundle.message("fix.remove.redundant", whole.text),
          ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly,
          object : LocalQuickFix {
            override fun getFamilyName() = CommonQuickFixBundle.message("fix.simplify")
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) = descriptor.psiElement.delete()
          },
        ),
      )
    }
  }
}
