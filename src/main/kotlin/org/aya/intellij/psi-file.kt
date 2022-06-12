package org.aya.intellij

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.antlr.intellij.adaptor.psi.ScopeNode
import javax.swing.*

class AyaPSIFileRoot(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, AyaLanguage), ScopeNode {
  override fun getFileType(): FileType = AyaFileType
  override fun toString(): String = fileType.description
  override fun getIcon(flags: Int): Icon = fileType.icon
  override fun getContext(): ScopeNode? = null
  override fun resolve(element: PsiNamedElement): PsiElement? {
    println("${this.javaClass.simpleName}.resolve(${element.name} at ${Integer.toHexString(element.hashCode())})")
    return null
  }
}
