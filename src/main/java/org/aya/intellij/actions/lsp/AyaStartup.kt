package org.aya.intellij.actions.lsp

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.aya.intellij.AyaConstants
import org.aya.intellij.externalSystem.settings.AyaSettings
import org.aya.intellij.service.AyaSettingService

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (AyaLsp.isActive(project)) return

    val settings = AyaSettingService.getInstance()
    if (!settings.lspEnable()) return
    val externalSystemSettings = AyaSettings.getInstance(project)

    for (externalProject in externalSystemSettings.linkedProjectsSettings) {
      val spec = ImportSpecBuilder(project, AyaConstants.SYSTEM_ID)
        .build()
      ExternalSystemUtil.refreshProject(externalProject.externalProjectPath, spec)
    }
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
