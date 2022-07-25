package org.aya.intellij.actions;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.aya.intellij.psi.AyaPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefactoringSupport extends RefactoringSupportProvider {
  @Override public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {
    return element instanceof AyaPsiElement;
  }
}
