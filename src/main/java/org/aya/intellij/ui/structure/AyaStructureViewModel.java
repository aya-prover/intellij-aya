package org.aya.intellij.ui.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.util.PsiTreeUtil;
import kala.collection.Seq;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiStructureElement;
import org.aya.intellij.psi.concrete.AyaPsiClassMember;
import org.aya.intellij.psi.concrete.AyaPsiDataBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
  public AyaStructureViewModel(@NotNull AyaPsiFile file, @Nullable Editor editor) {
    super(file, editor, new Element(file));
    withSuitableClasses(AyaPsiStructureElement.class);
  }

  @Override public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return element.getValue() instanceof AyaPsiFile;
  }

  @Override public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    var value = element.getValue();
    return value instanceof AyaPsiDataBody || value instanceof AyaPsiClassMember;
  }

  private record Element(@NotNull AyaPsiElement element) implements StructureViewTreeElement {
    @Override public AyaPsiElement getValue() {
      return element;
    }

    @Override public @NotNull ItemPresentation getPresentation() {
      return element.ayaPresentation(false);
    }

    @Override public Element @NotNull [] getChildren() {
      return Seq.wrapJava(PsiTreeUtil.getChildrenOfTypeAsList(element, AyaPsiStructureElement.class))
        .map(Element::new)
        .toArray(Element[]::new);
    }

    @Override public void navigate(boolean requestFocus) {
      element.navigate(requestFocus);
    }

    @Override public boolean canNavigate() {
      return element.canNavigate();
    }

    @Override public boolean canNavigateToSource() {
      return element.canNavigateToSource();
    }
  }
}
