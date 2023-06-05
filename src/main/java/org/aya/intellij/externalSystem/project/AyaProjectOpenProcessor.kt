package org.aya.intellij.externalSystem.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import kotlinx.coroutines.runBlocking
import org.aya.intellij.AyaConstants

class AyaProjectOpenProcessor : ProjectOpenProcessor() {
  override fun getName(): String {
    return AyaConstants.AYA_NAME
  }

  override fun canOpenProject(file: VirtualFile): Boolean {
    return AyaOpenProjectProvider().canOpenProject(file)
  }

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
    return runBlocking {
      AyaOpenProjectProvider().openProject(virtualFile, projectToClose, forceOpenInNewFrame)
    }
  }

  override fun importProjectAfterwards(project: Project, file: VirtualFile) {
    AyaOpenProjectProvider().linkToExistingProject(file, project)
  }
}
