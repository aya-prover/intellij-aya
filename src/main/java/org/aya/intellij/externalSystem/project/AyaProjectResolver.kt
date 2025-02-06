package org.aya.intellij.externalSystem.project

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.vfs.VfsUtil
import org.aya.intellij.AyaConstants
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.externalSystem.settings.AyaExecutionSettings
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class AyaProjectResolver : ExternalSystemProjectResolver<AyaExecutionSettings> {
  companion object {
    private val LOG = Logger.getInstance(AyaProjectResolver::class.java)
  }

  val moduleType: ModuleType<*> = ModuleTypeManager.getInstance().defaultModuleType

  fun tryInitializeLsp(settings: AyaExecutionSettings) {
    LOG.info("Initializing Lsp")
    if (!AyaLsp.isActive(settings.project)) {
      val ayaJson = VfsUtil.findFile(settings.buildFilePath, true)
        ?: throw IllegalStateException("${AyaConstants.BUILD_FILE_NAME} not found")
      AyaLsp.start(ayaJson, settings.project)
    } else {
      LOG.info("Lsp was initialized")
    }
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

  fun resolveProjectFileDir(settings: AyaExecutionSettings): Path {
    return settings.projectFileDir?.toAbsolutePath()
      ?: settings.linkedProjectPath.resolve(".idea")
  }

  fun createPreviewProjectInfo(projectNode: DataNode<ProjectData>, moduleFileDir: Path, projectPath: Path) {
    val projectData = projectNode.data
    val moduleId = moduleType.id

    projectNode.createChild(
      ProjectKeys.MODULE,
      ModuleData(
        projectData.externalName, AyaConstants.SYSTEM_ID, moduleId,
        projectData.externalName, moduleFileDir.toString(), projectPath.toString(),
      ),
    ).createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(AyaConstants.SYSTEM_ID, projectPath.toString()))
  }

  /**
   * Resolve aya project structure to idea project structure.
   * The idea project structure is exactly what you see in `Project Structure - Project Settings - Modules`
   *
   * @param isPreviewMode false if AyaSettingsService.AyaState.UseIntegration
   */
  override fun resolveProjectInfo(
    id: ExternalSystemTaskId,
    projectPath: String,
    isPreviewMode: Boolean,
    settings: AyaExecutionSettings?,
    policy: ProjectResolverPolicy?,
    listener: ExternalSystemTaskNotificationListener,
  ): DataNode<ProjectData>? {
    LOG.info("Resolving project $projectPath with isPreviewMode=$isPreviewMode")

    // TODO: When is settings null?
    if (settings == null) return null

    val nioProjectPath = Path.of(projectPath).toAbsolutePath()
    assert(nioProjectPath.isDirectory())      // We will solve other cases when assertion failed

    // I am not sure if they are equal, so we need some experiment
    if (nioProjectPath != settings.linkedProjectPath) {
      LOG.error("Assumption Failed")
    }

    // FIXME: should the param `ideProjectFileDirectoryPath` be `projectFileDir`?
    val projectData = ProjectData(AyaConstants.SYSTEM_ID, nioProjectPath.name, projectPath, projectPath)
    val projectNode = DataNode(ProjectKeys.PROJECT, projectData, null).apply {
      createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(AyaConstants.SYSTEM_ID, projectPath))
    }

    val projectFileDir = resolveProjectFileDir(settings)
    val linkedProjectPath = settings.linkedProjectPath

    if (isPreviewMode) {
      createPreviewProjectInfo(projectNode, projectFileDir, linkedProjectPath)
      return projectNode
    }

    tryInitializeLsp(settings)
    val moduleResolver = AyaModuleResolver(projectNode, moduleType.id, projectFileDir.toString(), linkedProjectPath.toString())
    doResolveModules(settings, moduleResolver)

    return projectNode
  }

  override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
    return false    // Unsupported for now!!
  }
}
