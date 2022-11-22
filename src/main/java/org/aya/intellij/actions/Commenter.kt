package org.aya.intellij.actions

import com.intellij.lang.CodeDocumentationAwareCommenterEx
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.aya.intellij.language.AyaParserDefinition
import org.aya.intellij.psi.concrete.AyaPsiRemark

class Commenter : CodeDocumentationAwareCommenterEx {
  override fun getLineCommentPrefix() = "//"
  override fun getBlockCommentPrefix() = "/*"
  override fun getBlockCommentSuffix() = "*/"

  override fun getDocumentationCommentPrefix() = "///"
  override fun getDocumentationCommentLinePrefix() = "///"
  override fun getDocumentationCommentSuffix() = null

  override fun getLineCommentTokenType() = AyaParserDefinition.LINE_COMMENT
  override fun getBlockCommentTokenType() = AyaParserDefinition.BLOCK_COMMENT
  override fun getDocumentationCommentTokenType() = AyaParserDefinition.DOC_COMMENT
  override fun isDocumentationComment(element: PsiComment) = element is AyaPsiRemark
  override fun isDocumentationCommentText(element: PsiElement) = element is AyaPsiRemark

  override fun getCommentedBlockCommentPrefix() = null
  override fun getCommentedBlockCommentSuffix() = null
}
