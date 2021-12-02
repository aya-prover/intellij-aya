package org.aya.intellij.services

import com.intellij.openapi.project.Project
import org.aya.intellij.MyBundle

class MyProjectService(project: Project) {
  init {
    println(MyBundle.message("projectService", project.name))
  }
}
