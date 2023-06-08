package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile

/**
 * aya project-level settings
 * > This class can be constructed by reflection
 */
class AyaProjectSettings() : ExternalProjectSettings() {
  companion object {
    fun createLinkSettings(projectDir: VirtualFile, project: Project): AyaProjectSettings? {
      return AyaProjectSettings(projectDir.toNioPath().toAbsolutePath().toCanonicalPath())
    }
  }

  constructor(externalProjectPath: String) : this() {
    this.externalProjectPath = externalProjectPath
  }

  override fun clone(): ExternalProjectSettings {
    return AyaProjectSettings(externalProjectPath)
  }
}
