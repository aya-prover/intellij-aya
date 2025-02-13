package org.aya.intellij.ui.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.ContentFactory;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableMap;
import kala.collection.immutable.ImmutableSeq;
import kala.collection.mutable.MutableList;
import kala.control.Option;
import kala.tuple.Tuple;
import kala.tuple.Tuple3;
import kotlin.Unit;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.actions.lsp.AyaLsp;
import org.aya.intellij.actions.lsp.JB;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.concrete.AyaPsiHoleExpr;
import org.aya.intellij.service.ProblemService;
import org.aya.intellij.ui.AyaIcons;
import org.aya.intellij.ui.AyaTreeView;
import org.aya.normalize.Finalizer;
import org.aya.prettier.AyaPrettierOptions;
import org.aya.pretty.doc.Doc;
import org.aya.syntax.core.term.ErrorTerm;
import org.aya.syntax.core.term.Term;
import org.aya.syntax.ref.LocalVar;
import org.aya.syntax.ref.MetaVar;
import org.aya.tyck.error.Goal;
import org.aya.util.PrettierOptions;
import org.aya.util.position.SourceFile;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.nio.file.Path;

public class GoalsView implements AyaTreeView.NodeAdapter<GoalsView.GoalNode> {
  private final @NotNull AyaTreeView<GoalNode> treeView;
  // TODO: user-defined distiller options
  private final @NotNull PrettierOptions options = AyaPrettierOptions.debug();
  private final @NotNull Project project;

  public GoalsView(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    this.project = project;
    var rootPanel = new SimpleToolWindowPanel(false);
    treeView = AyaTreeView.create(project, "All Goals", false);
    treeView.setAdapter(this);
    treeView.attach(rootPanel);

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
    ApplicationManager.getApplication().runReadAction(() -> {
      var editor = FileEditorManager.getInstance(project).getSelectedEditor();
      if (editor != null) treeView.scrollFromEditor(editor);
    });
  }

  private void updateView(
    @NotNull AyaTreeView.NodeBuilder<GoalNode> builder,
    @NotNull ImmutableMap<Path, ImmutableSeq<Problem>> problems
  ) {
    problems.forEach((p, ps) -> {
      var goals = ps.filterIsInstance(Goal.class);
      if (goals.isEmpty()) return;
      builder.shift(new FileG(ps.getFirst().sourcePos().file()));
      goals.forEach(g -> updateView(builder, g));
      builder.reduce();
    });
    builder.commit();
  }

  private void updateView(@NotNull AyaTreeView.NodeBuilder<GoalNode> builder, @NotNull Goal goal) {
    var g = new GoalG(goal);
    builder.shift(g);
    goal.hole().args()
      .zip(goal.scope(), (param, var) -> new TeleG(goal, var, param))
      .forEach(builder::append);
    builder.reduce();
  }

  @Override public @NotNull String renderTitle(@NotNull GoalNode node) {
    return switch (node) {
      case FileG f -> f.sourceFile.display();
      case GoalG g -> g.describe(options).debugRender();
      case TeleG c -> c.describe(options).debugRender();
    };
  }

  @Override public @Nullable Icon renderIcon(@NotNull GoalNode node) {
    return switch (node) {
      case FileG _ -> AyaIcons.AYA_FILE;
      case GoalG g -> solved(g.goal) ? AyaIcons.GOAL_SOLVED : AyaIcons.GOAL;
      case TeleG c -> c.inScope() ? AyaIcons.GOAL_CONTEXT : AyaIcons.GOAL_CONTEXT_NOT_IN_SCOPE;
    };
  }

  @Override public @Nullable PsiElement findElement(@NotNull GoalNode node) {
    return switch (node) {
      case FileG _ -> null;
      case GoalG g -> JB.elementAt(project, g.goal.sourcePos(), AyaPsiHoleExpr.class);
      case TeleG c -> JB.elementAt(project, c.goal.sourcePos(), AyaPsiHoleExpr.class);
    };
  }

  @Override public @Nullable Tuple3<GoalNode, TreePath, Boolean> findNode(@NotNull AyaPsiFile file, int offset) {
    var goals = AyaLsp.use(project, SeqView::<Goal>empty, lsp -> lsp.goalsAt(file, offset));
    var selected = goals.filter(this::solved).getFirstOption().getOrElse(goals::getFirstOrNull);
    return Option.ofNullable(selected)
      .mapNotNull(goal -> treeView.find(n -> switch (n) {
        case FileG _ -> false;
        case TeleG c -> goal == c.goal;
        case GoalG g -> goal == g.goal;
      }))
      .map(found -> Tuple.of(found.component1(), found.component2(), found.component1() instanceof TeleG))
      .getOrNull();
  }

  public boolean solved(@NotNull Goal goal) {
    var state = goal.state();
    var meta = goal.hole().ref();
    return state.solutions.containsKey(meta);
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

    /** Rewrite of {@link Goal#describe(PrettierOptions)} */
    public @NotNull Doc describe(@NotNull PrettierOptions options) {
      var meta = goal.hole().ref();
      var state = goal.state();
      var result = switch (meta.req()) {
        case MetaVar.OfType (var type) -> new Finalizer.Freeze(() -> state).zonk(type);
        case MetaVar.Misc _, MetaVar.PiDom _ -> new ErrorTerm(Doc.plain("???"), false);
      };
      var resultDoc = result.toDoc(options);
      var name = Doc.plain(meta.name());
      return Doc.cat(name, Doc.spaced(Doc.plain(":")), resultDoc);
    }
  }

  record TeleG(
    @NotNull Goal goal,
    @NotNull Term arg,
    @NotNull LocalVar var,
    @NotNull MutableList<GoalNode> children
  ) implements GoalNode {
    public TeleG(@NotNull Goal goal, @NotNull LocalVar var, @NotNull Term arg) {
      this(goal, arg, var, MutableList.create());
    }

    /** Rewrite of {@link Goal#describe(PrettierOptions)} */
    public @NotNull Doc describe(@NotNull PrettierOptions options) {
      var paramDoc = arg.toDoc(options);
      return inScope() ? paramDoc : Doc.sep(paramDoc, Doc.parened(
        Doc.english(AyaBundle.INSTANCE.message("aya.ui.goals.not.in.scope"))));
    }

    public boolean inScope() {
      return goal.scope().contains(var);
    }
  }
}
