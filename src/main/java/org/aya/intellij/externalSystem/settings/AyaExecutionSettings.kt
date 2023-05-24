package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaConstants
import java.nio.file.Path

/**
 * @param projectFileDir the path to the directory which stores the project file (such as *.iml or *.ipr), null when creating/importing a project
 */
class AyaExecutionSettings(
  var project: Project,
  var projectFileDir: Path?,
  var linkedProjectPath: Path
) : ExternalSystemExecutionSettings() {
  val buildFilePath: Path get() = linkedProjectPath.resolve(AyaConstants.BUILD_FILE_NAME)
}
