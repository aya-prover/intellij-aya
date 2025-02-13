@file:JvmName("ModuleUtil")

package org.aya.intellij.util

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile

/**
 * Trying to find the corresponding [ContentEntry] (come from [ModifiableRootModel.getContentEntries]) for [file] in [project], also the [Module] which the [ContentEntry] belongs to.
 * @return null if no such [ContentEntry]
 */
fun findContentEntryForFile(file: VirtualFile, project: Project) = ReadAction.compute<Pair<Module, ContentEntry>?, Throwable> {
  findContentEntryForFileUnlocked(file, project)
}

fun findContentEntryForFileUnlocked(file: VirtualFile, project: Project): Pair<Module, ContentEntry>? {
  val module = ModuleUtil.findModuleForFile(file, project) ?: return null
  val rootManager = ModuleRootManager.getInstance(module)
  val entry = rootManager.modifiableModel.contentEntries.firstOrNull { entry ->
    val contentRoot = entry.file ?: return@firstOrNull false
    // TODO: upgrade to 2023.1 and then use VfsUtil.compareByPath
    contentRoot.path == file.path
  } ?: return null

  return module to entry
}
