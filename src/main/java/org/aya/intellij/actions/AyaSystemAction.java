package org.aya.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.action.ExternalSystemAction;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import org.aya.intellij.AyaConstants;
import org.jetbrains.annotations.NotNull;

public abstract class AyaSystemAction extends ExternalSystemAction {
  @Override
  protected ProjectSystemId getSystemId(@NotNull AnActionEvent e) {
    return AyaConstants.SYSTEM_ID;
  }

  @Override
  protected boolean isVisible(@NotNull AnActionEvent e) {
    return super.isVisible(e);
  }

  @Override
  protected boolean isEnabled(@NotNull AnActionEvent e) {
    return super.isEnabled(e);
  }
}
