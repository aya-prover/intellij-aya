package org.aya.intellij.externalSystem.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.projectImport.ProjectOpenProcessor
import org.aya.intellij.AyaConstants

class AyaProjectOpenProcessor : ProjectOpenProcessor() {
  override val name: String = AyaConstants.AYA_NAME

  override fun canOpenProject(file: VirtualFile): Boolean {
    return AyaOpenProjectProvider().canOpenProject(file)
  }

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
    val modalOwner = projectToClose?.let(ModalTaskOwner::project) ?: ModalTaskOwner.guess()
    return runWithModalProgressBlocking(modalOwner, "Opening Aya Project") {
      AyaOpenProjectProvider().openProject(virtualFile, projectToClose, forceOpenInNewFrame)
    }
  }

  override suspend fun openProjectAsync(
    virtualFile: VirtualFile,
    projectToClose: Project?,
    forceOpenInNewFrame: Boolean,
  ): Project? {
    // TODO: what is the difference to above??
    return super.openProjectAsync(virtualFile, projectToClose, forceOpenInNewFrame)
  }

  override fun canImportProjectAfterwards(): Boolean {
    return true
  }

  // FIXME: Doesn't work for now
  override suspend fun importProjectAfterwardsAsync(project: Project, file: VirtualFile) {
    AyaOpenProjectProvider().linkToExistingProjectAsync(file, project)
  }
}
