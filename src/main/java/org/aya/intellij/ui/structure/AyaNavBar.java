package org.aya.intellij.ui.structure;

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.lang.Language;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.parser.AyaLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AyaNavBar extends StructureAwareNavBarModelExtension {
  @Override protected @NotNull Language getLanguage() {
    return AyaLanguage.INSTANCE;
  }

  @Override public @Nullable String getPresentableText(Object object) {
    return object instanceof AyaPsiElement e ? e.ayaPresentation(false).getPresentableText() : null;
  }

  @Override public @Nullable Icon getIcon(Object object) {
    return object instanceof AyaPsiElement e ? e.ayaPresentation(false).getIcon(false) : null;
  }
}
