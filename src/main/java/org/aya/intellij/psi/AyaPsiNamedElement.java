package org.aya.intellij.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.aya.ide.util.ModuleVar;
import org.aya.intellij.actions.lsp.AyaLsp;
import org.aya.syntax.concrete.stmt.QualifiedID;
import org.aya.syntax.ref.DefVar;
import org.aya.syntax.ref.LocalVar;
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
      var resolvedVar = lsp.resolveVarDefinedBy(this).getFirstOption();
      return switch (resolvedVar.map(WithPos::data).getOrNull()) {
        case DefVar<?, ?> defVar when defVar.module != null ->
          QualifiedID.join(defVar.module.module().module().appended(defVar.name()));
        case LocalVar localVar -> localVar.name();
        case ModuleVar moduleVar -> moduleVar.name();
        case null, default -> nameOrEmpty();
      };
    });
  }
}
