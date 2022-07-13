package org.aya.intellij.ui;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AyaSettingsUI {
  public final @NotNull JPanel root;
  public final @NotNull JBCheckBox useAyaLsp = new JBCheckBox("Use Aya language server");

  public AyaSettingsUI() {
    root = FormBuilder.createFormBuilder()
      .addComponent(useAyaLsp, 1)
      .addComponentFillVertically(new JPanel(), 0)
      .getPanel();
  }
}
