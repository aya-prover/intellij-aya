package org.aya.intellij.externalSystem

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import org.aya.intellij.externalSystem.project.AyaOpenProjectProvider

// copied from gradle plugin
// https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html
@Service(Service.Level.PROJECT)
class ProjectCoroutineScope(val coroutineScope: CoroutineScope) {
  companion object {
    fun getCoroutineScope(project: Project): CoroutineScope {
      return project.service<ProjectCoroutineScope>().coroutineScope
    }
  }
}

/**
 * @return whether the directory/file can be treated as an aya project
 * @see AyaOpenProjectProvider
 */
fun canOpenAyaProject(file: VirtualFile): Boolean {
  return AyaOpenProjectProvider().canOpenProject(file)
}
