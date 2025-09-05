package org.aya.intellij.actions

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import kotlinx.coroutines.runBlocking
import org.aya.intellij.actions.lsp.useLsp
import org.aya.intellij.psi.AyaPsiFile
import org.aya.intellij.util.computeReadAction

class InlayHints : InlayHintsProvider {
  class Collector(val project: Project) : OwnBypassCollector {
    override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
      file as AyaPsiFile

      val hints = computeReadAction {
        if (project.isDisposed) return@computeReadAction null
        runBlocking {
          project.useLsp(file, { null }) { it.collectInlayHint(file) }
        }
      } ?: return

      hints.forEach { hint ->
        val hintOffset = hint.sourcePos.tokenEndIndex + 1
        sink.addPresentation(
          InlineInlayPosition(hintOffset, true),
          hintFormat = HintFormat.default,
        ) {
          // TODO: choose appropriate renderer
          text(hint.doc().debugRender())
          // we can link the inlay hint with resolved psi element here, see
          // https://github.com/JetBrains/intellij-community/blob/idea/252.23892.409/java/java-impl/src/com/intellij/codeInsight/hints/JavaImplicitTypeDeclarativeInlayHintsProvider.kt
        }
      }
    }
  }

  override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
    if (file !is AyaPsiFile) return null
    return Collector(file.project)
  }
}
