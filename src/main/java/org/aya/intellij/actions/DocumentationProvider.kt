package org.aya.intellij.actions

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
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
      val builder = StringBuilder()

      // For saturationFactor, see DocumentationSettings.getHighlightingSaturation
      // and [this](https://github.com/JetBrains/intellij-community/blob/28b957346d4353f3970d8288e081bf59e32d4fc5/java/java-impl/src/com/intellij/lang/java/JavaDocumentationProvider.java#L90)
      HtmlSyntaxInfoUtil.appendStyledSpan(builder, SyntaxHighlight.ID, name, 1.0F)
      builder.append(' ')
      HtmlSyntaxInfoUtil.appendStyledSpan(builder, SyntaxHighlight.KEYWORD, ":", 1.0F)
      builder.append(' ')
      builder.append(type)

      return builder.toString()
    }
  }
}
