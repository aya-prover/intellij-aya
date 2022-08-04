package org.aya.intellij.run;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TyckRunConfigEditorUI extends SettingsEditor<TyckRunConfig> {
  private @NotNull JPanel root;
  private @NotNull LabeledComponent<TextFieldWithBrowseButton> moduleName;
  private @NotNull LabeledComponent<TextFieldWithBrowseButton> definitionName;

  @Override protected void resetEditorFrom(@NotNull TyckRunConfig config) {
    moduleName.getComponent().setText(config.moduleName());
    definitionName.getComponent().setText(config.definitionName());
  }

  @Override protected void applyEditorTo(@NotNull TyckRunConfig config) {
    config.moduleName(moduleName.getComponent().getText());
    config.definitionName(definitionName.getComponent().getText());
  }

  @Override protected @NotNull JComponent createEditor() {
    return root;
  }

  private void createUIComponents() {
    moduleName = new LabeledComponent<>();
    moduleName.setComponent(new TextFieldWithBrowseButton());
    definitionName = new LabeledComponent<>();
    definitionName.setComponent(new TextFieldWithBrowseButton());
  }
}
