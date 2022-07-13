package org.aya.intellij.settings;

import com.intellij.openapi.options.Configurable;
import org.aya.intellij.ui.AyaSettingsUI;
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
    var state = AyaSettingsState.getInstance();
    return state.useAyaLsp != ui.useAyaLsp.isSelected();
  }

  @Override public void apply() {
    if (ui == null) return;
    var state = AyaSettingsState.getInstance();
    state.useAyaLsp = ui.useAyaLsp.isSelected();
  }

  @Override public void reset() {
    if (ui == null) return;
    var state = AyaSettingsState.getInstance();
    ui.useAyaLsp.setSelected(state.useAyaLsp);
  }

  @Override public void disposeUIResources() {
    Configurable.super.disposeUIResources();
    ui = null;
  }
}
