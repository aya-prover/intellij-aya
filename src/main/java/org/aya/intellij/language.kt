package org.aya.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import org.aya.generic.Constants
import org.aya.parser.AyaLanguage

object AyaFileType : LanguageFileType(AyaLanguage.INSTANCE) {
  override fun getName() = "Aya File"
  override fun getDescription() = "Aya Prover source file"
  override fun getDefaultExtension() = Constants.AYA_POSTFIX.drop(1)
  override fun getIcon() = AyaIcons.AYA_FILE
}
