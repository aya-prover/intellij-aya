package org.aya.intellij.service;

import com.intellij.openapi.observable.properties.AtomicLazyProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import kala.collection.immutable.ImmutableMap;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.ui.GoalsView;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ProblemService {
  public final @NotNull AtomicLazyProperty<ImmutableMap<Path, ImmutableSeq<Problem>>> allProblems =
    new AtomicLazyProperty<>(ImmutableMap::empty);
  private @Nullable GoalsView view;

  public void initToolWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    this.view = new GoalsView(project, toolWindow);
    view.updateView(allProblems.get());
  }
}
