package org.aya.intellij.inspection

import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException
import org.aya.intellij.AyaBundle.message
import org.aya.intellij.inspection.error.ErrorInspection
import org.aya.intellij.inspection.goal.GoalInspection
import org.aya.intellij.inspection.info.InfoInspection
import org.aya.intellij.inspection.warning.*
import org.aya.intellij.lsp.AyaLsp
import org.aya.util.distill.DistillerOptions

abstract class AyaInspection : LocalInspectionTool() {
  override fun isEnabledByDefault() = true

  override fun getGroupDisplayName() = message("aya.group.name")

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = AyaLsp.use<_, IncorrectOperationException>(
    holder.project,
    { lsp -> buildVisitor(lsp, holder, isOnTheFly) },
    { super.buildVisitor(holder, isOnTheFly) },
  )

  abstract fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor

  // TODO: user-defined distiller options
  fun distillerOptions() : DistillerOptions = DistillerOptions.pretty()

  class Provider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<out AyaInspection>> = arrayOf(
      // warnings
      DominationInspection::class.java,
      NamingInspection::class.java,
      BadModifierInspection::class.java,
      BadCounterexampleInspection::class.java,
      WarningInspection::class.java,
      // errors
      ErrorInspection::class.java,
      // goals
      GoalInspection::class.java,
      // info
      InfoInspection::class.java,
    )
  }
}
