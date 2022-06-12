package org.aya.intellij

import com.intellij.lang.ASTNode
import com.intellij.lang.DefaultASTFactoryImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.tree.IElementType
import org.antlr.intellij.adaptor.SymtabUtils
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import org.antlr.intellij.adaptor.psi.ANTLRPsiLeafNode
import org.antlr.intellij.adaptor.psi.IdentifierDefSubtree
import org.antlr.intellij.adaptor.psi.ScopeNode
import org.antlr.intellij.adaptor.psi.Trees
import org.aya.parser.AyaLexer
import org.jetbrains.annotations.NonNls

class AyaPsiFactory : DefaultASTFactoryImpl() {
  override fun createLeaf(type: IElementType, text: CharSequence): LeafElement {
    return if (type is TokenIElementType &&
      type.antlrTokenType == AyaLexer.ID) {
      AyaId(type, text)
    } else super.createLeaf(type, text)
  }
}

class AyaDecl(node: ASTNode, idElementType: IElementType, private val ruleName: String) : IdentifierDefSubtree(node, idElementType), ScopeNode {
  override fun resolve(element: PsiNamedElement): PsiElement? {
    println("${this.javaClass.simpleName}.resolve(${element.name} at ${Integer.toHexString(element.hashCode())})")
    return SymtabUtils.resolve(
      this, AyaLanguage,
      element, "/program/stmt/decl/${ruleName}",
    )
  }
}

class AyaTele(node: ASTNode, idElementType: IElementType) : IdentifierDefSubtree(node, idElementType) {
  fun extractId(): AyaId {
    TODO("Not yet implemented")
  }
}

class AyaId(type: IElementType?, text: CharSequence?) : ANTLRPsiLeafNode(type, text), PsiNamedElement {
  override fun getName() = text

  override fun setName(@NonNls name: String): PsiElement {
    if (parent == null) return this
    val newID = Trees.createLeafFromText(
      project,
      AyaLanguage,
      context,
      name,
      AyaParserDefinition.ID,
    )
    return if (newID != null) this.replace(newID) else this
  }

  override fun getReference(): PsiReference? {
    val context = this.context ?: return null
    if (context !is AyaDecl) return null
    val tele = context.tele()
    val found = tele.find { resolveTele(it, this) }
    if (found != null) return AyaTeleRef(found.extractId())
    return resolveGlobal(context, this)
  }

  private fun resolveGlobal(self: AyaDecl, ayaId: AyaId): PsiReference? {
    TODO("resolve global definition")
  }

  private fun resolveTele(tele : AyaTele, id: AyaId): Boolean {
    TODO("check if the name of the tele is id")
  }
}

abstract class AyaRef(element: AyaId) : PsiReferenceBase<AyaId>(element, TextRange(0, element.text.length)) {
  override fun handleElementRename(newElementName: String): PsiElement {
    return myElement.setName(newElementName)
  }

  override fun resolve(): PsiElement? {
    val scope = myElement.context as ScopeNode? ?: return null
    return scope.resolve(myElement)
  }

  override fun isReferenceTo(def: PsiElement): Boolean {
    var ayaDecl = def
    if (ayaDecl is AyaId && isAyaDecl(ayaDecl.parent)) ayaDecl = ayaDecl.parent
    if (isAyaDecl(ayaDecl)) {
      val defName = (ayaDecl as PsiNameIdentifierOwner).nameIdentifier?.text
      return myElement.name == defName
    }
    return false
  }

  abstract fun isAyaDecl(def: PsiElement?): Boolean
}

class AyaTeleRef(element: AyaId) : AyaRef(element) {
  override fun isAyaDecl(def: PsiElement?) = def is AyaTele
}

class AyaDeclRef(element: AyaId) : AyaRef(element) {
  override fun isAyaDecl(def: PsiElement?) = def is AyaDecl
}
