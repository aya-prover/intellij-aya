package com.github.imkiva.intellijaya.services

import com.intellij.openapi.project.Project
import com.github.imkiva.intellijaya.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
