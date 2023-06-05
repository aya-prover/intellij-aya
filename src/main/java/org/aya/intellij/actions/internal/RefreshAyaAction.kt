package org.aya.intellij.actions.internal

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import org.aya.intellij.AyaConstants
import org.aya.intellij.actions.AyaSystemAction
import org.aya.intellij.externalSystem.settings.AyaSettings

class RefreshAllAyaAction : AyaSystemAction() {
  init {
    templatePresentation.text = "Refresh All Aya Project"
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project!!     // this is okay to internal action.
    val settings = AyaSettings.getInstance(project)

    settings.linkedProjectsSettings.forEach {
      ExternalSystemUtil.refreshProject(
        it.externalProjectPath!!,
        ImportSpecBuilder(project, AyaConstants.SYSTEM_ID)
      )
    }
  }
}
