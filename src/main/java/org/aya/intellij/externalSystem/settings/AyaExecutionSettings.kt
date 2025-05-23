package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaConstants
import java.nio.file.Path

/**
 * [AyaExecutionSettings] is used for... execution, see [org.aya.intellij.externalSystem.project.AyaProjectResolver]
 *
 * All [Path]s are assumed to be absolute
 * @param projectFileDir the path to the directory which stores the project file (such as *.iml or *.ipr), null when creating/importing a project
 * @param linkedExternalProjectPath the path to the external project, the file may not exist.
 */
class AyaExecutionSettings(
  val project: Project,
  val projectFileDir: Path?,
  val linkedExternalProjectPath: Path,
) : ExternalSystemExecutionSettings() {
  val buildFilePath: Path get() = linkedExternalProjectPath.resolve(AyaConstants.BUILD_FILE_NAME)
}
