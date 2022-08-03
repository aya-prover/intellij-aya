package org.aya.intellij.service;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.xml.util.XmlStringUtil;
import org.aya.core.term.Term;
import org.aya.util.distill.DistillerOptions;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

// TODO: user-defined distiller options
public class DistillerService {
  private static @NotNull DistillerOptions goalSolution() {
    return DistillerOptions.pretty();
  }

  private static @NotNull DistillerOptions showingError() {
    return DistillerOptions.pretty();
  }

  public static @NotNull String solution(@NotNull Term solution) {
    return solution.toDoc(goalSolution()).debugRender();
  }

  /**
   * For annotators, highlight visitors, highlight passes which supports HTML escaped tooltip.
   * Usually used as the argument of the following method:
   *
   * @see com.intellij.lang.annotation.AnnotationBuilder#tooltip(String)
   * @see com.intellij.lang.annotation.AnnotationHolder#newAnnotation(HighlightSeverity, String)
   * @see com.intellij.codeInsight.daemon.impl.HighlightInfo.Builder#escapedToolTip(String)
   */
  public static @NotNull String escapedBrief(@NotNull Problem problem) {
    return escaped(plainBrief(problem));
  }

  /**
   * For inspections which only supports plain text tooltip.
   * Used as the argument of the following method:
   *
   * @see ProblemDescriptor#getDescriptionTemplate()
   */
  public static @NotNull String plainBrief(@NotNull Problem problem) {
    return problem.brief(showingError()).debugRender();
  }

  /** @see #escapedBrief(Problem) */
  public static @NotNull String escapedDescribe(@NotNull Problem problem) {
    return escaped(plainDescribe(problem));
  }

  /** @see #plainBrief(Problem) */
  public static @NotNull String plainDescribe(@NotNull Problem problem) {
    return problem.describe(showingError()).debugRender();
  }

  private static @NotNull String escaped(@NotNull String text) {
    // TODO: make DocHtmlPrinter more customizable:
    //  - Whether warp the content in a <pre> block
    //  - Whether use nbsp for space
    return XmlStringUtil.escapeString(text)
      .replace("\n", "<br>")
      .replace(" ", "&nbsp;");
  }
}
