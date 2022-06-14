package org.aya.intellij.actions;

import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.aya.intellij.lsp.AyaStartup;
import org.aya.intellij.psi.AyaPsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LspHighlight extends RainbowVisitor {
  private static final HighlightInfoType SEMANTIC_TYPE = new HighlightInfoType.HighlightInfoTypeImpl(
    HighlightSeverity.TEXT_ATTRIBUTES,
    SyntaxHighlight.LSP);

  @Override public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof AyaPsiFile;
  }

  @Override public void visit(@NotNull PsiElement element) {
    if (!(element instanceof LeafPsiElement psi)) return;
    var file = psi.getContainingFile();
    var project = file.getProject();
    var lsp = AyaStartup.of(project);
    if (lsp == null) return;
    var range = element.getTextRange();
    var kind = lsp.highlight(file, range);
    render(element, SyntaxHighlight.choose(kind));
  }

  @Override public @NotNull HighlightVisitor clone() {
    return new LspHighlight();
  }

  private void render(@NotNull PsiElement element, @Nullable TextAttributesKey color) {
    if (color == null) return;
    addInfo(HighlightInfo.newHighlightInfo(SEMANTIC_TYPE)
      .textAttributes(color)
      .range(element)
      .create());
  }
}
