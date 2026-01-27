package org.aya.intellij.externalSystem.project

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
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.aya.intellij.AyaConstants
import org.aya.intellij.actions.lsp.startLsp
import org.aya.intellij.actions.lsp.useLsp
import org.aya.intellij.externalSystem.ProjectCoroutineScope
import org.aya.intellij.externalSystem.settings.AyaExecutionSettings
import org.aya.intellij.service.AyaSettingService
import org.aya.lsp.utils.Log
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

// TODO: find and fix all places that treat `projectConfigPath` as `projectPath`
class AyaProjectResolver : ExternalSystemProjectResolver<AyaExecutionSettings> {
  val moduleType: ModuleType<*> = ModuleTypeManager.getInstance().defaultModuleType

  /**
   * Initialize lsp and trying to register library for [settings]
   *
   * @implNote this method should be thread-safe
   */
  private suspend fun tryInitializeLsp(settings: AyaExecutionSettings) {
    val ayaProjectDir = VfsUtil.findFile(settings.linkedExternalProjectPath, true) ?: return
    Log.i("[intellij-aya] Initializing Lsp")

    startLsp(settings.project)
    settings.project.useLsp { lsp ->
      val loaded = lsp.isLibraryLoaded(ayaProjectDir)
      Log.i("[intellij-aya] ${if (loaded) "Reloading" else "Loading"} library: ${ayaProjectDir.toNioPath()}")
      lsp.registerLibrary(ayaProjectDir, true)
    }
  }

  /**
   * @return whether resolve success, [resolver] will not mutate the [AyaModuleResolver.rootNode] if failed.
   */
  private suspend fun doResolveModules(settings: AyaExecutionSettings, resolver: AyaModuleResolver): Boolean {
    val file = VfsUtil.findFile(settings.linkedExternalProjectPath, true) ?: return false
    // failed if: 1) lsp is inactivated 2) library not found
    return settings.project.useLsp({ false }) { lsp ->
      val rootLibrary = lsp.getLoadedLibrary(file) ?: return@useLsp false
      resolver.resolve(rootLibrary)
      true
    }
  }

  private fun resolveProjectFileDir(settings: AyaExecutionSettings): Path {
    return settings.projectFileDir?.toAbsolutePath()
      ?: settings.linkedExternalProjectPath.parent.resolve(AyaConstants.IDEA_PROJECT_FILE_DIR)
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

  private fun makeProjectNode(projectFileDir: Path, projectDir: Path): DataNode<ProjectData> {
    val projectName = projectDir.name
    val projectPathString = projectDir.toString()
    val projectData = ProjectData(AyaConstants.SYSTEM_ID, projectName, projectFileDir.toString(), projectPathString)
    return DataNode(ProjectKeys.PROJECT, projectData, null).apply {
      createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(AyaConstants.SYSTEM_ID, projectPathString))
    }
  }

  /**
   * Resolve aya project structure to idea project structure.
   * The idea project structure is exactly what you see in `Project Structure - Project Settings - Modules`.
   *
   * This method must be thread-safe.
   *
   * @param projectPath the project path to the external system project.
   *   Note that the path may not exist, for example, the project is deleted externally
   *   and idea is trying to resolve that project
   *   according to the module data stored in `.idea`.
   *   This path is identical to [org.aya.intellij.externalSystem.settings.AyaProjectSettings.getExternalProjectPath]
   */
  override fun resolveProjectInfo(
    id: ExternalSystemTaskId,
    projectPath: String,
    isPreviewMode: Boolean,
    settings: AyaExecutionSettings?,
    listener: ExternalSystemTaskNotificationListener,
  ): DataNode<ProjectData>? {
    Log.i("[intellij-aya] Resolving project $projectPath with isPreviewMode=$isPreviewMode")

    // TODO: When is settings null?
    if (settings == null) return null

    val nioProjectPath = Path.of(projectPath).toAbsolutePath()
    if (!nioProjectPath.exists()) {
      Log.i("[intellij-aya] Project path $projectPath is absent")
      return null
    }

    // This is possible when:
    // * we pass an invalid project path to refreshProject or something
    // * intellij trying to refresh a project that is not linked
    //
    // maybe we can load this project by ProjectPath.resolve
    if (!AyaOpenProjectProvider.isProjectFilePath(nioProjectPath)) {
      Log.i("Invalid project path: $projectPath")
      return null
    }

    val linkedProjectConfigPath = settings.linkedExternalProjectPath
    val linkedProjectDir = linkedProjectConfigPath.parent
    // I am not sure if they are equal, so we need some testing
    if (nioProjectPath != linkedProjectConfigPath) {
      throw IllegalStateException("projectPath=$nioProjectPath but linkedProjectPath=$linkedProjectConfigPath")
    }

    val projectFileDir = resolveProjectFileDir(settings)
    val projectNode = makeProjectNode(projectFileDir, linkedProjectDir)

    // i guess it is still possible that isPreviewMode=false when lspEnable=false
    // i.e. call from idea instead from us
    val lspEnable = AyaSettingService.getInstance().lspEnable()

    if ((!isPreviewMode) && lspEnable) {
      val job = ProjectCoroutineScope.getCoroutineScope(settings.project).async {
        tryInitializeLsp(settings)
        val moduleResolver = AyaModuleResolver(projectNode, moduleType.id, projectFileDir.toString(), linkedProjectDir.toString())
        val success = doResolveModules(settings, moduleResolver)
        projectNode.takeIf { success }
      }

      val result = runBlocking { job.await() }
      if (result != null) return result
      // failed, use preview project info
    }

    // now: isPreviewMode or ! success

    createPreviewProjectInfo(projectNode, projectFileDir, linkedProjectDir)
    return projectNode
  }

  override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
    return false    // Unsupported for now!!
  }
}
