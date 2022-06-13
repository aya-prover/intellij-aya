package org.aya.intellij.language;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface AyaIcons {
  @NotNull Icon AYA_FILE = IconLoader.getIcon("/icons/ayaFile.svg", AyaIcons.class);
}
