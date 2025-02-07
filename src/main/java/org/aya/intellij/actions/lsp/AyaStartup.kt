package org.aya.intellij.actions.lsp

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import org.aya.generic.Constants
import org.aya.intellij.service.AyaSettingService

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (AyaLsp.isActive(project)) return

    val settings = AyaSettingService.getInstance()
    if (!settings.lspEnable()) return
    // TODO: what if the user link a sub-directory as (primary) aya project? in this case we should not use `findAyaJson`.

    val ayaJson = findAyaJson(project)
    if (ayaJson != null) {
      if (!JB.fileSupported(ayaJson)) return
      AyaLsp.start(ayaJson, project)
    }
  }

  // TODO: this method has problem when work with external system, as there may have more than 1 modules.
  private fun findAyaJson(project: Project): VirtualFile? {
    val mods = ModuleManager.getInstance(project).modules
    if (mods.size != 1) return null
    val mod = mods[0]
    val contentRoots = ModuleRootManager.getInstance(mod).contentRoots
    if (contentRoots.size != 1) return null
    val root: VirtualFile = contentRoots[0]
    return root.findChild(Constants.AYA_JSON)
  }
}
