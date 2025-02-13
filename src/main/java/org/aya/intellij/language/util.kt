package org.aya.intellij.language

import com.intellij.openapi.vfs.VirtualFile

fun isAya(file: VirtualFile): Boolean = file.fileType is AyaFileType
