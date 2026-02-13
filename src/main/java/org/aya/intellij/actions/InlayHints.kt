package org.aya.intellij.actions

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.aya.intellij.actions.lsp.useLsp
import org.aya.intellij.psi.AyaPsiFile
import java.util.concurrent.atomic.AtomicReference

class InlayHints : InlayHintsProvider {
  @Service(Service.Level.PROJECT)
  class Manager(val project: Project) : Disposable {
    companion object {
      fun getInstance(project: Project): Manager = project.getService(Manager::class.java)
    }

    val currentJob: AtomicReference<Deferred<Any?>?> = AtomicReference(null)
    private var cancelled: Boolean = false

    // we don't care if null represent job cancellation or the job returns null
    fun <T> submit(job: Deferred<T>): T? {
      if (cancelled) {
        job.cancel()
        return null
      }

      val old = currentJob.get()
      val witness = currentJob.compareAndExchange(old, job)
      if (old == witness) {
        // replace successfully
        old?.cancel()
      } else {
        job.cancel()
        return null
      }

      while (!job.isCompleted) {
        try {
          ProgressManager.checkCanceled()
        } catch (e: ProcessCanceledException) {
          job.cancel()
          throw e
        }
      }

      return runBlocking { job.await() }
    }

    override fun dispose() {
      cancelled = true
      currentJob.get()?.cancel()
    }
  }

  class Collector(val project: Project) : OwnBypassCollector {
    override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
      file.virtualFile
      file as AyaPsiFile

      if (project.isDisposed) return
      val manager = Manager.getInstance(project)
      val job = project.useLsp(file, { null }) { lsp ->
        lsp.collectInlayHint(file)
      }

      // in worst case, this cost `2 * job cost` where job cost is basically compilation cost
      val hints = manager.submit(job) ?: return
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
