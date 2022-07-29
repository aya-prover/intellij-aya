package org.aya.intellij

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

object AyaBundle {
  @NonNls private const val BUNDLE = "aya.aya-bundle"
  private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
    AbstractBundle.message(bundle, key, *params)
}
