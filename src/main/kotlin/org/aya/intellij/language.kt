package org.aya.intellij

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import org.aya.generic.Constants

object AyaLanguage : Language("Aya") {
  override fun getDisplayName() = "Aya Prover"
  override fun isCaseSensitive() = true
  override fun getAssociatedFileType() = AyaFileType
}

object AyaFileType : LanguageFileType(AyaLanguage) {
  override fun getName() = "Aya File"
  override fun getDescription() = "Aya Prover source file"
  override fun getDefaultExtension() = Constants.AYA_POSTFIX.drop(1)
  override fun getIcon() = AyaIcons.AYA_FILE
}
