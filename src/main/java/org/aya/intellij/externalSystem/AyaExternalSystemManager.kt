package org.aya.intellij.externalSystem

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.util.Function
import org.aya.intellij.AyaConstants
import org.aya.intellij.externalSystem.project.AyaProjectResolver
import org.aya.intellij.externalSystem.settings.*
import org.aya.intellij.externalSystem.task.AyaTaskManager
import org.aya.lsp.utils.Log
import java.io.File
import java.nio.file.Path

/**
 * ## Terms
 *
 * * external project: a project of external system.
 * * linked project: short for "linked external project" (linked- means imported to intellij)
 * * project file: the file that stores all information of an idea project (such as *.iml or *.ipr)
 * * project file directory: the directory that stores project file, it is usually ".idea"
 */
class AyaExternalSystemManager : ExternalSystemManager<
  AyaProjectSettings,
  AyaSettingsListener,
  AyaSettings,
  AyaLocalSettings,
  AyaExecutionSettings,
  >,
  ExternalSystemAutoImportAware {
  private val autoImportAware = CachingExternalSystemAutoImportAware(AyaExternalSystemAutoImportAware())

  override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
    throw UnsupportedOperationException()
  }

  override fun getSystemId(): ProjectSystemId = AyaConstants.SYSTEM_ID

  override fun getSettingsProvider(): Function<Project, AyaSettings> = Function {
    AyaSettings.getInstance(it)
  }

  override fun getLocalSettingsProvider(): Function<Project, AyaLocalSettings> = Function {
    AyaLocalSettings.getInstance(it)
  }

  override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, AyaExecutionSettings> = Function { pair ->
    val project = pair.first
    val linkedProjectPath = Path.of(pair.second)
    val projectFileDir = project.projectFile
      ?.parent
      ?.toNioPath()
      ?.toAbsolutePath()

    AyaExecutionSettings(project, projectFileDir, linkedProjectPath.toAbsolutePath())
  }

  override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<AyaExecutionSettings>> {
    return AyaProjectResolver::class.java
  }

  override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<AyaExecutionSettings>> {
    return AyaTaskManager::class.java
  }

  // https://plugins.jetbrains.com/docs/intellij/external-system-integration.html#auto-import
  // TODO: Even though idea listens (really?) to the `aya.json` that create the project (i.e. the one you used to open the project),
  //       we still need to handle other `aya.json` that is used by the root project
  // This method find the root config file (bottom to top)
  override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
    Log.i("Affect path: %s", changedFileOrDirPath)
    return autoImportAware.getAffectedExternalProjectPath(changedFileOrDirPath, project)
  }

  // This method find all config file that affect [projectPath] (top to bottom)
  override fun getAffectedExternalProjectFiles(projectPath: String?, project: Project): List<File>? {
    Log.i("Affect files: %s", projectPath)
    return autoImportAware.getAffectedExternalProjectFiles(projectPath, project)
  }

  /**
   * Used for selecting aya project file (aya.json) in order to linking a project as an aya project
   */
  override fun getExternalProjectDescriptor(): FileChooserDescriptor {
    return FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
  }
}
