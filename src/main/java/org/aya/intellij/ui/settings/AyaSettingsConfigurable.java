package org.aya.intellij.ui.settings;

import com.intellij.openapi.options.Configurable;
import org.aya.intellij.service.AyaSettingService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AyaSettingsConfigurable implements Configurable {
  private @Nullable AyaSettingsUI ui;

  @Override @Nls(capitalization = Nls.Capitalization.Title)
  public @NotNull String getDisplayName() {
    return "Aya Prover";
  }

  @Override public @Nullable JComponent createComponent() {
    ui = new AyaSettingsUI();
    return ui.root;
  }

  @Override public boolean isModified() {
    if (ui == null) return false;
    var state = AyaSettingService.getInstance();
    return state.ayaLspState != ui.comboBoxUseAyaLsp.getSelectedItem();
  }

  @Override public void apply() {
    if (ui == null) return;
    var state = AyaSettingService.getInstance();
    state.ayaLspState = (AyaSettingService.AyaState) ui.comboBoxUseAyaLsp.getSelectedItem();
  }

  @Override public void reset() {
    if (ui == null) return;
    var state = AyaSettingService.getInstance();
    ui.comboBoxUseAyaLsp.setSelectedItem(state.ayaLspState);
  }

  @Override public void disposeUIResources() {
    Configurable.super.disposeUIResources();
    ui = null;
  }
}
