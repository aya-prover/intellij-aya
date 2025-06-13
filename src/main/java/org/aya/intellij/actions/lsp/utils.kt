package org.aya.intellij.actions.lsp

import com.intellij.openapi.project.Project
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Schedule a task that requires an [AyaLsp].
 * Note that all object obtained from [AyaLsp] or [org.aya.lsp.server.AyaLanguageServer] may be
 * invalid (or changed unexpectedly) after the task completed.
 */
suspend fun <R> Project.useLsp(orElse: () -> R, block: suspend (AyaLsp) -> R): R {
  val project = this
  val lsp = AyaLsp.of(project) ?: return orElse()
  val deferred = lsp.async { block(lsp) }
  return deferred.await()
}

suspend fun Project.useLsp(block: suspend (AyaLsp) -> Unit) {
  val project = this
  val lsp = AyaLsp.of(project) ?: return
  lsp.launch { block(lsp) }.join()
}
