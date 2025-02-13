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
  val externalProjectPath: String,
) {
  private val resolved: MutableMap<Path, DataNode<ModuleData>> = MutableMap.create()

  fun isInScope(dir: Path): Boolean {
    // TODO
    return true
  }

  @Contract(mutates = "this,param1")
  fun resolve(parent: DataNode<ModuleData>?, library: LibraryOwner): DataNode<ModuleData> {
    val config = library.underlyingLibrary()
    val libraryDir = config.libraryRoot.toAbsolutePath()
    val resolvedLib = resolved.getOrNull(libraryDir)
    if (resolvedLib != null) return resolvedLib

    val libraryDirName = libraryDir.name
    val libraryName = config.name
//    val prefix = if (parent != null) "${parent.data.externalName}." else ""
    // TODO: deal with name conflict
    val externalName = libraryName // prefix + libraryName

    val moduleData = ModuleData(libraryDirName, AyaConstants.SYSTEM_ID, moduleTypeId,
      externalName, moduleFileDirectoryPath, externalProjectPath)
    // Create modules on rootNode
    // ~~TODO: deal with out-of-scope modules~~
    // no we needn't, idea will solve that
    val thisNode = rootNode.createChild(ProjectKeys.MODULE, moduleData).apply {
      val contentRoot = ContentRootData(AyaConstants.SYSTEM_ID, libraryDir.toString()).apply {
        storePath(ExternalSystemSourceType.SOURCE, config.librarySrcRoot.toAbsolutePath().toString())
        storePath(ExternalSystemSourceType.EXCLUDED, config.libraryBuildRoot.toAbsolutePath().toString())
      }

      createChild(ProjectKeys.CONTENT_ROOT, contentRoot)
    }

    resolved.put(libraryDir, thisNode)

    library.libraryDeps().forEach { dep ->
      val depNode = resolve(thisNode, dep)
      thisNode.createChild(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(thisNode.data, depNode.data))
    }

    return thisNode
  }
}
