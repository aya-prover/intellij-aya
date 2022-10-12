package org.aya.intellij.actions.lsp;

import kala.collection.Seq;
import org.javacs.lsp.MarkedString;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LspUtils {
  private LspUtils() {}

  public static @NotNull @Nls String renderMarkedStrings(List<MarkedString> strs) {
    return Seq.from(strs)
      .foldLeft(new StringBuilder(),
        (left, right) -> left.append(right.value))
      .toString();
  }
}
