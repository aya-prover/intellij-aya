package org.aya.intellij.actions.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.aya.intellij.service.AyaSettingService

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (AyaLsp.isActive(project)) return

    val settings = AyaSettingService.getInstance()
    if (!settings.lspEnable()) return

    // TODO: start aya lsp?
  }
}

private val initLock: Mutex = Mutex()

suspend fun startLsp(project: Project) {
  initLock.withLock {
    if (!AyaLsp.isActive(project)) {
      AyaLsp.start(project)
    }
  }
}
