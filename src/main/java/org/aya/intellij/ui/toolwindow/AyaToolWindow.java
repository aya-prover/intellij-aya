package org.aya.intellij.ui.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.aya.intellij.service.ProblemService;
import org.aya.intellij.ui.AyaIcons;
import org.jetbrains.annotations.NotNull;

public class AyaToolWindow implements ToolWindowFactory {
  public static final String TOOLBAR_PLACE = "AyaToolWindow.toolbar";

  @Override public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setIcon(AyaIcons.TOOL_WINDOW);
    var service = project.getService(ProblemService.class);
    service.initToolWindow(project, toolWindow);
  }
}
