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
import org.aya.util.error.WithPos;
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
      case AyaPsiTeleParamName $ -> "Telescopic Param";
      // The PSI parser does not distinguish between a bind pattern and a constructor pattern.
      // But the following match case does not cause constructor patterns to be described as "Pattern Binding"
      // because this method is always called with the resolved results returned by `AyaPsiReference#resolve()`
      // which calls the Aya compiler who knows the truth. So constructor patterns will be described
      // by former cases like `AyaPsiFnDecl`, `AyaPsiDataDecl`, etc.
      case AyaPsiAtomBindPattern $ -> "Pattern Binding";
      case AyaPsiDoBinding $ -> "do-notation Binding";
      default -> "";
    };
  }

  @Override
  public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
    if (!(element instanceof AyaPsiNamedElement named)) return "";
    return AyaLsp.use(named.getProject(), lsp -> {
      var resolvedVar = lsp.resolveVarDefinedBy(named).firstOption();
      return switch (resolvedVar.map(WithPos::data).getOrNull()) {
        case DefVar<?, ?> defVar && defVar.module != null -> QualifiedID.join(defVar.module.appended(defVar.name()));
        case LocalVar localVar -> localVar.name();
        case ModuleVar moduleVar -> moduleVar.name();
        case default, null -> named.nameOrEmpty();
      };
    }, named::nameOrEmpty);
  }

  @Override
  public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return element instanceof AyaPsiNamedElement named ? named.nameOrEmpty() : "";
  }
}
