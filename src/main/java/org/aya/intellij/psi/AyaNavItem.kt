package org.aya.intellij.psi

import com.intellij.navigation.NavigationItem
import com.intellij.pom.Navigatable

/**
 * Provide flexible (the verbose flag) presentation control over psi elements.
 * And fix the icon not showing bug when directly overriding [NavigationItem.getPresentation] in [AyaPsiElement]
 */
class AyaNavItem(private val psi: AyaPsiElement, private val verbose: Boolean) : Navigatable by psi, NavigationItem {
  override fun getName() = psi.presentableName()
  override fun getPresentation() = psi.ayaPresentation(verbose)
}
