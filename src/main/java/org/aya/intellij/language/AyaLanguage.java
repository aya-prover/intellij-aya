package org.aya.intellij.language;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public class AyaLanguage extends Language {
  public static final @NotNull AyaLanguage INSTANCE = new AyaLanguage();
  protected AyaLanguage() {
    super("Aya");
  }
}
