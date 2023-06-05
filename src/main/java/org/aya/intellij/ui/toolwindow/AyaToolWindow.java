package org.aya.intellij.ui.toolwindow;

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.aya.intellij.AyaConstants;
import org.aya.intellij.externalSystem.settings.AyaSettings;
import org.aya.intellij.service.ProblemService;
import org.aya.intellij.ui.AyaIcons;
import org.jetbrains.annotations.NotNull;

public class AyaToolWindow extends AbstractExternalSystemToolWindowFactory {
  public static final String TOOLBAR_PLACE = "AyaToolWindow.toolbar";

  public AyaToolWindow() {
    super(AyaConstants.SYSTEM_ID);
  }

  @Override
  protected @NotNull AbstractExternalSystemSettings<?, ?, ?> getSettings(@NotNull Project project) {
    return AyaSettings.getInstance(project);
  }

  @Override public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setIcon(AyaIcons.TOOL_WINDOW);
    var service = project.getService(ProblemService.class);
    service.initToolWindow(project, toolWindow);

    super.createToolWindowContent(project, toolWindow);
  }
}
