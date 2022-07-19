package org.aya.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface AyaIcons {
  @NotNull Icon AYA_FILE = IconLoader.getIcon("/icons/ayaFile.svg", AyaIcons.class);
  @NotNull Icon GUTTER_RUN = AllIcons.RunConfigurations.TestState.Run;
}
