package org.aya.intellij.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.aya.generic.Constants;
import org.aya.intellij.ui.AyaIcons;
import org.aya.parser.AyaLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AyaFileType extends LanguageFileType {
  public static final @NotNull AyaFileType INSTANCE = new AyaFileType();

  public AyaFileType() {
    super(AyaLanguage.INSTANCE);
  }

  @Override public @NonNls @NotNull String getName() {
    return "Aya File";
  }

  @Override public @NlsContexts.Label @NotNull String getDescription() {
    return "Aya Prover source file";
  }

  @Override public @NlsSafe @NotNull String getDefaultExtension() {
    return Constants.AYA_POSTFIX.substring(1);
  }

  @Override public Icon getIcon() {
    return AyaIcons.AYA_FILE;
  }
}

