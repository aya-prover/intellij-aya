package org.aya.intellij.inspection.warning

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import org.aya.concrete.error.BadModifierWarn
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.concrete.AyaPsiDeclModifiers
import org.aya.intellij.psi.concrete.AyaPsiVisitor

class BadModifierInspection : WarningInspection() {
  companion object {
    init {
      JOBS.passMe(BadModifierWarn::class.java)
    }
  }

  override fun getDisplayName() = AyaBundle.message("aya.insp.bad.modifier")

  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    // TODO: also ModifierProblem::class.java
    override fun visitDeclModifiers(mod: AyaPsiDeclModifiers) = lsp.warningsAt(mod, BadModifierWarn::class.java).forEach { _ ->
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
