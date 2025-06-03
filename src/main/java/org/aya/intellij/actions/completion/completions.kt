package org.aya.intellij.actions.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import kala.collection.immutable.ImmutableSeq
import org.aya.parser.AyaPsiElementTypes
import org.intellij.lang.annotations.MagicConstant
import org.javacs.lsp.CompletionItem
import org.javacs.lsp.CompletionItemKind
import org.javacs.lsp.CompletionList
import javax.swing.*

fun CompletionList.toLookupElements(): ImmutableSeq<LookupElement> {
  val length = this.items.size
  return ImmutableSeq.from(this.items)
    .mapIndexed { idx, it -> it.toLookupElement((length - idx).toDouble()) }
}

/**
 * @param priority used if this item is [CompletionItemKind.Variable]
 * @see org.aya.lsp.actions.CompletionProvider
 * @see PrioritizedLookupElement
 */
fun CompletionItem.toLookupElement(priority: Double): LookupElement {
  // only detail, kind, label are set
  val tailText: String? = detail

  @MagicConstant(valuesFromClass = CompletionItemKind::class)
  val kind: Int = kind
  val name: String = label

  var builder = LookupElementBuilder.create(name)
    .withInsertHandler(WhitespaceInsertHandler)

  // TODO: data info for constructors

  // https://intellij-icons.jetbrains.design/
  val icon: Icon? = when (kind) {
    CompletionItemKind.Module -> AllIcons.Nodes.Package
    CompletionItemKind.Variable -> AllIcons.Nodes.Variable
    CompletionItemKind.Function -> AllIcons.Nodes.Function
    CompletionItemKind.Interface -> AllIcons.Nodes.Parameter   // prim
    CompletionItemKind.Constructor -> AllIcons.Nodes.Class  // constructor
    CompletionItemKind.Struct -> AllIcons.Nodes.Interface   // inductive
    CompletionItemKind.Field -> AllIcons.Nodes.Field
    else -> null
  }

  if (icon != null) {
    builder = builder.withIcon(icon)
  }

  if (tailText != null) builder = builder.withTailText(tailText)
  if (kind != CompletionItemKind.Module) builder = builder.withBoldness(true)

  val element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.NEVER_AUTOCOMPLETE)
  return when (kind) {
    CompletionItemKind.Module -> PrioritizedLookupElement.withPriority(element, priority)
    CompletionItemKind.Variable -> PrioritizedLookupElement.withPriority(element, priority)
    // top decl
    else -> PrioritizedLookupElement.withPriority(element, priority)
  }
}

/**
 * Also see the constants in [com.intellij.codeInsight.lookup.Lookup]
 */
object WhitespaceInsertHandler : InsertHandler<LookupElement> {
  /**
   * Includes only the end part of delimiters and whitespace
   *
   * @see org.aya.parser.AyaParserDefinitionBase.DELIMITERS
   */
  val DELIMITERS = StandardPatterns.or(
    PlatformPatterns.psiElement(AyaPsiElementTypes.RPAREN),
    PlatformPatterns.psiElement(AyaPsiElementTypes.RBRACE),
    PlatformPatterns.psiElement().whitespace(),
  )

  override fun handleInsert(ctx: InsertionContext, item: LookupElement) {
    val document = ctx.document
    val file = ctx.file
    val editor = ctx.editor
    val tailOffset = ctx.tailOffset

    // ctx.completionChar
    // Completion Character means which character triggers a completion (not providing completion list, but inserting string)

    // the completion already insert item, we need to insert a whitespace if necessary
    val nextToken = file.findElementAt(tailOffset)

    if (!DELIMITERS.accepts(nextToken)) {
      document.insertString(tailOffset, " ")
      editor.caretModel.moveToOffset(tailOffset + 1)
    }
  }
}
