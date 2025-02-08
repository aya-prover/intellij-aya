package org.aya.intellij.notification

import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import kotlinx.coroutines.launch
import org.aya.intellij.AyaBundle
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.intellij.externalSystem.ProjectCoroutineScope
import org.aya.intellij.language.isAya
import org.aya.intellij.service.AyaSettingService
import java.util.function.Function
import javax.swing.*

class InLibraryChecker : EditorNotificationProvider {
  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
    if (!isAya(file)) return CONST_NULL
    if (!AyaSettingService.getInstance().lspEnable()) return CONST_NULL
    if (ProjectFileIndex.getInstance(project).isInSource(file)) return CONST_NULL
    // don't report if AyaLsp is not active
    val isInLibrary = AyaLsp.useUnchecked(project, { true }) { it.isWatched(file) }
    if (!isInLibrary) return CONST_NULL

    return Function { editor ->
      EditorNotificationPanel(editor, EditorNotificationPanel.Status.Error)
        .text(AyaBundle.message("aya.notification.lsp.untracked"))
        .createActionLabel("Add to lsp as single file library") {
          addSingleFileToLsp(project, file)
        }
    }
  }

  // Runs on EDT
  private fun addSingleFileToLsp(project: Project, file: VirtualFile) {
    val coroutineScope = ProjectCoroutineScope.getCoroutineScope(project)
    coroutineScope.launch {
      readAction {
        if (!file.isValid) return@readAction
        AyaLsp.useUnchecked(project) { lsp ->
          lsp.registerLibrary(file)
        }
      }
    }
  }
}

private val CONST_NULL: Function<in FileEditor, out JComponent?> = Function { null }
