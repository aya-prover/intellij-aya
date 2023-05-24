package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaConstants

class AyaLocalSettings(project: Project) : AbstractExternalSystemLocalSettings<AyaLocalSettings.State>(AyaConstants.SYSTEM_ID, project, State()) {
  companion object {
    fun getInstance(project: Project): AyaLocalSettings {
      return project.getService(AyaLocalSettings::class.java)
    }
  }
  
  class State : AbstractExternalSystemLocalSettings.State() {
  }
}
