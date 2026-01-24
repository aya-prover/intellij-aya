package org.aya.intellij.externalSystem

import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.project.Project
import org.aya.cli.library.source.LibraryOwner
import org.aya.generic.Constants
import org.aya.intellij.actions.lsp.AyaLsp
import org.aya.lsp.models.ProjectPath
import org.aya.lsp.utils.Log
import java.io.File
import kotlin.io.path.Path

class AyaExternalSystemAutoImportAware : ExternalSystemAutoImportAware {
  override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
    val path = ProjectPath.resolve(Path(changedFileOrDirPath)) ?: return null
    if (path !is ProjectPath.Project) return null

    // only read, and the data is not very important, so some thread issues is acceptable
    // as this method is call rather often
    // ^ or may not, we use CachingExternalSystemAutoImportAware, so it only be slow on first call
    val owner = AyaLsp.useUnchecked(project, { null }) { lsp ->
      lsp.findRootLibrary(path)
    } ?: return null

    return owner
      .getFirstOption()
      .map { it.underlyingLibrary().libraryRoot().resolve(Constants.AYA_JSON).toString() }
      .orNull
  }

  override fun getAffectedExternalProjectFiles(projectPath: String?, project: Project): List<File> {
    if (projectPath == null) return emptyList()
    val path = ProjectPath.resolve(Path(projectPath))
    if (path !is ProjectPath.Project) return emptyList()

    val configFiles = AyaLsp.useUnchecked(project, { emptyList() }) { lsp ->
      val root = lsp.getLoadedLibrary(path) ?: return@useUnchecked emptyList<File>()
      val deps = LibraryOwner.collectDependencies(root)
      deps.map { it.underlyingLibrary().libraryRoot.resolve(Constants.AYA_JSON).toFile() }.toList()
    }

    Log.i("Found affected files for $projectPath: $configFiles")
    return configFiles
  }
}
