package org.aya.intellij.actions

import com.intellij.lang.CodeDocumentationAwareCommenterEx
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.aya.intellij.language.AyaParserDefinition

class Commenter : CodeDocumentationAwareCommenterEx {
  override fun getLineCommentPrefix() = "//"
  override fun getBlockCommentPrefix() = "/*"
  override fun getBlockCommentSuffix() = "*/"

  override fun getDocumentationCommentPrefix() = null
  override fun getDocumentationCommentLinePrefix() = null
  override fun getDocumentationCommentSuffix() = null

  override fun getLineCommentTokenType() = AyaParserDefinition.LINE_COMMENT
  override fun getBlockCommentTokenType() = AyaParserDefinition.BLOCK_COMMENT
  override fun getDocumentationCommentTokenType() = null
  override fun isDocumentationComment(element: PsiComment) = false
  override fun isDocumentationCommentText(element: PsiElement) = false

  override fun getCommentedBlockCommentPrefix() = null
  override fun getCommentedBlockCommentSuffix() = null
}
