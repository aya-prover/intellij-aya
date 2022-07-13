package org.aya.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AyaToolWindow implements ToolWindowFactory {
  @Override public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    var contentFactory = ContentFactory.getInstance();
    var content = contentFactory.createContent(new JLabel("Hello AYA"), "Aya Prover", false);
    toolWindow.getContentManager().addContent(content);
  }
}
