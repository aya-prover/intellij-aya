package org.aya.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.aya.intellij.AyaFileType;
import org.aya.intellij.AyaLanguage;
import org.jetbrains.annotations.NotNull;

public class AyaPsiFile extends PsiFileBase implements AyaPsiElement {
  public AyaPsiFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, AyaLanguage.INSTANCE);
  }

  @Override public @NotNull FileType getFileType() {
    return AyaFileType.INSTANCE;
  }
}
