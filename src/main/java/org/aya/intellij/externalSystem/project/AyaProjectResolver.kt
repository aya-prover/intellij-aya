package org.aya.intellij.externalSystem.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.ModuleTypeId
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.io.isDirectory
import org.aya.intellij.AyaConstants
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.externalSystem.settings.AyaExecutionSettings
import java.nio.file.Path
import kotlin.io.path.name

class AyaProjectResolver : ExternalSystemProjectResolver<AyaExecutionSettings> {
  fun initializeLsp(settings: AyaExecutionSettings) {
    val ayaJson = VfsUtil.findFile(settings.buildFilePath, true)
      ?: throw IllegalStateException("${AyaConstants.BUILD_FILE_NAME} not found")
    AyaLsp.start(ayaJson, settings.project)
  }

  fun doResolveModules(settings: AyaExecutionSettings, resolver: AyaModuleResolver): Boolean {
    return AyaLsp.useUnchecked(settings.project, { false }) { lsp ->
      val rootLibrary = lsp.entryLibrary
      if (rootLibrary == null) {
        false
      } else {
        resolver.resolve(null, rootLibrary)
        true
      }
    }
  }

  /**
   * @param isPreviewMode false if AyaSettingsService.AyaState.UseIntegration
   */
  override fun resolveProjectInfo(
    id: ExternalSystemTaskId,
    projectPath: String,
    isPreviewMode: Boolean,
    settings: AyaExecutionSettings?,
    listener: ExternalSystemTaskNotificationListener,
  ): DataNode<ProjectData>? {
    // TODO: When is settings null?
    if (settings == null) return null

    val nioProjectPath = Path.of(projectPath).toAbsolutePath()
    assert(nioProjectPath.isDirectory())      // We will solve other cases when assertion failed

    val projectData = ProjectData(AyaConstants.SYSTEM_ID, nioProjectPath.name, projectPath, projectPath)
    val projectNode = DataNode(ProjectKeys.PROJECT, projectData, null).apply {
      createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(AyaConstants.SYSTEM_ID, projectPath))
    }

    val projectFileDir = settings.projectFileDir?.toAbsolutePath()?.toString()
      ?: nioProjectPath.resolve(".idea").toString()

    if (! isPreviewMode) {
      initializeLsp(settings)
      val moduleResolver = AyaModuleResolver(projectNode, ModuleTypeId.JAVA_MODULE, projectFileDir, projectFileDir)
      doResolveModules(settings, moduleResolver)
    } else {
      projectNode.createChild(ProjectKeys.MODULE, ModuleData(
        projectData.externalName, AyaConstants.SYSTEM_ID, ModuleTypeId.JAVA_MODULE,
        projectData.externalName, projectFileDir, projectFileDir))
        .createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(AyaConstants.SYSTEM_ID, projectPath))
    }

    return projectNode
  }

  override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
    return false    // Unsupported for now!!
  }
}
