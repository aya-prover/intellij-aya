package org.aya.intellij.actions.lsp

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.aya.intellij.AyaConstants
import org.aya.intellij.externalSystem.settings.AyaSettings
import org.aya.lsp.utils.Log

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
  }
}

private val initLock: Mutex = Mutex()

fun refreshAllAyaProjects(project: Project) {
  val externalSystemSettings = AyaSettings.getInstance(project)

  for (externalProject in externalSystemSettings.linkedProjectsSettings) {
    val spec = ImportSpecBuilder(project, AyaConstants.SYSTEM_ID)
      .build()

    val path = externalProject.externalProjectPath
    Log.i("[intellij-aya] Refreshing external project: $path")
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
