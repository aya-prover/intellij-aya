package org.aya.intellij.ui.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.pom.Navigatable;
import com.intellij.psi.util.PsiTreeUtil;
import kala.collection.Seq;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.psi.concrete.AyaPsiDataBody;
import org.aya.intellij.psi.concrete.AyaPsiStructField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
  public AyaStructureViewModel(@NotNull AyaPsiFile file, @Nullable Editor editor) {
    super(file, editor, new Element(file));
    withSuitableClasses(AyaPsiNamedElement.class);
  }

  @Override public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return element.getValue() instanceof AyaPsiFile;
  }

  @Override public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    var value = element.getValue();
    return value instanceof AyaPsiDataBody || value instanceof AyaPsiStructField;
  }

  private record Element(@NotNull AyaPsiElement element) implements StructureViewTreeElement {
    @Override public AyaPsiElement getValue() {
      return element;
    }

    @Override public @NotNull ItemPresentation getPresentation() {
      return element.ayaPresentation(false);
    }

    @Override public Element @NotNull [] getChildren() {
      return Seq.wrapJava(PsiTreeUtil.getChildrenOfTypeAsList(element, AyaPsiNamedElement.class))
        .map(Element::new)
        .toArray(Element[]::new);
    }

    @Override public void navigate(boolean requestFocus) {
      if (element instanceof Navigatable e) e.navigate(requestFocus);
    }

    @Override public boolean canNavigate() {
      return element instanceof Navigatable e && e.canNavigate();
    }

    @Override public boolean canNavigateToSource() {
      return element instanceof Navigatable e && e.canNavigateToSource();
    }
  }
}
