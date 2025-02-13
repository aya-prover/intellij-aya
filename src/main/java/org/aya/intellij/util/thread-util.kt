package org.aya.intellij.util

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction

fun runWriteAction(block: () -> Unit) {
  WriteAction.run<Throwable>(block)
}

fun <T> computeWriteAction(block: () -> T): T {
  return WriteAction.compute<T, Throwable>(block)
}

fun runReadAction(block: () -> Unit) {
  ReadAction.run<Throwable>(block)
}

fun <T> computeReadAction(block: () -> T): T {
  return ReadAction.compute<T, Throwable>(block)
}
