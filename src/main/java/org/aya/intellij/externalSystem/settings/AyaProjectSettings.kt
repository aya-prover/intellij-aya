package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile

/**
 * aya project-level settings
 * > This class will be constructed by reflection
 */
class AyaProjectSettings() : ExternalProjectSettings() {
  companion object {
    /**
     * @param ayaJson the virtual file to `aya.json`
     */
    fun createLinkSettings(ayaJson: VirtualFile, project: Project): AyaProjectSettings {
      return AyaProjectSettings(ayaJson.toNioPath().toAbsolutePath().toCanonicalPath())
    }
  }

  /**
   * @param externalProjectPath the path to aya project config file (aya.json), **NOT THE DIRECTORY**
   */
  constructor(externalProjectPath: String) : this() {
    this.externalProjectPath = externalProjectPath
  }

  override fun clone(): ExternalProjectSettings {
    return AyaProjectSettings(externalProjectPath)
  }
}
