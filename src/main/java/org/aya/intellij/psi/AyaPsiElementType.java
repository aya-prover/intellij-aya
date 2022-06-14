package org.aya.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.aya.intellij.AyaLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class AyaPsiElementType extends IElementType {
  public AyaPsiElementType(@NonNls @NotNull String debugName) {
    super(debugName, AyaLanguage.INSTANCE);
  }
}
