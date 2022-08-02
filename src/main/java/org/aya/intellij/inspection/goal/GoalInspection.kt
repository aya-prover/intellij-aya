package org.aya.intellij.inspection.goal

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaBundle
import org.aya.intellij.inspection.AyaInspection
import org.aya.intellij.lsp.AyaLsp
import org.aya.intellij.psi.concrete.AyaPsiHoleExpr
import org.aya.intellij.psi.concrete.AyaPsiVisitor
import org.aya.intellij.psi.utils.AyaPsiFactory
import org.aya.intellij.service.DistillerOptionsService
import org.aya.tyck.error.Goal

class GoalInspection : AyaInspection() {
  private class Fix(val solution: String) : LocalQuickFix {
    override fun getFamilyName() = AyaBundle.message("aya.insp.goal.apply", solution)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      descriptor.psiElement.replace(AyaPsiFactory.expr(project, solution))
    }
  }

  override fun getDisplayName() = AyaBundle.message("aya.insp.goal")

  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    override fun visitHoleExpr(hole: AyaPsiHoleExpr) = lsp.goalsAt(hole).forEach { goal ->
      // Only show goals that have solution. We show all goals in the tool window.
      val fix = candidate(goal)?.let { arrayOf(Fix(it)) } ?: return@forEach
      holder.registerProblem(
        holder.manager.createProblemDescriptor(
          hole, hole, AyaBundle.message("aya.insp.goal.solved"),
          ProblemHighlightType.WARNING, isOnTheFly, *fix,
        ),
      )
    }
  }

  private fun candidate(goal: Goal): String? {
    val metas = goal.state().metas()
    val meta = goal.hole().ref()
    return if (metas.containsKey(meta)) metas[meta].toDoc(DistillerOptionsService.goalSolution()).debugRender() else null
  }
}
