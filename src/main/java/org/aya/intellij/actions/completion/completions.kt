package org.aya.intellij.actions.completion

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import org.aya.ide.action.Completion

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
