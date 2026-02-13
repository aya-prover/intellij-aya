@file:JvmName("AyaLspUtils")

package org.aya.intellij.actions.lsp

import com.intellij.concurrency.currentThreadContext
import com.intellij.concurrency.installThreadContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.platform.locking.impl.getGlobalNestedLockingThreadingSupport
import com.intellij.util.concurrency.ThreadingAssertions
import kotlinx.coroutines.*
import org.aya.intellij.actions.lsp.library.IJLibrarySource
import org.aya.intellij.externalSystem.ProjectCoroutineScope
import org.aya.intellij.psi.AyaPsiFile
import org.aya.lsp.utils.Log
import java.util.function.Consumer

/**
 * Schedule a task that requires an [AyaLsp].
 * Note that all object obtained from [AyaLsp] or [org.aya.lsp.server.AyaLanguageServer] may be
 * invalid (or changed unexpectedly) after the task completed.
 *
 * Some lsp operations involve read action, thus DO NOT [runBlocking] inside a read action/read permit and [useLsp], which will cause
 * a UI freeze/deadlock, see [org.aya.intellij.actions.InlayHints.Collector.collectHintsForFile]
 */
fun <R> Project.useLsp(orElse: () -> R, block: (AyaLsp) -> R): Deferred<R> {
  val project = this
  val lsp = AyaLsp.of(project) ?: return CompletableDeferred(orElse())
  return lsp.async { block(lsp) }
}

/**
 * Share read action with lsp thread
 */
fun <R> Project.useLspWithSharedRead(orElse: () -> R, block: (AyaLsp) -> R): Deferred<R> {
  val project = this
  val lsp = AyaLsp.of(project) ?: return CompletableDeferred(orElse())

  ThreadingAssertions.assertReadAccess()
  val (ctx, cleanUp) = getGlobalNestedLockingThreadingSupport()
    .getPermitAsContextElement(currentThreadContext(), true)

  return lsp.async {
    installThreadContext(ctx, true) { block(lsp) }
  }.also { cleanUp() }
}

fun <R> Project.useLspSmartRead(orElse: () -> R, block: (AyaLsp) -> R): Deferred<R> {
  return if (ApplicationManager.getApplication().isReadAccessAllowed) {
    useLspWithSharedRead(orElse, block)
  } else {
    useLsp(orElse, block)
  }
}

fun Project.useLsp(block: (AyaLsp) -> Unit): Job {
  val project = this
  val lsp = AyaLsp.of(project) ?: return Job().apply { complete() }
  return lsp.launch { block(lsp) }
}

fun Project.useLspBlocking(block: Consumer<AyaLsp>) {
  runBlocking {
    useLsp { block.accept(it) }.join()
  }
}

fun Project.useLspAsync(block: Consumer<AyaLsp>) {
  ProjectCoroutineScope.getCoroutineScope(this).launch {
    useLsp { block.accept(it) }
  }
}

/**
 * The whole thing depends on a fact that the lsp is only used in a single thread.
 *
 * @param orElse true if lsp is active but no source for [file], false if lsp is inactive
 */
fun <R> Project.useLsp(file: AyaPsiFile, orElse: (Boolean) -> R, onCancel: () -> R, block: (AyaLsp) -> R): Deferred<R> {
  return useLspSmartRead({ orElse(false) }) { lsp ->
    val source = lsp.sourceFileOf(file) ?: return@useLspSmartRead orElse(true)

    if (source is IJLibrarySource) {
      // TODO: check if file is unchanged (same as disk version) and avoid unnecessary recompilation
      // Q: should we save the previous [source.psiFile]?
      // A: I guess no, this method (useLsp) should not be called in lsp-thread,
      //    thus [source.psiFile] should be null
      source.psiFile = file
      lsp.recompile {
        // TODO: not sure if we should update the highlights
        Log.i("[intellij-aya] In Memory Compilation finished.")
      }

      // TODO: we may check cancellation here, but the block here is not suspendable
      // due to installThreadContext is not inline

      return@useLspSmartRead block(lsp).also {
        source.psiFile = null
      }
    } else {
      block(lsp)
    }
  }
}
