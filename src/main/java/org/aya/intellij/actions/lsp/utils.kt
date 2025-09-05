package org.aya.intellij.actions.lsp

import com.intellij.openapi.project.Project
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.aya.intellij.actions.lsp.library.IJLibraryOwner
import org.aya.intellij.actions.lsp.library.IJLibrarySource
import org.aya.intellij.externalSystem.ProjectCoroutineScope
import org.aya.intellij.psi.AyaPsiFile
import java.util.function.Consumer

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

fun Project.useLspBlocking(block: Consumer<AyaLsp>) {
  runBlocking {
    useLsp { block.accept(it) }
  }
}

fun Project.useLspAsync(block: Consumer<AyaLsp>) {
  ProjectCoroutineScope.getCoroutineScope(this).launch {
    useLsp { block.accept(it) }
  }
}

/**
 * The whole thing depends on the lsp is only used in a single thread.
 *
 * @param orElse true if lsp is active but no source for [file], false if lsp is inactive
 */
suspend fun <R> Project.useLsp(file: AyaPsiFile, orElse: (Boolean) -> R, block: suspend (AyaLsp) -> R): R {
  return useLsp({ orElse(false) }) { lsp ->
    val source = lsp.sourceFileOf(file) ?: return@useLsp orElse(true)
    val owner = source.owner

    if (owner is IJLibraryOwner) {
      owner.inMemorySourcesMut().put(source, IJLibrarySource(owner, source, file))
      lsp.recompile(null)   // TODO: not sure if we should update the highlights
      return@useLsp block(lsp).also {
        owner.inMemorySourcesMut().remove(source)
      }
    } else {
      block(lsp)
    }
  }
}
