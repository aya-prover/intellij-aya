package org.aya.intellij.inspection.warning

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import org.aya.concrete.error.BadCounterexampleWarn
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.concrete.AyaPsiOpenKw
import org.aya.intellij.psi.concrete.AyaPsiVisitor

class BadCounterexampleInspection : WarningInspection() {
  companion object {
    init {
      JOBS.passMe(BadCounterexampleWarn::class.java)
    }
  }

  override fun getDisplayName() = AyaBundle.message("aya.insp.bad.counter")

  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    override fun visitOpenKw(op: AyaPsiOpenKw) = lsp.warningsAt(op, BadCounterexampleWarn::class.java).forEach { _ ->
      holder.registerProblem(
        holder.manager.createProblemDescriptor(
          op, op,
          AyaBundle.message("aya.insp.bad.counter"),
          ProblemHighlightType.LIKE_DEPRECATED, isOnTheFly,
          object : LocalQuickFix {
            override fun getFamilyName() = CommonQuickFixBundle.message("fix.remove", op.text)
            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
              descriptor.psiElement.prevSibling.delete() // the whitespace
              descriptor.psiElement.delete()
            }
          },
        ),
      )
    }
  }
}
