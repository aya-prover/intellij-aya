package org.aya.intellij.inspection.warning

import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.AyaPsiElement
import org.aya.intellij.psi.AyaPsiNamedElement
import org.aya.intellij.psi.concrete.AyaPsiVisitor
import org.aya.resolve.error.NameProblem
import org.aya.util.reporter.Problem

class NamingInspection : WarningInspection() {
  companion object {
    init {
      JOBS.passMe(NameProblem.ShadowingWarn::class.java)
      JOBS.passMe(NameProblem.ModShadowingWarn::class.java)
      JOBS.passMe(NameProblem.AmbiguousNameWarn::class.java)
    }
  }

  override fun getDisplayName() = AyaBundle.message("aya.insp.shadow")
  override fun buildVisitor(lsp: AyaLsp, holder: ProblemsHolder, isOnTheFly: Boolean) = object : AyaPsiVisitor() {
    override fun visitElement(element: AyaPsiElement) {
      if (element !is AyaPsiNamedElement) return
      val nameId = element.nameIdentifier ?: return
      lsp.warningsAt(element, Problem::class.java).forEach {
        when (it) {
          // TODO: 1. make module name a named element
          //       2. make LSP resolves module/import/open commands
          is NameProblem.ShadowingWarn, is NameProblem.ModShadowingWarn, is NameProblem.AmbiguousNameWarn -> holder.registerProblem(
            holder.manager.createProblemDescriptor(
              nameId, nameId,
              AyaBundle.message("aya.insp.shadow"),
              ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly,
              RenameElementFix(element, nameId.text + "'"),
            ),
          )

          else -> {}
        }
      }
    }
  }
}

