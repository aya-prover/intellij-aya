package org.aya.intellij.ui.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.aya.intellij.psi.AyaPsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructureViewFactory implements PsiStructureViewFactory {
  @Override public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
    if (!(psiFile instanceof AyaPsiFile file)) return null;
    return new TreeBasedStructureViewBuilder() {
      @Override
      public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new AyaStructureViewModel(file, editor);
      }
    };
  }
}
