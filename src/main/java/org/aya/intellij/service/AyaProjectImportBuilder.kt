package org.aya.intellij.service

import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.packaging.artifacts.ModifiableArtifactModel
import com.intellij.platform.backend.observation.launchTracked
import com.intellij.projectImport.ProjectImportBuilder
import org.aya.intellij.AyaBundle
import org.aya.intellij.externalSystem.ProjectCoroutineScope
import org.aya.intellij.externalSystem.project.AyaOpenProjectProvider
import org.aya.intellij.ui.AyaIcons
import javax.swing.*

/**
 * I don't know what the type parameter is used for, gradle use [Any]
 */
class AyaProjectImportBuilder : ProjectImportBuilder<Any>() {
  override fun commit(
    project: Project?,
    model: ModifiableModuleModel?,
    modulesProvider: ModulesProvider?,
    artifactModel: ModifiableArtifactModel?,
  ): MutableList<Module> {
    val fileToImport = this.fileToImport

    if (project != null && fileToImport != null) {
      ProjectCoroutineScope.getCoroutineScope(project).launchTracked {
        AyaOpenProjectProvider.linkAndSyncAyaProject(project, fileToImport)
      }
    }

    return mutableListOf()
  }

  override fun getName(): String = AyaBundle.message("aya.name")

  // TODO: better icon
  override fun getIcon(): Icon = AyaIcons.AYA_FILE

  override fun isMarked(element: Any?): Boolean = true

  override fun setOpenProjectSettingsAfter(on: Boolean) {
  }
}
