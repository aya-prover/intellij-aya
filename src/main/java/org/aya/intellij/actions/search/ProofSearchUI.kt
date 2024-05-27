package org.aya.intellij.actions.search

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.Processor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.aya.intellij.AyaBundle
import org.aya.intellij.psi.utils.AyaPsiUtils
import java.awt.*
import javax.swing.*

class ProofSearchContributorFactory : SearchEverywhereContributorFactory<ProofSearch.Proof> {
  override fun createContributor(initEvent: AnActionEvent) = ProofSearchContributor(initEvent)
}

/** highly inspired from [com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor] */
class ProofSearchContributor(initEvent: AnActionEvent) : WeightedSearchEverywhereContributor<ProofSearch.Proof> {
  private val project: Project = initEvent.getRequiredData(CommonDataKeys.PROJECT)

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
    if (!isEmptyPatternSupported && pattern.isEmpty()) return
    val app = ApplicationManager.getApplication()
    val fetcher = {
      ProofSearch.search(project, true, pattern)
        .forEachBreakable { consumer.process(FoundItemDescriptor(it, 0)) }
    }
    if (app.isUnitTestMode || app.isDispatchThread) fetcher()
    else {
      ProgressIndicatorUtils.yieldToPendingWriteActions()
      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetcher, prog)
    }
  }

  override fun processSelectedItem(selected: ProofSearch.Proof, modifiers: Int, searchText: String) = when (selected) {
    is ProofSearch.Proof.Err -> false
    is ProofSearch.Proof.Yes -> {
      val element = selected.element
      if (element.isValid) AyaPsiUtils.navigate(element, true)
      true
    }
  }

  override fun getDataForItem(item: ProofSearch.Proof, dataId: String) = when {
    CommonDataKeys.PSI_ELEMENT.`is`(dataId) -> (item as? ProofSearch.Proof.Yes)?.element
    else -> null
  }

  override fun getItemDescription(element: ProofSearch.Proof): String? = null
}

class ProofResultRenderer : ListCellRenderer<ProofSearch.Proof> {
  private val definition = SimpleColoredComponent()
  private val signature = SimpleColoredComponent()
  private val sideGap = JBUIScale.scale(UIUtil.getListCellHPadding())
  private val separator = " :" + " ".repeat(sideGap / 2)

  private val where = SimpleColoredComponent().apply {
    isIconOnTheRight = true
  }
  private val root = JPanel(BorderLayout()).apply {
    add(definition, BorderLayout.WEST)
    add(signature, BorderLayout.CENTER)
    add(where, BorderLayout.EAST)
    border = JBUI.Borders.empty(1, sideGap)
  }

  override fun getListCellRendererComponent(
    list: JList<out ProofSearch.Proof>,
    value: ProofSearch.Proof,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean,
  ): Component {
    val pre = value.presentation()

    definition.apply {
      clear()
      val foreground = if (isSelected) list.selectionForeground else list.foreground
      val attributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, foreground)
      append(pre.presentableText ?: "", attributes)
      icon = pre.getIcon(true)
    }

    signature.apply {
      clear()
      val foreground = if (isSelected) list.selectionForeground else list.foreground
      val attributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, foreground)
      append(separator, SimpleTextAttributes.GRAY_ATTRIBUTES)
      append(pre.tooltip ?: "", attributes)
    }

    where.apply {
      clear()
      val attributes = if (isSelected) {
        SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, list.selectionForeground)
      } else {
        SimpleTextAttributes.GRAY_ATTRIBUTES
      }
      isIconOnTheRight = true
      append(pre.locationString ?: "", attributes)
    }

    root.background = UIUtil.getListBackground(isSelected, true)
    root.font = list.font
    return root
  }
}
