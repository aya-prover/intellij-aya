package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import java.util.*

/**
 * idea project-level settings
 */
@State(name = "AyaSettings", storages = [Storage("aya.xml")])
class AyaSettings(project: Project) :
  PersistentStateComponent<AyaSettings.MyState>,
  AbstractExternalSystemSettings<AyaSettings, AyaProjectSettings, AyaSettingsListener>(
    AyaSettingsListener.TOPIC,
    project,
  ) {
  companion object {
    fun getInstance(project: Project): AyaSettings {
      return project.getService(AyaSettings::class.java)
    }
  }

  class MyState : State<AyaSettings> {
    private val linkedProjectSettings : MutableSet<AyaSettings> = TreeSet()

    override fun getLinkedExternalProjectsSettings(): MutableSet<AyaSettings> {
      return linkedProjectSettings
    }

    override fun setLinkedExternalProjectsSettings(settings: MutableSet<AyaSettings>?) {
      if (settings != null) {
        linkedProjectSettings.addAll(settings)
      }
    }
  }

  override fun subscribe(listener: ExternalSystemSettingsListener<AyaProjectSettings>) {
    doSubscribe(AyaSettingsListener.Delegate(listener), this)
  }

  override fun copyExtraSettingsFrom(settings: AyaSettings) {
  }

  override fun checkSettings(old: AyaProjectSettings, current: AyaProjectSettings) {
  }

  override fun getState(): MyState {
    return MyState()
  }

  override fun loadState(state: MyState) {
  }
}
