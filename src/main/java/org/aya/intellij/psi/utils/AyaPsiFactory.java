package org.aya.intellij.psi.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.aya.intellij.psi.concrete.AyaPsiFnDecl;
import org.aya.intellij.psi.concrete.AyaPsiImportCmd;
import org.aya.intellij.psi.concrete.AyaPsiQualifiedId;
import org.aya.intellij.psi.concrete.AyaPsiWeakId;
import org.aya.parser.AyaLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AyaPsiFactory {
  private static @NotNull PsiFile file(@NotNull Project project, @NotNull String text) {
    return PsiFileFactory.getInstance(project).createFileFromText("a.aya", AyaLanguage.INSTANCE, text, false, false);
  }

  public static @NotNull PsiElement leaf(@NotNull Project project, @NotNull String text) {
    return PsiTreeUtil.getDeepestFirst(file(project, text));
  }

  public static @NotNull PsiElement expr(@NotNull Project project, @NotNull String text) {
    var fn = (AyaPsiFnDecl) file(project, "def a : " + text).getFirstChild();
    return Objects.requireNonNull(Objects.requireNonNull(fn.getType()).getExpr(), text);
  }

  public static @NotNull AyaPsiQualifiedId qualifiedId(@NotNull Project project, @NotNull String text) {
    var file = file(project, "import " + text);
    return ((AyaPsiImportCmd) file.getFirstChild()).getQualifiedId();
  }

  public static @NotNull AyaPsiWeakId weakId(@NotNull Project project, @NotNull String text) {
    return qualifiedId(project, text).getWeakIdList().get(0);
  }
}
