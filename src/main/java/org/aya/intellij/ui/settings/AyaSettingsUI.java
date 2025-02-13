package org.aya.intellij.ui.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.FormBuilder;
import org.aya.intellij.service.AyaSettingService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AyaSettingsUI {
  public final @NotNull JPanel root;
  public final @NotNull ComboBox<AyaSettingService.AyaState> comboBoxUseAyaLsp = new ComboBox<>(AyaSettingService.AyaState.values());

  public AyaSettingsUI() {
    root = FormBuilder.createFormBuilder()
      .addLabeledComponent("Aya lsp state", comboBoxUseAyaLsp, 1)
      .addComponentFillVertically(new JPanel(), 0)
      .getPanel();
  }
}
