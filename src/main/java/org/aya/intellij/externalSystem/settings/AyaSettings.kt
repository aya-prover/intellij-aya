package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import org.aya.intellij.AyaConstants
import java.util.*

/**
 * idea project-level settings
 *
 * We cannot use [AyaSettings] as the state of [PersistentStateComponent]
 * due to [AbstractExternalSystemSettings] introduces something very heavy and cannot be serialized.
 */
@Service(Service.Level.PROJECT)
@State(name = "AyaSettings", storages = [Storage(AyaConstants.AYA_ES_SETTINGS)])
class AyaSettings(project: Project) :
  PersistentStateComponent<AyaSettings.MyState>,
  AbstractExternalSystemSettings<AyaSettings, AyaProjectSettings, AyaSettingsListener>(
    AyaSettingsListener.TOPIC,
    project,
  ) {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): AyaSettings {
      return project.getService(AyaSettings::class.java)
    }
  }

  class MyState : State<AyaProjectSettings> {
    private val linkedProjectSettings : MutableSet<AyaProjectSettings> = TreeSet()

    override fun getLinkedExternalProjectsSettings(): MutableSet<AyaProjectSettings> {
      return linkedProjectSettings
    }

    override fun setLinkedExternalProjectsSettings(settings: MutableSet<AyaProjectSettings>?) {
      if (settings != null) {
        linkedProjectSettings.addAll(settings)
      }
    }
  }

  override fun subscribe(listener: ExternalSystemSettingsListener<AyaProjectSettings>, parentDisposable: Disposable) {
    doSubscribe(AyaSettingsListener.Delegate(listener), this)
  }

  override fun copyExtraSettingsFrom(settings: AyaSettings) {
  }

  override fun checkSettings(old: AyaProjectSettings, current: AyaProjectSettings) {
  }

  override fun getState(): MyState {
    return MyState().apply(::fillState)
  }

  override fun loadState(state: MyState) {
    super.loadState(state)
  }
}
