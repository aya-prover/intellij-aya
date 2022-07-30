package org.aya.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.ContentFactory;
import kala.collection.immutable.ImmutableMap;
import kala.collection.immutable.ImmutableSeq;
import kala.collection.mutable.MutableList;
import kotlin.Unit;
import org.aya.core.term.ErrorTerm;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.AyaIcons;
import org.aya.intellij.lsp.ProblemService;
import org.aya.pretty.doc.Doc;
import org.aya.tyck.error.Goal;
import org.aya.util.distill.DistillerOptions;
import org.aya.util.error.SourceFile;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;

public class GoalsView implements AyaTreeView.NodeAdapter<GoalsView.GoalNode> {
  private final @NotNull AyaTreeView<GoalNode> treeView;
  // TODO: user-defined distiller options
  private final @NotNull DistillerOptions options = DistillerOptions.informative();

  public GoalsView(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    var rootPanel = new SimpleToolWindowPanel(false);
    treeView = AyaTreeView.create("All Goals", false);
    treeView.setAdapter(this);
    treeView.attach(project, rootPanel);

    var contentFactory = ContentFactory.getInstance();
    toolWindow.getContentManager().addContent(contentFactory.createContent(
      rootPanel,
      AyaBundle.INSTANCE.message("aya.ui.goals"), true
    ));

    project.getService(ProblemService.class).allProblems.afterChange(project, goals -> {
      updateView(goals);
      return Unit.INSTANCE;
    });
  }

  public void updateView(@NotNull ImmutableMap<Path, ImmutableSeq<Problem>> goals) {
    updateView(treeView.edit(), goals);
  }

  private void updateView(
    @NotNull AyaTreeView.NodeBuilder<GoalNode> builder,
    @NotNull ImmutableMap<Path, ImmutableSeq<Problem>> problems
  ) {
    problems.forEach((p, ps) -> {
      var goals = ps.filterIsInstance(Goal.class);
      if (goals.isEmpty()) return;
      builder.shift(new FileG(ps.first().sourcePos().file()));
      goals.forEach(g -> updateView(builder, g));
      builder.reduce();
    });
    builder.commit();
  }

  private void updateView(@NotNull AyaTreeView.NodeBuilder<GoalNode> builder, @NotNull Goal goal) {
    var g = new GoalG(goal);
    builder.shift(g);
    g.context(options).forEach(builder::append);
    builder.reduce();
  }

  @Override public @NotNull String renderTitle(@NotNull GoalsView.GoalNode node) {
    return switch (node) {
      case FileG f -> f.sourceFile.display();
      case GoalG g -> g.describe(options);
      case GoalContextG l -> l.label;
    };
  }

  @Override public @Nullable Icon renderIcon(@NotNull GoalsView.GoalNode node) {
    return switch (node) {
      case FileG $ -> AyaIcons.AYA_FILE;
      case GoalG g -> solved(g.goal) ? AyaIcons.GOAL_SOLVED : AyaIcons.GOAL;
      case GoalContextG c -> c.inScope ? AyaIcons.GOAL_CONTEXT : AyaIcons.GOAL_CONTEXT_NOT_IN_SCOPE;
    };
  }

  public boolean solved(@NotNull Goal goal) {
    var state = goal.state();
    var meta = goal.hole().ref();
    return state.metas().containsKey(meta);
  }

  public sealed interface GoalNode extends AyaTreeView.Node<GoalNode> {
  }

  record FileG(
    @NotNull SourceFile sourceFile,
    @NotNull MutableList<GoalNode> children
  ) implements GoalNode {
    public FileG(@NotNull SourceFile sourceFile) {
      this(sourceFile, MutableList.create());
    }
  }

  record GoalG(
    @NotNull Goal goal,
    @NotNull MutableList<GoalNode> children
  ) implements GoalNode {
    public GoalG(@NotNull Goal goal) {
      this(goal, MutableList.create());
    }

    /** Rewrite of {@link Goal#describe(DistillerOptions)} */
    public @NotNull String describe(@NotNull DistillerOptions options) {
      var meta = goal.hole().ref();
      var state = goal.state();
      var result = meta.result != null ? meta.result.freezeHoles(state)
        : new ErrorTerm(Doc.plain("???"), false);
      var resultDoc = result.toDoc(options);
      var name = Doc.plain(meta.name);
      return Doc.cat(name, Doc.spaced(Doc.plain(":")), resultDoc).debugRender();
    }

    public @NotNull ImmutableSeq<GoalContextG> context(@NotNull DistillerOptions options) {
      var meta = goal.hole().ref();
      var scope = goal.scope();
      return meta.fullTelescope().map(param -> {
        var paramDoc = param.toDoc(options);
        var inScope = scope.contains(param.ref());
        var doc = inScope ? paramDoc : Doc.sep(paramDoc, Doc.parened(
          Doc.english(AyaBundle.INSTANCE.message("aya.ui.goals.not.in.scope"))));
        return new GoalContextG(doc.debugRender(), inScope);
      }).toImmutableSeq();
    }
  }

  record GoalContextG(
    @NotNull String label,
    boolean inScope,
    @NotNull MutableList<GoalNode> children
  ) implements GoalNode {
    public GoalContextG(@NotNull String label, boolean inScope) {
      this(label, inScope, MutableList.create());
    }
  }
}
