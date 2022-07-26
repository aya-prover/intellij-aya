package org.aya.intellij.psi.types;

import com.intellij.psi.tree.IElementType;
import org.aya.intellij.AyaLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class AyaPsiTokenType extends IElementType {
  public AyaPsiTokenType(@NonNls @NotNull String debugName) {
    super(debugName, AyaLanguage.INSTANCE);
  }
}
