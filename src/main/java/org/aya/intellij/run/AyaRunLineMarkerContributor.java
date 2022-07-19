package org.aya.intellij.run;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.psi.PsiElement;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.AyaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaRunLineMarkerContributor extends RunLineMarkerContributor {
  @Override public @Nullable Info getInfo(@NotNull PsiElement psi) {
    if (!AyaTyckRunConfig.isTyckUnit(psi)) return null;
    final var actions = ExecutorAction.getActions(Integer.MAX_VALUE);
    return new Info(
      AyaIcons.GUTTER_RUN,
      actions,
      element -> ImmutableSeq.of(actions)
        .mapNotNull(action -> getText(action, element))
        .joinToString("\n")
    );
  }
}
