package org.aya.intellij.externalSystem.settings

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.util.messages.Topic

interface AyaSettingsListener : ExternalSystemSettingsListener<AyaProjectSettings> {
  companion object {
    val TOPIC: Topic<AyaSettingsListener> = Topic(AyaSettingsListener::class.java, Topic.BroadcastDirection.NONE)
  }

  class Delegate(val delegate: ExternalSystemSettingsListener<AyaProjectSettings>) :
    ExternalSystemSettingsListener<AyaProjectSettings> by delegate,
    AyaSettingsListener {
  }
}
