package org.aya.intellij.externalSystem.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import org.aya.intellij.AyaBundle
import org.aya.intellij.externalSystem.canOpenAyaProject

class AyaProjectOpenProcessor : ProjectOpenProcessor() {
  override val name: String = AyaBundle.message("aya.name")

  override fun canOpenProject(file: VirtualFile): Boolean {
    return canOpenAyaProject(file)
  }

  override suspend fun openProjectAsync(
    virtualFile: VirtualFile,
    projectToClose: Project?,
    forceOpenInNewFrame: Boolean,
  ): Project? {
    return AyaOpenProjectProvider().openProject(virtualFile, projectToClose, forceOpenInNewFrame)
  }

  override fun canImportProjectAfterwards(): Boolean {
    return true
  }

  // FIXME: Doesn't work for now
  override suspend fun importProjectAfterwardsAsync(project: Project, file: VirtualFile) {
    AyaOpenProjectProvider().linkToExistingProjectAsync(file, project)
  }
}
