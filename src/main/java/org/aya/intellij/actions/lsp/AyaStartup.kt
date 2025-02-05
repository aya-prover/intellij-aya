package org.aya.intellij.actions.lsp

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import org.aya.generic.Constants

class AyaStartup : ProjectActivity {
  override suspend fun execute(project: Project) {
  }

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
