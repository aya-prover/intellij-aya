package org.aya.intellij

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.IElementType
import org.antlr.intellij.adaptor.SymtabUtils
import org.antlr.intellij.adaptor.psi.IdentifierDefSubtree
import org.antlr.intellij.adaptor.psi.ScopeNode

class AyaDecl(node: ASTNode, idElementType: IElementType, private val ruleName: String) : IdentifierDefSubtree(node, idElementType), ScopeNode {
  override fun resolve(element: PsiNamedElement): PsiElement? {
    return SymtabUtils.resolve(
      this, AyaLanguage,
      element, "/program/stmt/decl/${ruleName}",
    )
  }
}
