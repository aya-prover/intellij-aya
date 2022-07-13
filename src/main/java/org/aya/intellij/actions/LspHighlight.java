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
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.lsp.models.HighlightResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LspHighlight extends RainbowVisitor {
  private static final HighlightInfoType SEMANTIC_TYPE = new HighlightInfoType.HighlightInfoTypeImpl(
    HighlightSeverity.TEXT_ATTRIBUTES,
    SyntaxHighlight.LSP,
    true);

  @Override public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof AyaPsiFile;
  }

  @Override public void visit(@NotNull PsiElement element) {
    if (!(element instanceof LeafPsiElement psi)) return;
    var file = psi.getContainingFile();
    var project = file.getProject();
    var lsp = AyaLsp.of(project);
    if (lsp == null) return;
    var range = element.getTextRange();
    var kind = lsp.highlight(file, range);
    render(element, choose(kind));
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

  private static @Nullable TextAttributesKey choose(@Nullable HighlightResult.Kind kind) {
    return switch (kind) {
      case FnDef -> SyntaxHighlight.FN_DEF;
      case DataDef -> SyntaxHighlight.DATA_DEF;
      case StructDef -> SyntaxHighlight.STRUCT_DEF;
      case ConDef -> SyntaxHighlight.CON_DEF;
      case FieldDef -> SyntaxHighlight.FIELD_DEF;
      case PrimDef -> SyntaxHighlight.PRIM_DEF;
      case FnCall -> SyntaxHighlight.FN_CALL;
      case DataCall -> SyntaxHighlight.DATA_CALL;
      case StructCall -> SyntaxHighlight.STRUCT_CALL;
      case ConCall -> SyntaxHighlight.CON_CALL;
      case FieldCall -> SyntaxHighlight.FIELD_CALL;
      case PrimCall -> SyntaxHighlight.PRIM_CALL;
      case default, null -> null;
    };
  }
}
