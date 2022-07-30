package org.aya.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface AyaIcons {
  @NotNull Icon AYA_FILE = IconLoader.getIcon("/icons/ayaFile.svg", AyaIcons.class);
  @NotNull Icon GOAL = IconLoader.getIcon("/icons/goal.svg", AyaIcons.class);
  @NotNull Icon GOAL_SOLVED = IconLoader.getIcon("/icons/goalSolved.svg", AyaIcons.class);
  @NotNull Icon GOAL_CONTEXT_NOT_IN_SCOPE = IconLoader.getIcon("/icons/goalContextNotInScope.svg", AyaIcons.class);
  @NotNull Icon GOAL_CONTEXT = IconLoader.getIcon("/icons/goalContext.svg", AyaIcons.class);
  @NotNull Icon GUTTER_RUN = AllIcons.RunConfigurations.TestState.Run;
  @NotNull Icon TOOL_WINDOW = AllIcons.Toolwindows.ToolWindowMessages;
}
