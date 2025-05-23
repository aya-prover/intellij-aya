package org.aya.intellij.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.useLsp
import org.aya.intellij.externalSystem.ProjectCoroutineScope
import org.aya.intellij.language.isAya
import org.aya.intellij.service.AyaSettingService
import java.util.function.Function
import javax.swing.*

class InLibraryChecker : EditorNotificationProvider {
  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
    if (!isAya(file)) return null
    if (!AyaSettingService.getInstance().lspEnable()) return null
    if (ProjectFileIndex.getInstance(project).isInSource(file)) return null

    // don't report if AyaLsp is not active
    val isInLibrary = runBlocking {
      project.useLsp({ true }) { it.isWatched(file) }
    }
    if (isInLibrary) return null

    return Function { editor ->
      // [EditorNotificationPanel.Status.Warning] here is necessary, at least it cannot be [Error] which makes [createActionLabel]
      // works incorrectly.
      EditorNotificationPanel(editor, EditorNotificationPanel.Status.Warning).apply {
        text(AyaBundle.message("aya.notification.lsp.untracked"))
        createActionLabel(
          AyaBundle.message("aya.notification.lsp.untracked.fix"),
          {
            addSingleFileToLsp(project, file)
          },
          true,
        )
      }
    }
  }

  // Runs on EDT
  private fun addSingleFileToLsp(project: Project, file: VirtualFile) {
    val coroutineScope = ProjectCoroutineScope.getCoroutineScope(project)
    coroutineScope.launch {
      val success = project.useLsp({ false }) { lsp ->
        lsp.registerLibrary(file)
        true
      }

      if (success) {
        EditorNotifications.getInstance(project)
          .updateNotifications(file)
      }
    }
  }
}
