package org.aya.intellij.externalSystem.project

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
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
  companion object {
    private val LOG = Logger.getInstance(AyaOpenProjectProvider::class.java)
  }

  override val systemId: ProjectSystemId = AyaConstants.SYSTEM_ID

  override fun isProjectFile(file: VirtualFile): Boolean {
    return !file.isDirectory && AyaConstants.BUILD_FILE_NAME == file.name
  }

  override fun linkToExistingProject(projectFile: VirtualFile, project: Project) {
    LOG.info("Linking file '${projectFile.path}' to project '${project.name}'")

    if (ExternalSystemUtil.confirmLoadingUntrustedProject(project, AyaConstants.SYSTEM_ID)) {
      val projectDir = getProjectDirectory(projectFile)
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
