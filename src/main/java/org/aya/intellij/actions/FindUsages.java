package org.aya.intellij.actions;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.psi.concrete.*;
import org.aya.lsp.utils.ModuleVar;
import org.aya.ref.DefVar;
import org.aya.ref.LocalVar;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FindUsages implements FindUsagesProvider {
  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psi) {
    return psi instanceof AyaPsiNamedElement;
  }

  @Override
  public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  public @Nls @NotNull String getType(@NotNull PsiElement element) {
    if (!(element instanceof AyaPsiNamedElement named)) return "";
    return switch (named) {
      case AyaPsiFnDecl $ -> "Function";
      case AyaPsiDataDecl $ -> "Data";
      case AyaPsiStructDecl $ -> "Struct";
      case AyaPsiPrimDecl $ -> "Primitive";
      case AyaPsiDataBody $ -> "Data Constructor";
      case AyaPsiField $ -> "Struct Field";
      case AyaPsiTele $ -> "Telescopic Binding";
      case AyaPsiNewArgTele $ -> "Telescopic Binding";
      default -> "";
    };
  }

  @Override
  public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
    if (!(element instanceof AyaPsiNamedElement named)) return "";
    var lsp = AyaLsp.of(named.getProject());
    if (lsp == null) return named.nameOrEmpty();
    var resolvedVar = lsp.resolveVar(named).firstOrNull();
    if (resolvedVar == null) return named.nameOrEmpty();
    return switch (resolvedVar.data()) {
      case DefVar<?, ?> defVar && defVar.module != null -> QualifiedID.join(defVar.module.appended(defVar.name()));
      case LocalVar localVar -> localVar.name();
      case ModuleVar moduleVar -> moduleVar.name();
      default -> named.nameOrEmpty();
    };
  }

  @Override
  public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return element instanceof AyaPsiNamedElement named ? named.nameOrEmpty() : "";
  }
}
