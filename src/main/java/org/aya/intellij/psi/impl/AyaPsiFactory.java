package org.aya.intellij.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.aya.intellij.AyaLanguage;
import org.jetbrains.annotations.NotNull;

public class AyaPsiFactory {
  private static @NotNull PsiFile createFile(@NotNull Project project, @NotNull String text) {
    return PsiFileFactory.getInstance(project).createFileFromText("a.aya", AyaLanguage.INSTANCE, text, false, false);
  }

  public static @NotNull PsiElement createLeafFromText(@NotNull Project project, @NotNull String text) {
    return PsiTreeUtil.getDeepestFirst(createFile(project, text));
  }
}
