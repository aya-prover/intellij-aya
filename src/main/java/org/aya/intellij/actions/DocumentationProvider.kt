package org.aya.intellij.actions

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.psi.AyaPsiElement
import org.aya.intellij.psi.AyaPsiNamedElement

class DocumentationProvider : AbstractDocumentationProvider() {
  /**
   * Show type of [originalElement], because the element might not a RefExpr
   */
  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
    return if (originalElement is AyaPsiElement) {
        val name = originalElement.text ?: return null
        generateTypeWithName(originalElement, name)
    } else null
  }

  companion object {
    /**
     * Generate type of the [element]
     *
     * @return null if failed
     */
    fun generateType(element: AyaPsiElement) : String? {
      val project = element.project

      return AyaLsp.use<String?, Throwable>(project, { null }) { lsp: AyaLsp ->
        when (element) {
          is AyaPsiNamedElement -> null   // TODO[hoshino]: no need for definition?
          else -> lsp.showRefType(element)
        }
      }
    }

    /**
     * generate type of the [element] with name, like: `foo : Nat`
     *
     * @return null if failed
     * @see DocumentationProvider.generateType
     */
    fun generateTypeWithName(element: AyaPsiElement, name: String) : String? {
      val type = generateType(element) ?: return null

      // TODO: use Doc
      return "$name : $type"
    }
  }
}
