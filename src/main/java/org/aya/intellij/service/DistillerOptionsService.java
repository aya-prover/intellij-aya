package org.aya.intellij.service;

import org.aya.util.distill.DistillerOptions;
import org.jetbrains.annotations.NotNull;

public class DistillerOptionsService {
  public static @NotNull DistillerOptions goalSolution() {
    // TODO: user-defined distiller options
    return DistillerOptions.pretty();
  }

  public static @NotNull DistillerOptions showingError() {
    // TODO: user-defined distiller options
    return DistillerOptions.pretty();
  }
}
