package org.aya.intellij.externalSystem.project

import com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.aya.intellij.AyaConstants
import org.aya.intellij.externalSystem.settings.AyaProjectSettings
import org.aya.intellij.service.AyaSettingService

class AyaOpenProjectProvider : AbstractOpenProjectProvider() {
  private fun getProjectDirectory(file: VirtualFile): VirtualFile? {
    return if (file.isDirectory) file else file.parent
  }

  override fun isProjectFile(file: VirtualFile): Boolean {
    return !file.isDirectory && AyaConstants.BUILD_FILE_NAME == file.name
  }

  override fun linkToExistingProject(projectFile: VirtualFile, project: Project) {
    if (ExternalSystemUtil.confirmLoadingUntrustedProject(project, AyaConstants.SYSTEM_ID)) {
      val projectDir = getProjectDirectory(projectFile) ?: return
      val projectSettings = AyaProjectSettings.createLinkSettings(projectDir, project) ?: return

      ExternalSystemApiUtil.getSettings(project, AyaConstants.SYSTEM_ID).linkProject(projectSettings)

      ExternalSystemUtil.refreshProject(
        projectSettings.externalProjectPath.toString(),
        ImportSpecBuilder(project, AyaConstants.SYSTEM_ID)
          .usePreviewMode()
          .use(ProgressExecutionMode.MODAL_SYNC),
      )

      if (AyaSettingService.getInstance().ayaLspState == AyaSettingService.AyaState.UseIntegration) {
        ExternalProjectsManager.getInstance(project).runWhenInitialized {
          ExternalSystemUtil.refreshProject(
            projectSettings.externalProjectPath.toString(),
            ImportSpecBuilder(project, AyaConstants.SYSTEM_ID),
          )
        }
      }
    }
  }
}
