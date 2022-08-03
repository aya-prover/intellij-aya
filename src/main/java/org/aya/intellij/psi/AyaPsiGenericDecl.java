package org.aya.intellij.psi;

import org.aya.intellij.AyaIcons;
import org.aya.intellij.psi.concrete.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Generic concrete definitions in PSI, corresponding to {@link org.aya.concrete.stmt.Decl}.
 *
 * @see org.aya.intellij.psi.impl.AyaPsiGenericDeclImpl
 */
public interface AyaPsiGenericDecl extends AyaPsiNamedElement {
  default @NotNull Icon ayaIcon() {
    return switch (this) {
      case AyaPsiDataDecl $ -> AyaIcons.AYA_DATA;
      case AyaPsiStructDecl $ -> AyaIcons.AYA_STRUCT;
      case AyaPsiPrimDecl $ -> AyaIcons.AYA_PRIM;
      case AyaPsiFnDecl $ -> AyaIcons.AYA_FN;
      case AyaPsiDataBody $ -> AyaIcons.AYA_CTOR;
      case AyaPsiStructField $ -> AyaIcons.AYA_FIELD;
      default -> throw new IllegalStateException("unreachable");
    };
  }
}
