package org.aya.intellij.actions.lsp

import com.intellij.openapi.project.Project
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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
