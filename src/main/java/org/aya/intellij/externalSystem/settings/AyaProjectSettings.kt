package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * aya project-level settings
 */
class AyaProjectSettings(
  externalProjectPath: String
) : ExternalProjectSettings() {
  companion object {
    fun createLinkSettings(projectDir: VirtualFile, project: Project): AyaProjectSettings? {
      return AyaProjectSettings(projectDir.toNioPath().toAbsolutePath().toString())
    }
  }

  init {
    this.externalProjectPath = externalProjectPath
  }

  override fun clone(): ExternalProjectSettings {
    return AyaProjectSettings(externalProjectPath)
  }
}
