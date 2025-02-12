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
import org.aya.intellij.util.computeReadAction
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class AyaProjectResolver : ExternalSystemProjectResolver<AyaExecutionSettings> {
  companion object {
    private val LOG = Logger.getInstance(AyaProjectResolver::class.java)
  }

  val moduleType: ModuleType<*> = ModuleTypeManager.getInstance().defaultModuleType

  /**
   * Initialize lsp and trying to register library for [settings]
   */
  private fun tryInitializeLsp(settings: AyaExecutionSettings) {
    val ayaProjectDir = VfsUtil.findFile(settings.linkedProjectPath, true)
    if (!AyaLsp.isActive(settings.project)) {
      LOG.info("Initializing Lsp")
      AyaLsp.start(settings.project)
        .registerLibrary(ayaProjectDir ?: return)
    } else {
      ayaProjectDir ?: return

      LOG.info("Lsp was initialized")
      AyaLsp.useUnchecked(settings.project) { lsp ->
        if (!lsp.isLibraryLoaded(ayaProjectDir)) {
          LOG.info("Loading library: ${ayaProjectDir.toNioPath()}")
          lsp.registerLibrary(ayaProjectDir)
        } else {
          LOG.info("Library was loaded: ${ayaProjectDir.toNioPath()}")
        }
      }
    }
  }

  /**
   * @return whether resolve success, [resolver] will not mutate the [AyaModuleResolver.rootNode] if failed.
   */
  private fun doResolveModules(settings: AyaExecutionSettings, resolver: AyaModuleResolver): Boolean {
    return AyaLsp.useUnchecked(settings.project, { false }) { lsp ->
      val rootLibrary = computeReadAction {
        val file = VfsUtil.findFile(settings.linkedProjectPath, true)
          ?: return@computeReadAction null
        lsp.getLoadedLibrary(file)
      }

      if (rootLibrary == null) {
        false
      } else {
        resolver.resolve(null, rootLibrary)
        true
      }
    }
  }

  private fun resolveProjectFileDir(settings: AyaExecutionSettings): Path {
    return settings.projectFileDir?.toAbsolutePath()
      ?: settings.linkedProjectPath.resolve(AyaConstants.IDEA_PROJECT_FILE_DIR)
  }

  private fun createPreviewProjectInfo(projectNode: DataNode<ProjectData>, moduleFileDir: Path, projectPath: Path) {
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

  private fun makeProjectNode(projectFileDir: Path, projectPath: Path): DataNode<ProjectData> {
    val projectName = projectPath.name
    val projectPathString = projectPath.toString()
    val projectData = ProjectData(AyaConstants.SYSTEM_ID, projectName, projectFileDir.toString(), projectPathString)
    return DataNode(ProjectKeys.PROJECT, projectData, null).apply {
      createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(AyaConstants.SYSTEM_ID, projectPathString))
    }
  }

  /**
   * Resolve aya project structure to idea project structure.
   * The idea project structure is exactly what you see in `Project Structure - Project Settings - Modules`
   *
   * TODO: [projectPath] is documented as the path to the config file of external system, but we got a directory
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
    // TODO: ^what does this mean?

    val linkedProjectPath = settings.linkedProjectPath
    // I am not sure if they are equal, so we need some experiment
    if (nioProjectPath != linkedProjectPath) {
      LOG.error("Assumption Failed")
    }

    val projectFileDir = resolveProjectFileDir(settings)
    val projectNode = makeProjectNode(projectFileDir, nioProjectPath)

    if (!isPreviewMode) {
      tryInitializeLsp(settings)
      val moduleResolver = AyaModuleResolver(projectNode, moduleType.id, projectFileDir.toString(), linkedProjectPath.toString())
      val success = doResolveModules(settings, moduleResolver)
      if (success) return projectNode
      // failed, use preview project info
    }

    // now: isPreviewMode or ! success

    createPreviewProjectInfo(projectNode, projectFileDir, linkedProjectPath)
    return projectNode
  }

  override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
    return false    // Unsupported for now!!
  }
}
