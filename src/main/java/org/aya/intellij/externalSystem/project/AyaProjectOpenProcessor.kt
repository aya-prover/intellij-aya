package org.aya.intellij.externalSystem.project

import com.intellij.openapi.progress.ModalTaskOwner
import com.intellij.openapi.progress.runBlockingModal
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import org.aya.intellij.AyaConstants

class AyaProjectOpenProcessor : ProjectOpenProcessor() {
  override val name: String = AyaConstants.AYA_NAME

  override fun canOpenProject(file: VirtualFile): Boolean {
    return AyaOpenProjectProvider().canOpenProject(file)
  }

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
    return runBlockingModal(ModalTaskOwner.guess(), "Opening Aya Project") {
      AyaOpenProjectProvider().openProject(virtualFile, projectToClose, forceOpenInNewFrame)
    }
  }

  override fun canImportProjectAfterwards(): Boolean {
    return true
  }

  override fun importProjectAfterwards(project: Project, file: VirtualFile) {
    AyaOpenProjectProvider().linkToExistingProject(file, project)
  }
}
