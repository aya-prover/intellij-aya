package org.aya.intellij.proof;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import org.aya.intellij.AyaIcons;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.ref.DefVar;
import org.jetbrains.annotations.NotNull;

public interface ProofSearch {
  sealed interface Proof {
    record Err(@NotNull String message) implements Proof {}
    record Yes(@NotNull AyaPsiElement element, @NotNull DefVar<?, ?> defVar) implements Proof {}

    default @NotNull ItemPresentation presentation() {
      return switch (this) {
        case Err err -> new PresentationData(err.message, null, AyaIcons.PROOF_SEARCH_ERROR, null);
        case Yes yes -> yes.element.ayaPresentation(true);
      };
    }
  }
}
