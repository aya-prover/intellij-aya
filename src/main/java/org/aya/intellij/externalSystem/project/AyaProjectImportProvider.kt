package org.aya.intellij.externalSystem.project

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportProvider
import com.intellij.openapi.vfs.VirtualFile
import org.aya.intellij.AyaBundle.message
import org.aya.intellij.AyaConstants
import org.aya.intellij.externalSystem.canOpenAyaProject
import org.aya.intellij.service.AyaProjectImportBuilder

/**
 * This is used for manually linking aya project, i.e. the "+" button in aya tool window.
 */
class AyaProjectImportProvider : AbstractExternalProjectImportProvider(
  AyaProjectImportBuilder(),
  AyaConstants.SYSTEM_ID,
) {
  override fun getPathToBeImported(file: VirtualFile?): String {
    return getDefaultPath(file)
  }

  override fun canImportFromFile(file: VirtualFile?): Boolean {
    if (file == null) return false
    return canOpenAyaProject(file)
  }

  override fun createSteps(context: WizardContext?): Array<ModuleWizardStep> {
    return ModuleWizardStep.EMPTY_ARRAY
  }

  override fun getFileSample(): String {
    return message("aya.file.type.description", AyaConstants.BUILD_FILE_NAME)
  }
}
