package org.aya.intellij.ui;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.AutoScrollFromSourceHandler;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Option;
import org.aya.intellij.settings.AyaSettingsState;
import org.aya.util.TreeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.function.Function;

/** A super-friendly tree view */
public class AyaTreeView<T extends AyaTreeView.Node<T>> extends Tree {
  public interface Node<T extends Node<T>> extends TreeBuilder.Tree<T> {
  }

  public interface NodeAdapter<T extends Node<T>> {
    @Nullable String renderTitle(@NotNull T node);
    @Nullable Icon renderIcon(@NotNull T node);
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

  public static <T extends Node<T>> @NotNull AyaTreeView<T> create(@NotNull String rootText, boolean showRoot) {
    var rootNode = new DefaultMutableTreeNode(rootText);
    var rootNodeModel = new DefaultTreeModel(rootNode);
    return new AyaTreeView<>(rootNode, rootNodeModel, showRoot);
  }

  private final @NotNull DefaultMutableTreeNode rootNode;
  private final @NotNull DefaultTreeModel rootNodeModel;
  private @Nullable NodeAdapter<T> adapter;

  private AyaTreeView(
    @NotNull DefaultMutableTreeNode rootNode,
    @NotNull DefaultTreeModel rootNodeModel,
    boolean showRoot
  ) {
    super(rootNodeModel);
    setRootVisible(showRoot);
    this.rootNode = rootNode;
    this.rootNodeModel = rootNodeModel;
  }

  public @NotNull NodeBuilder<T> edit() {
    return new NodeBuilder<>(this);
  }

  public void attach(@NotNull Project project, @NotNull SimpleToolWindowPanel rootPanel) {
    var actionGroup = new DefaultActionGroup();
    var actionManager = CommonActionsManager.getInstance();
    var treeExpander = new DefaultTreeExpander(this);

    actionGroup.add(actionManager.createExpandAllAction(treeExpander, this));
    actionGroup.add(actionManager.createCollapseAllAction(treeExpander, this));
    actionGroup.addSeparator();
    actionGroup.add(new ScrollTo<>(project, this).createToggleAction());
    actionGroup.add(new ScrollFrom<>(project, this).createToggleAction());

    var toolbar = ActionManager.getInstance().createActionToolbar(
      AyaToolWindow.TOOLBAR_PLACE, actionGroup, false);
    toolbar.setTargetComponent(rootPanel);
    rootPanel.setToolbar(toolbar.getComponent());
    rootPanel.setContent(ScrollPaneFactory.createScrollPane(this, true));
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

  private static class ScrollFrom<T extends Node<T>> extends AutoScrollFromSourceHandler {
    public ScrollFrom(@NotNull Project project, @NotNull AyaTreeView<T> treeView) {
      super(project, treeView, project);
    }

    @Override protected boolean isAutoScrollEnabled() {
      return AyaSettingsState.getInstance().autoScrollFromSource;
    }

    @Override protected void setAutoScrollEnabled(boolean enabled) {
      AyaSettingsState.getInstance().autoScrollFromSource = enabled;
    }

    @Override protected void selectElementFromEditor(@NotNull FileEditor editor) {
    }
  }

  private static class ScrollTo<T extends Node<T>> extends AutoScrollToSourceHandler {
    public ScrollTo(@NotNull Project project, @NotNull AyaTreeView<T> treeView) {
      install(treeView);
    }

    @Override protected boolean isAutoScrollMode() {
      return AyaSettingsState.getInstance().autoScrollToSource;
    }

    @Override protected void setAutoScrollMode(boolean state) {
      AyaSettingsState.getInstance().autoScrollToSource = state;
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
