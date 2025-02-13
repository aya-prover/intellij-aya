package org.aya.intellij.actions.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.aya.intellij.service.AyaSettingService

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (AyaLsp.isActive(project)) return

    val settings = AyaSettingService.getInstance()
    if (!settings.lspEnable()) return

    // TODO: start aya lsp?
  }
}
