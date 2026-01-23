package org.aya.intellij.externalSystem

import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.project.Project
import org.aya.generic.Constants
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.lsp.models.ProjectPath
import kotlin.io.path.Path

class AyaExternalSystemAutoImportAware : ExternalSystemAutoImportAware {
  override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
    val path = ProjectPath.resolve(Path(changedFileOrDirPath)) ?: return null
    if (path !is ProjectPath.Project) return null

    // only read, and the data is not very important, so some thread issues is acceptable
    // as this method is call rather often
    // ^ or may not, we use CachingExternalSystemAutoImportAware, so it only be slow on first call
    val owner = AyaLsp.useUnchecked(project, { null }) { lsp ->
      lsp.getLoadedLibrary(path)
    } ?: return null

    return owner.underlyingLibrary().libraryRoot.resolve(Constants.AYA_JSON).toString()
  }
}
