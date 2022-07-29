package org.aya.intellij.inspection

import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException
import org.aya.intellij.AyaBundle.message
import org.aya.intellij.lsp.AyaLsp

abstract class AyaInspection : LocalInspectionTool() {
  override fun isEnabledByDefault() = true

  override fun getGroupDisplayName() = message("aya.group.name")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = AyaLsp.use<_, IncorrectOperationException>(
    holder.project,
    { lsp -> buildVisitor(lsp, holder, isOnTheFly) },
    { super.buildVisitor(holder, isOnTheFly) },
  )

  abstract fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor

  class Provider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<out AyaInspection>> = arrayOf(
      DominationInspection::class.java,
      ShadowingInspection::class.java,
      BadModifierInspection::class.java,
      BadCounterexampleInspection::class.java,
    )
  }
}
