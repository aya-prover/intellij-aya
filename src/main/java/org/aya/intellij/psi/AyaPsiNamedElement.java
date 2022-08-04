package org.aya.intellij.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.lsp.utils.ModuleVar;
import org.aya.ref.DefVar;
import org.aya.ref.LocalVar;
import org.aya.util.error.WithPos;
import org.jetbrains.annotations.NotNull;

/** elements that introduce a referable name */
public interface AyaPsiNamedElement extends AyaPsiStructureElement, PsiNameIdentifierOwner {
  default @NotNull String nameOrEmpty() {
    var name = getName();
    return name != null ? name : "";
  }

  /** @return qualified name for definitions and modules if possible, parameter name for local variables. */
  default @NotNull String canonicalName() {
    return AyaLsp.use(getProject(), this::nameOrEmpty, lsp -> {
      var resolvedVar = lsp.resolveVarDefinedBy(this).firstOption();
      return switch (resolvedVar.map(WithPos::data).getOrNull()) {
        case DefVar<?, ?> defVar && defVar.module != null -> QualifiedID.join(defVar.module.appended(defVar.name()));
        case LocalVar localVar -> localVar.name();
        case ModuleVar moduleVar -> moduleVar.name();
        case default, null -> nameOrEmpty();
      };
    });
  }
}
