package org.aya.intellij.externalSystem.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import kala.collection.mutable.MutableMap
import org.aya.cli.library.source.LibraryOwner
import org.aya.intellij.AyaConstants
import org.jetbrains.annotations.Contract
import kotlin.io.path.name

/**
 * The dependency graph below
 *
 * ```
 * A ---> B ---> C
 *        |      |
 *        v      v
 *        D ---> E
 * ```
 *
 * will be resolved to
 *
 * ```
 * + A
 *   + A.B
 *     + A.B.C
 *       + A.B.C.E
 *     + A.B.D
 * ```
 */
class AyaModuleResolver(val rootNode: DataNode<ProjectData>, val moduleTypeId: String, val moduleFileDirectoryPath: String, val externalConfigPath: String) {
  private val resolved: MutableMap<LibraryOwner, DataNode<ModuleData>> = MutableMap.create()

  @Contract(mutates = "this,param1")
  fun resolve(parent: DataNode<ModuleData>?, library: LibraryOwner): DataNode<ModuleData> {
    val resolvedLib = resolved.getOrNull(library)
    if (resolvedLib != null) return resolvedLib

    val config = library.underlyingLibrary()
    val libraryDir = config.libraryRoot.toAbsolutePath()
    val libraryDirName = libraryDir.name
    val libraryName = config.name
    val prefix = if (parent != null) "${parent.data.externalName}." else ""
    val externalName = prefix + libraryName

    val moduleData = ModuleData(libraryDirName, AyaConstants.SYSTEM_ID, moduleTypeId,
      externalName, moduleFileDirectoryPath, externalConfigPath)
    // Create modules on rootNode
    val thisNode = rootNode.createChild(ProjectKeys.MODULE, moduleData).apply {
      val contentRoot = ContentRootData(AyaConstants.SYSTEM_ID, libraryDir.toString()).apply {
        storePath(ExternalSystemSourceType.SOURCE, libraryDir.resolve(AyaConstants.SOURCE_DIR_NAME).toString())
        storePath(ExternalSystemSourceType.EXCLUDED, libraryDir.resolve(AyaConstants.BUILD_DIR_NAME).toString())
      }

      createChild(ProjectKeys.CONTENT_ROOT, contentRoot)
    }

    resolved.put(library, thisNode)

    library.libraryDeps().forEach { dep ->
      val depNode = resolve(thisNode, dep)
      thisNode.createChild(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(thisNode.data, depNode.data))
    }

    return thisNode
  }
}
