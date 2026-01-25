package org.aya.intellij.actions.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
  }
}

private val initLock: Mutex = Mutex()

suspend fun startLsp(project: Project) {
  if (AyaLsp.isActive(project)) return
  initLock.withLock {
    if (!AyaLsp.isActive(project)) {
      AyaLsp.start(project)
    }
  }
}
