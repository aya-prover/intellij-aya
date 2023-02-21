package org.aya.intellij.ui;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.AutoScrollFromSourceHandler;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Option;
import kala.tuple.Tuple;
import kala.tuple.Tuple2;
import kala.tuple.Tuple3;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.utils.AyaPsiUtils;
import org.aya.intellij.service.AyaSettingService;
import org.aya.intellij.ui.toolwindow.AyaToolWindow;
import org.aya.util.TreeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** A super-friendly tree view */
public class AyaTreeView<T extends AyaTreeView.Node<T>> extends Tree {
  public interface Node<T extends Node<T>> extends TreeBuilder.Tree<T> {
  }

  public interface NodeAdapter<T extends Node<T>> {
    @Nullable String renderTitle(@NotNull T node);
    @Nullable Icon renderIcon(@NotNull T node);
    @Nullable PsiElement findElement(@NotNull T node);
    @Nullable Tuple3<T, TreePath, Boolean> findNode(@NotNull AyaPsiFile file, int offset);
  }

  public static final class NodeBuilder<T extends Node<T>> extends TreeBuilder<T> {
    private final @NotNull AyaTreeView<T> treeView;

    public NodeBuilder(@NotNull AyaTreeView<T> treeView) {
      this.treeView = treeView;
    }

    public void commit() {
      treeView.updateNode(root().toImmutableSeq());
    }
  }

  public static <T extends Node<T>> @NotNull AyaTreeView<T> create(@NotNull Project project, @NotNull String rootText, boolean showRoot) {
    var rootNode = new DefaultMutableTreeNode(rootText);
    var rootNodeModel = new DefaultTreeModel(rootNode);
    return new AyaTreeView<>(project, rootNode, rootNodeModel, showRoot);
  }

  private final @NotNull DefaultMutableTreeNode rootNode;
  private final @NotNull DefaultTreeModel rootNodeModel;
  private @Nullable NodeAdapter<T> adapter;
  private final @NotNull Project project;

  private AyaTreeView(
    @NotNull Project project,
    @NotNull DefaultMutableTreeNode rootNode,
    @NotNull DefaultTreeModel rootNodeModel,
    boolean showRoot
  ) {
    super(rootNodeModel);
    setRootVisible(showRoot);
    this.rootNode = rootNode;
    this.rootNodeModel = rootNodeModel;
    this.project = project;
  }

  public @NotNull NodeBuilder<T> edit() {
    return new NodeBuilder<>(this);
  }

  public void attach(@NotNull SimpleToolWindowPanel rootPanel) {
    var actionGroup = new DefaultActionGroup();
    var actionManager = CommonActionsManager.getInstance();
    var treeExpander = new DefaultTreeExpander(this);

    actionGroup.add(actionManager.createExpandAllAction(treeExpander, this));
    actionGroup.add(actionManager.createCollapseAllAction(treeExpander, this));
    actionGroup.addSeparator();
    actionGroup.add(new ScrollTo(this).createToggleAction());
    actionGroup.add(new ScrollFrom(project, this).createToggleAction());

    var toolbar = ActionManager.getInstance().createActionToolbar(
      AyaToolWindow.TOOLBAR_PLACE, actionGroup, false);
    toolbar.setTargetComponent(rootPanel);
    rootPanel.setToolbar(toolbar.getComponent());
    rootPanel.setContent(ScrollPaneFactory.createScrollPane(this, true));
  }

  public void scrollFromEditor(@NotNull FileEditor editor) {
    if (editor instanceof TextEditor textEditor) {
      scrollFromEditor(textEditor.getEditor());
    }
  }

  public void scrollFromEditor(@NotNull Editor editor) {
    if (editor.getProject() != project) return;
    if (adapter == null) return;
    var project = editor.getProject();
    if (project == null) return;
    var file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
    if (!(file instanceof AyaPsiFile ayaFile)) return;
    var node = adapter.findNode(ayaFile, editor.getCaretModel().getOffset());
    if (node != null) {
      select(node.component2());
      if (node.component3() && node.component2().getParentPath() != null)
        select(node.component2().getParentPath());
    }
  }

  public void scrollToEditor(boolean focus) {
    if (adapter == null) return;
    nodeOf(getLastSelectedPathComponent(), adapter::findElement)
      .forEach(e -> AyaPsiUtils.navigate(e, focus));
  }

  public @Nullable Tuple2<T, TreePath> find(@NotNull Predicate<T> predicate) {
    var it = rootNode.depthFirstEnumeration().asIterator();
    while (it.hasNext()) {
      var n = it.next();
      if (!(n instanceof DefaultMutableTreeNode treeNode)) continue;
      var is = nodeOf(n, (T t) -> t).getOrNull();
      if (is != null && predicate.test(is)) {
        return Tuple.of(is, new TreePath(treeNode.getPath()));
      }
    }
    return null;
  }

  public void select(@NotNull TreePath path) {
    setSelectionPath(path);
    scrollToPath(path);
  }

  private void scrollToPath(@NotNull TreePath path) {
    makeVisible(path);
    var bounds = getPathBounds(path);
    if (bounds == null) return;
    var parent = getParent();
    if (parent instanceof JViewport) {
      var width = parent.getParent() instanceof JScrollPane pane ? pane.getVerticalScrollBar().getWidth() : 0;
      bounds.width = min(bounds.width, max(parent.getWidth() - bounds.x - width, 0));
    } else {
      bounds.x = 0;
    }
    scrollRectToVisible(bounds);
    if (getAccessibleContext() instanceof AccessibleJTree tree)
      tree.fireVisibleDataPropertyChange();
  }

  private void updateNode(@NotNull ImmutableSeq<T> nodes) {
    // TODO: be incremental
    rootNode.removeAllChildren();
    nodes.forEach(n -> updateNode(rootNode, n));
    rootNodeModel.reload();
  }

  private void updateNode(@NotNull DefaultMutableTreeNode parent, @NotNull T node) {
    var model = new DefaultMutableTreeNode(node);
    parent.add(model);
    node.children().forEach(child -> updateNode(model, child));
  }

  public void setAdapter(@NotNull NodeAdapter<T> adapter) {
    this.adapter = adapter;
    this.setCellRenderer(new TreeElementRenderer<>(adapter));
  }

  @Override public @NotNull String convertValueToText(
    @Nullable Object value, boolean selected, boolean expanded,
    boolean leaf, int row, boolean hasFocus
  ) {
    if (adapter == null) return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    return nodeOf(value, adapter::renderTitle).getOrElse(()
      -> super.convertValueToText(value, selected, expanded, leaf, row, hasFocus));
  }

  private class ScrollFrom extends AutoScrollFromSourceHandler {
    public ScrollFrom(@NotNull Project project, @NotNull AyaTreeView<T> treeView) {
      super(project, treeView, project);
      install();
    }

    @Override public void install() {
      EditorFactory.getInstance().getEventMulticaster().addCaretListener(new CaretListener() {
        @Override public void caretPositionChanged(@NotNull CaretEvent event) {
          selectInAlarm(event.getEditor());
        }
      }, myProject);
    }

    private void selectInAlarm(@Nullable Editor editor) {
      if (editor != null && AyaTreeView.this.isShowing() && isAutoScrollEnabled()) {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(() -> AyaTreeView.this.scrollFromEditor(editor), getAlarmDelay(), getModalityState());
      }
    }

    @Override protected boolean isAutoScrollEnabled() {
      return AyaSettingService.getInstance().autoScrollFromSource;
    }

    @Override protected void setAutoScrollEnabled(boolean enabled) {
      AyaSettingService.getInstance().autoScrollFromSource = enabled;
    }

    @Override protected void selectElementFromEditor(@NotNull FileEditor editor) {
      AyaTreeView.this.scrollFromEditor(editor);
    }
  }

  private class ScrollTo extends AutoScrollToSourceHandler {
    public ScrollTo(@NotNull AyaTreeView<T> treeView) {
      install(treeView);
    }

    @Override protected boolean isAutoScrollMode() {
      return AyaSettingService.getInstance().autoScrollToSource;
    }

    @Override protected void setAutoScrollMode(boolean state) {
      AyaSettingService.getInstance().autoScrollToSource = state;
    }

    @Override protected void scrollToSource(Component tree) {
      AyaTreeView.this.scrollToEditor(false);
    }
  }

  private static class TreeElementRenderer<T extends Node<T>> extends LabelBasedRenderer.Tree {
    private final @NotNull NodeAdapter<T> adapter;

    private TreeElementRenderer(@NotNull NodeAdapter<T> adapter) {
      this.adapter = adapter;
    }

    @Override public @NotNull Component getTreeCellRendererComponent(
      @NotNull JTree tree, @Nullable Object value,
      boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus
    ) {
      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      nodeOf(value, adapter::renderIcon).forEach(this::setIcon);
      return this;
    }
  }

  private static <T extends Node<T>, U> Option<U> nodeOf(@Nullable Object value, @NotNull Function<T, U> mapper) {
    if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof Node<?> ayaNode) {
      //noinspection unchecked
      var t = (T) ayaNode;
      return Option.ofNullable(mapper.apply(t));
    }
    return Option.none();
  }
}
