package org.aya.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.antlr.intellij.adaptor.psi.ScopeNode;
import org.aya.intellij.language.AyaFileType;
import org.aya.intellij.language.AyaLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiFileRoot extends PsiFileBase implements ScopeNode {
  public AyaPsiFileRoot(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, AyaLanguage.INSTANCE);
  }

  @Override public @NotNull FileType getFileType() {
    return AyaFileType.INSTANCE;
  }

  @Override public @Nullable PsiElement resolve(PsiNamedElement element) {
    return null;
  }

  @Override public ScopeNode getContext() {
    return null;
  }
}
