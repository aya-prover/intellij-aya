package org.aya.intellij.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import org.aya.intellij.AyaBundle
import org.aya.intellij.language.isAya
import org.aya.intellij.service.AyaSettingService
import java.util.function.Function
import javax.swing.*

class InLibraryChecker : EditorNotificationProvider {
  companion object {
    val CONST_NULL: Function<in FileEditor, out JComponent?> = Function { null }
  }

  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
    if (!isAya(file)) return CONST_NULL
    if (AyaSettingService.getInstance().ayaLspState != AyaSettingService.AyaState.UseIntegration) return CONST_NULL
    if (ProjectFileIndex.getInstance(project).isInSource(file)) return CONST_NULL
    return Function { editor ->
      EditorNotificationPanel(editor, EditorNotificationPanel.Status.Error)
        .text(AyaBundle.message("aya.notification.lsp.untracked"))
    }
  }
}
