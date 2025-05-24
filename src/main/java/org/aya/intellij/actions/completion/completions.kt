package org.aya.intellij.actions.completion

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiWhiteSpace
import org.aya.ide.action.Completion
import org.intellij.lang.annotations.MagicConstant
import org.javacs.lsp.CompletionItem
import org.javacs.lsp.CompletionItemKind
import org.javacs.lsp.CompletionList

fun CompletionList.toLookupElements(): Array<LookupElement> {
  return this.items.mapIndexed { idx, it -> it.toLookupElement(idx) }.toTypedArray()
}

fun CompletionItem.toLookupElement(index: Int): LookupElement {
  // only detail, kind, label are set
  val tailText: String? = detail

  @MagicConstant(valuesFromClass = CompletionItemKind::class)
  val kind: Int = kind
  val name: String = label

  var builder = LookupElementBuilder.create(name)
    .withInsertHandler { ctx, item ->
      // the completion already insert item, we need to insert a whitespace if necessary
      val nextToken = ctx.file.findElementAt(ctx.tailOffset)

      if (!(nextToken == null || nextToken is PsiWhiteSpace)) {
        ctx.document.insertString(ctx.tailOffset, " ")
      }
    }
  if (tailText != null) builder = builder.withTailText(tailText)
  if (kind != CompletionItemKind.Module) builder = builder.withBoldness(true)

  val element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
  return when (kind) {
    CompletionItemKind.Module -> PrioritizedLookupElement.withGrouping(element, 0)
    CompletionItemKind.Variable -> PrioritizedLookupElement.withGrouping(
      PrioritizedLookupElement.withPriority(element, index.toDouble()), 2,
    )
    // top decl
    else -> PrioritizedLookupElement.withGrouping(element, 1)
  }
}

/**
 * @param index the index of local variable, not used if this is not [Completion.Item.Local]
 * @see org.aya.lsp.actions.CompletionProvider
 */
fun Completion.Item.toLookupElement(index: Int): LookupElement {
  when (this) {
    is Completion.Item.Module -> {
      val element = LookupElementBuilder.create(this.moduleName().toString())
        .withBoldness(true)
        .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)

      return PrioritizedLookupElement.withGrouping(element, 0)
    }

    is Completion.Item.Symbol -> {
      val sig = this.type().easyToString()
      // TODO: provide data name if this is a constructor
      // TODO: provide icon according to the decl kind
      val element = LookupElementBuilder.create(this.name())
        .withBoldness(true)
        .withTailText(sig, false)
        .withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)

      return when (this) {
        is Completion.Item.Decl -> PrioritizedLookupElement.withGrouping(element, 1)
        is Completion.Item.Local -> PrioritizedLookupElement.withPriority(element, index.toDouble())
      }
    }
  }
}
