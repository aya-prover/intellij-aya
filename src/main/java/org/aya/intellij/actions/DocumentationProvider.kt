package org.aya.intellij.actions

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.AyaPsiElement
import org.aya.intellij.psi.AyaPsiNamedElement
import org.aya.intellij.psi.AyaPsiNamedWeakId

class DocumentationProvider : AbstractDocumentationProvider() {
  /**
   * Show type for [originalElement]
   */
  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
    return if (element is AyaPsiElement && originalElement is AyaPsiElement) {
        val name = originalElement.text ?: return null
        generateTypeWithName(element, originalElement, name)
    } else null
  }

  companion object {
    /**
     * Generate the type of the [element].
     *
     * * For reference to a LocalVar, we just generate its type: `Nat` or `Nat -> Nat`
     * * For reference to a definition, we generate its signature, like: `{A : Type} (a : A) : Nat`
     *
     * @return null if failed
     */
    fun generateType(resolved: AyaPsiElement, element: AyaPsiElement) : String? {
      val project = element.project

      return AyaLsp.use<String?, Throwable>(project, { null }) { lsp: AyaLsp ->
        when {
          isDefinition(resolved) -> lsp.showDefType(resolved)
          else -> lsp.showRefType(element)
        }
      }
    }

    /**
     * Generate the type of the [element] with name, like: `local : Nat` and `decl {A : Type} (a : A) : A`
     *
     * @return null if failed
     * @see DocumentationProvider.generateType
     */
    fun generateTypeWithName(resolved: AyaPsiElement, element: AyaPsiElement, name: String) : String? {
      val type = generateType(resolved, element) ?: return null
      val builder = StringBuilder()

      // For saturationFactor, see DocumentationSettings.getHighlightingSaturation
      // and [this](https://github.com/JetBrains/intellij-community/blob/28b957346d4353f3970d8288e081bf59e32d4fc5/java/java-impl/src/com/intellij/lang/java/JavaDocumentationProvider.java#L90)
      HtmlSyntaxInfoUtil.appendStyledSpan(builder, SyntaxHighlight.ID, name, 1.0F)

      // a local var, use `x : Nat`
      if (! isDefinition(resolved)) {
        builder.append(' ')
        HtmlSyntaxInfoUtil.appendStyledSpan(builder, SyntaxHighlight.KEYWORD, ":", 1.0F)
      }

      builder.append(' ')
      builder.append(type)

      return builder.toString()
    }

    /**
     * Check whether the [element] is a definition (FnDecl, DataDecl, but not Expr.Param or Pattern.Bind)
     */
    private fun isDefinition(element: AyaPsiElement) : Boolean = element is AyaPsiNamedElement && element !is AyaPsiNamedWeakId
  }
}
