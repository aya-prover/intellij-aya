package org.aya.intellij.actions;

import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.aya.intellij.lsp.AyaStartup;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiLeafElement;
import org.jetbrains.annotations.NotNull;

public class SemanticHighlight extends RainbowVisitor {
  private static final HighlightInfoType SEMANTIC_TYPE = new HighlightInfoType.HighlightInfoTypeImpl(
    HighlightSeverity.TEXT_ATTRIBUTES,
    SyntaxHighlight.SEMANTIC);

  @Override public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof AyaPsiFile;
  }

  @Override public void visit(@NotNull PsiElement element) {
    if (!(element instanceof AyaPsiLeafElement psi)) return;
    var project = psi.getContainingFile().getProject();
    var lsp = AyaStartup.of(project);
    if (lsp == null) return;
    // TODO: search HighlightResult in server
  }

  @Override public @NotNull HighlightVisitor clone() {
    return new SemanticHighlight();
  }

  private void render(@NotNull PsiElement element, @NotNull TextAttributesKey color) {
    addInfo(HighlightInfo.newHighlightInfo(SEMANTIC_TYPE)
      .textAttributes(color)
      .range(element)
      .create());
  }
}
