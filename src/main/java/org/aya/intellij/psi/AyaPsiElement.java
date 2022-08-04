package org.aya.intellij.psi;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.intellij.AyaIcons;
import org.aya.intellij.psi.concrete.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface AyaPsiElement extends NavigatablePsiElement {
  default @Override Icon getIcon(int flags) {
    return ayaIcon();
  }

  default @Nullable Icon ayaIcon() {
    return switch (this) {
      case AyaPsiFile $ -> AyaIcons.AYA_FILE;
      case AyaPsiDataDecl $ -> AyaIcons.AYA_DATA;
      case AyaPsiStructDecl $ -> AyaIcons.AYA_STRUCT;
      case AyaPsiPrimDecl $ -> AyaIcons.AYA_PRIM;
      case AyaPsiFnDecl $ -> AyaIcons.AYA_FN;
      case AyaPsiDataBody $ -> AyaIcons.AYA_CTOR;
      case AyaPsiStructField $ -> AyaIcons.AYA_FIELD;
      default -> null;
    };
  }

  default @NotNull ItemPresentation ayaPresentation(boolean verbose) {
    var location = verbose ? QualifiedID.join(containingModule()) : null;
    return new PresentationData(presentableName(), location, ayaIcon(), null);
  }

  default @NotNull String presentableName() {
    return switch (this) {
      case AyaPsiNamedElement named -> named.nameOrEmpty();
      case AyaPsiFile file -> QualifiedID.join(file.containingFileModule());
      default -> "";
    };
  }

  default @NotNull ImmutableSeq<String> containingFileModule() {
    var file = (AyaPsiFile) getContainingFile();
    return file.containingFileModule();
  }

  default @NotNull ImmutableSeq<String> containingSubModule() {
    var modulePsi = PsiTreeUtil.getParentOfType(this, AyaPsiModule.class);
    var subModule = modulePsi != null
      ? SeqView.of(modulePsi.getWeakId().getText())
      : SeqView.<String>empty();
    return subModule.toImmutableSeq();
  }

  default @NotNull ImmutableSeq<String> containingModule() {
    return containingFileModule().concat(containingSubModule());
  }
}
