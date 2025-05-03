package org.aya.intellij.inspection.warning

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.concrete.AyaPsiDeclModifier
import org.aya.intellij.psi.concrete.AyaPsiVisitor
import org.aya.producer.error.BadXWarn

class BadModifierInspection : WarningInspection() {
  companion object {
    init {
      JOBS.passMe(BadXWarn.OnlyExprBodyWarn::class.java)
    }
  }

  override fun getDisplayName() = AyaBundle.message("aya.insp.bad.modifier")

  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    // TODO: also ModifierProblem::class.java
    override fun visitDeclModifier(mod: AyaPsiDeclModifier) = lsp.warningsAt(mod, BadXWarn.OnlyExprBodyWarn::class.java).forEach { _ ->
      holder.registerProblem(
        holder.manager.createProblemDescriptor(
          mod, mod,
          AyaBundle.message("aya.insp.bad.modifier.info", mod.text),
          ProblemHighlightType.LIKE_DEPRECATED, isOnTheFly,
          object : LocalQuickFix {
            override fun getFamilyName() = CommonQuickFixBundle.message("fix.remove", mod.text)
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
