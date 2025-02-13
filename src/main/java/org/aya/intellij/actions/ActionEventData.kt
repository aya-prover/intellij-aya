package org.aya.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile

object ActionEventData {
  val AnActionEvent.virtualFile: VirtualFile? get() = getData(CommonDataKeys.VIRTUAL_FILE)
  val AnActionEvent.virtualFileArray: Array<VirtualFile>? get() = getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
}
