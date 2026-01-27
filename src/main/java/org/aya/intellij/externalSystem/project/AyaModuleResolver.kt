package org.aya.intellij.externalSystem.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import kala.collection.mutable.MutableMap
import org.aya.cli.library.source.LibraryOwner
import org.aya.intellij.AyaConstants
import org.jetbrains.annotations.Contract
import java.nio.file.Path
import kotlin.io.path.name

class AyaModuleResolver(
  val rootNode: DataNode<ProjectData>,
  val moduleTypeId: String,
  val moduleFileDirectoryPath: String,
  // TODO: externalProjectPath is a directory, maybe change to the path to `aya.json`?
  val externalProjectPath: String,
) {
  private val resolved: MutableMap<Path, DataNode<ModuleData>> = MutableMap.create()

  @Contract(mutates = "this")
  fun resolve(library: LibraryOwner): DataNode<ModuleData> {
    val config = library.underlyingLibrary()
    val libraryDir = config.libraryRoot.toAbsolutePath()
    val resolvedLib = resolved.getOrNull(libraryDir)
    if (resolvedLib != null) return resolvedLib

    val libraryDirName = libraryDir.name
    val libraryName = config.name
    // TODO: deal with name conflict
    val externalName = libraryName

    val moduleData = ModuleData(libraryDirName, AyaConstants.SYSTEM_ID, moduleTypeId,
      externalName, moduleFileDirectoryPath, externalProjectPath)
    // Create modules on rootNode
    val thisNode = rootNode.createChild(ProjectKeys.MODULE, moduleData).apply {
      val contentRoot = ContentRootData(AyaConstants.SYSTEM_ID, libraryDir.toString()).apply {
        storePath(ExternalSystemSourceType.SOURCE, config.librarySrcRoot.toAbsolutePath().toString())
        storePath(ExternalSystemSourceType.EXCLUDED, config.libraryBuildRoot.toAbsolutePath().toString())
      }

      createChild(ProjectKeys.CONTENT_ROOT, contentRoot)
    }

    resolved.put(libraryDir, thisNode)

    library.libraryDeps().forEach { dep ->
      val depNode = resolve(dep)
      thisNode.createChild(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(thisNode.data, depNode.data))
    }

    return thisNode
  }
}
