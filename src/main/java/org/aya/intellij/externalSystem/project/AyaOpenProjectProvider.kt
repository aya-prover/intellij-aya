package org.aya.intellij.externalSystem.project

import com.intellij.ide.IdeBundle
import com.intellij.ide.trustedProjects.TrustedProjectsDialog
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.aya.intellij.AyaConstants
import org.aya.intellij.externalSystem.settings.AyaProjectSettings
import org.aya.intellij.service.AyaSettingService
import java.nio.file.Path

class AyaOpenProjectProvider : AbstractOpenProjectProvider() {
  companion object {
    private val LOG = Logger.getInstance(AyaOpenProjectProvider::class.java)

    suspend fun linkAndSyncAyaProject(project: Project, projectFile: String) {
      AyaOpenProjectProvider().linkToExistingProjectAsync(projectFile, project)
    }
  }

  override val systemId: ProjectSystemId = AyaConstants.SYSTEM_ID

  override fun isProjectFile(file: VirtualFile): Boolean {
    return !file.isDirectory && AyaConstants.BUILD_FILE_NAME == file.name
  }

  suspend fun doLinkProject(projectDir: VirtualFile, project: Project) {
    val projectSettings = AyaProjectSettings.createLinkSettings(projectDir, project)

    ExternalSystemApiUtil.getSettings(project, AyaConstants.SYSTEM_ID).linkProject(projectSettings)

    val importSpec = ImportSpecBuilder(project, AyaConstants.SYSTEM_ID)
    val shouldPreview = !AyaSettingService.getInstance().lspEnable()

    if (shouldPreview) {
      importSpec.usePreviewMode()
    }

    ExternalSystemUtil.refreshProject(
      projectSettings.externalProjectPath.toString(),
      importSpec,
    )
  }

  private suspend fun confirmUntrustedProject(projectPath: Path, project: Project): Boolean {
    return TrustedProjectsDialog.confirmOpeningOrLinkingUntrustedProject(
      projectPath, project,
      IdeBundle.message("untrusted.project.link.dialog.title", systemId.readableName, projectPath.fileName),
    )
  }

  /**
   * @param projectFile a [VirtualFile] to a directory or file that represents an external system project,
   *                    i.e. `build.gradle` or a directory contains it.
   */
  override suspend fun linkProject(projectFile: VirtualFile, project: Project) {
    LOG.info("Linking file '${projectFile.path}' to project '${project.name}'")

    val projectDir = getProjectDirectory(projectFile)
    val projectPath = projectDir.toNioPath()
    val isTrusted = confirmUntrustedProject(projectPath, project)

    if (!isTrusted) {
      return
    }

    doLinkProject(projectDir, project)
  }
}
