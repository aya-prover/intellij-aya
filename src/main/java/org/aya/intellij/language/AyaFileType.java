package org.aya.intellij.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class AyaFileType extends LanguageFileType {
  public static final @NotNull AyaFileType INSTANCE = new AyaFileType();

  private AyaFileType() {
    super(AyaLanguage.INSTANCE);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return "Aya File";
  }

  @Override
  public @NlsContexts.Label @NotNull String getDescription() {
    return "Aya Prover source file";
  }

  @Override
  public @NlsSafe @NotNull String getDefaultExtension() {
    return "aya";
  }

  @Override
  public Icon getIcon() {
    return AyaIcons.AYA_FILE;
  }
}
