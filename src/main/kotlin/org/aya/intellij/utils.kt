package org.aya.intellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

fun AyaDecl.findType(colon: IElementType): PsiElement? =
  node.findChildByType(colon)?.psi?.nextSiblingIgnoring(TokenType.WHITE_SPACE)

fun AyaDecl.tele() : Sequence<AyaTele> = this.childrenWithLeaves.filter { it is AyaTele }.map { it as AyaTele }

inline fun <reified Psi : PsiElement> PsiElement.nextSiblingIgnoring(vararg types: IElementType): Psi? {
  var next: PsiElement = nextSibling ?: return null
  while (true) {
    next = next.nextSibling ?: return null
    return if (types.any { next.node.elementType == it }) continue
    else next as? Psi
  }
}

inline fun <reified Psi : PsiElement> PsiElement.prevSiblingIgnoring(vararg types: IElementType): Psi? {
  var next: PsiElement = prevSibling ?: return null
  while (true) {
    next = next.prevSibling ?: return null
    return if (types.any { next.node.elementType == it }) continue
    else next as? Psi
  }
}

val PsiElement.leftLeaves: Sequence<PsiElement>
  get() = generateSequence(this, PsiTreeUtil::prevLeaf).drop(1)

val PsiElement.rightSiblings: Sequence<PsiElement>
  get() = generateSequence(this.nextSibling) { it.nextSibling }

val PsiElement.leftSiblings: Sequence<PsiElement>
  get() = generateSequence(this.prevSibling) { it.prevSibling }

val PsiElement.childrenWithLeaves: Sequence<PsiElement>
  get() = generateSequence(this.firstChild) { it.nextSibling }

val PsiElement.childrenRevWithLeaves: Sequence<PsiElement>
  get() = generateSequence(this.lastChild) { it.prevSibling }

val PsiElement.ancestors: Sequence<PsiElement>
  get() = generateSequence(this) {
    if (it is PsiFile) null else it.parent
  }
