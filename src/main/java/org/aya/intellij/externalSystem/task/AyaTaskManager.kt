package org.aya.intellij.externalSystem.task

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import org.aya.intellij.externalSystem.settings.AyaExecutionSettings

class AyaTaskManager : ExternalSystemTaskManager<AyaExecutionSettings> {
  override fun executeTasks(
    id: ExternalSystemTaskId,
    taskNames: MutableList<String>,
    projectPath: String,
    settings: AyaExecutionSettings?,
    jvmParametersSetup: String?,
    listener: ExternalSystemTaskNotificationListener,
  ) {
  }

  override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
    return false
  }
}
