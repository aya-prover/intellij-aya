package org.aya.intellij.proof

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.Processor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.aya.intellij.AyaBundle
import java.awt.*
import java.awt.event.KeyEvent
import javax.swing.*

class ProofSearchContributorFactory : SearchEverywhereContributorFactory<ProofSearch.Proof> {
  override fun createContributor(initEvent: AnActionEvent) = ProofSearchContributor()
}

class ProofSearchContributor : WeightedSearchEverywhereContributor<ProofSearch.Proof> {
  override fun getSearchProviderId(): String = javaClass.name
  override fun getGroupName() = AyaBundle.message("aya.search.proof.group.name")
  override fun getFullGroupName() = AyaBundle.message("aya.search.proof.group.name.full")
  override fun getSortWeight() = 114514
  override fun showInFindResults() = true
  override fun isShownInSeparateTab() = true
  override fun getAdvertisement() = "Aya Proof Search Advertisement"
  override fun getElementsRenderer() = ProofResultRenderer()

  override fun fetchWeightedElements(
    pattern: String,
    prog: ProgressIndicator,
    consumer: Processor<in FoundItemDescriptor<ProofSearch.Proof>>,
  ) {
    // TODO: search proof
  }

  override fun processSelectedItem(
    selected: ProofSearch.Proof,
    modifiers: Int,
    searchText: String,
  ): Boolean {
    // TODO: when user selected, go to it or insert it to editor
    return true
  }

  override fun getDataForItem(element: ProofSearch.Proof, dataId: String): Any? = null
  override fun getItemDescription(element: ProofSearch.Proof): String? = null
}

class ProofResultRenderer : ListCellRenderer<ProofSearch.Proof> {
  private val proof = SimpleColoredComponent()
  private val shortcut = SimpleColoredComponent()
  private val root = JPanel(BorderLayout()).apply {
    add(proof, BorderLayout.CENTER)
    add(shortcut, BorderLayout.EAST)
    val sideGap = if (UIUtil.isUnderWin10LookAndFeel()) 0
    else JBUIScale.scale(UIUtil.getListCellHPadding())
    border = JBUI.Borders.empty(1, sideGap)
  }

  override fun getListCellRendererComponent(
    list: JList<out ProofSearch.Proof>,
    value: ProofSearch.Proof,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean,
  ): Component {
    proof.clear()
    proof.apply {
      val foreground = if (isSelected) list.selectionForeground else list.foreground
      val attributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, foreground)
      val pre = value.presentation()
      append(pre.presentableText ?: "", attributes)
      icon = pre.getIcon(true)
    }

    shortcut.clear()
    shortcut.apply {
      val key = KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))
      val attributes = if (isSelected) {
        SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, list.selectionForeground)
      } else {
        SimpleTextAttributes.GRAY_ATTRIBUTES
      }
      append(AyaBundle.message("aya.search.proof.shortcut.goto", key), attributes)
    }

    root.background = UIUtil.getListBackground(isSelected, true)
    root.font = list.font
    return root
  }
}
