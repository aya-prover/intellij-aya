package org.aya.intellij.ui.toolwindow;

import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManager;
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManagerImpl;
import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.view.ExternalProjectsViewImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;
import org.aya.intellij.AyaConstants;
import org.aya.intellij.externalSystem.settings.AyaSettings;
import org.aya.intellij.service.AyaSettingService;
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

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    var ayaSetting = AyaSettingService.getInstance();
    return ayaSetting.lspEnable() || super.shouldBeAvailable(project);
  }

  @Override public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setIcon(AyaIcons.TOOL_WINDOW);
    toolWindow.setTitle(AyaConstants.AYA_PROVER_NAME);

    ExternalProjectsManager.getInstance(project).runWhenInitialized(() -> {
      initExternalSystemToolWindow(project, toolWindow);
      initAyaToolWindow(project, toolWindow);
    });
  }

  private void initExternalSystemToolWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ContentManager manager = toolWindow.getContentManager();
    ExternalProjectsViewImpl projectView = new ExternalProjectsViewImpl(project, (ToolWindowEx)toolWindow, AyaConstants.SYSTEM_ID);
    ExternalProjectsManagerImpl.getInstance(project).registerView(projectView);
    ContentImpl taskContent = new ContentImpl(projectView, "Project", true);
    manager.addContent(taskContent);
  }

  private void initAyaToolWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    var service = project.getService(ProblemService.class);
    service.initToolWindow(project, toolWindow);
  }
}
