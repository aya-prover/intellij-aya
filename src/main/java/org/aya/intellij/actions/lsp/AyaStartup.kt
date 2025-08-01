package org.aya.intellij.actions.lsp

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.aya.intellij.AyaConstants
import org.aya.intellij.actions.lsp.AyaStartup.Companion.LOGGER
import org.aya.intellij.externalSystem.settings.AyaSettings
import org.aya.intellij.service.AyaSettingService

class AyaStartup : ProjectActivity {
  companion object {
    val LOGGER = Logger.getInstance(AyaStartup::class.java)
  }

  override suspend fun execute(project: Project) {
    val settings = AyaSettingService.getInstance()
    if (!settings.lspEnable()) return
    refreshAllAyaProjects(project)
  }
}

private val initLock: Mutex = Mutex()

fun refreshAllAyaProjects(project: Project) {
  val externalSystemSettings = AyaSettings.getInstance(project)

  for (externalProject in externalSystemSettings.linkedProjectsSettings) {
    val spec = ImportSpecBuilder(project, AyaConstants.SYSTEM_ID)
      .build()

    val path = externalProject.externalProjectPath
    LOGGER.info("Automatically refreshing external project: $path")
    ExternalSystemUtil.refreshProject(path, spec)
  }
}

suspend fun startLsp(project: Project) {
  if (AyaLsp.isActive(project)) return
  initLock.withLock {
    if (!AyaLsp.isActive(project)) {
      AyaLsp.start(project)
    }
  }
}
