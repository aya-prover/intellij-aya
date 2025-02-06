package org.aya.intellij.externalSystem

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
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
import java.nio.file.Path

/**
 * ## Terms
 *
 * * external project: a project of external system.
 * * linked project: short for "linked external project"
 * * externalProjectPath/linkedProjectPath: the path to the directory of external system project
 */
class AyaExternalSystemManager : ExternalSystemManager<
  AyaProjectSettings,
  AyaSettingsListener,
  AyaSettings,
  AyaLocalSettings,
  AyaExecutionSettings,
  >,
  ExternalSystemAutoImportAware {
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
    val projectFileDir = project.projectFile?.toNioPath()?.toAbsolutePath()

    AyaExecutionSettings(project, projectFileDir, linkedProjectPath.toAbsolutePath())
  }

  override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<AyaExecutionSettings>> {
    return AyaProjectResolver::class.java
  }

  override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<AyaExecutionSettings>> {
    return AyaTaskManager::class.java
  }

  override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
    return null
  }

  override fun getExternalProjectDescriptor(): FileChooserDescriptor {
    return FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
  }
}
