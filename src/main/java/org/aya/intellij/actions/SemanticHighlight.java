package org.aya.intellij.actions;

import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiGenericDecl;
import org.aya.intellij.psi.concrete.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SemanticHighlight extends RainbowVisitor {
  private static final HighlightInfoType SEMANTIC_TYPE = new HighlightInfoType.HighlightInfoTypeImpl(
    HighlightSeverity.TEXT_ATTRIBUTES,
    SyntaxHighlight.SEMANTICS);

  @Override public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof AyaPsiFile;
  }

  @Override public void visit(@NotNull PsiElement element) {
    switch (element) {
      case AyaPsiDeclNameOrInfix name -> {
        var id = name.getWeakId();
        switch (PsiTreeUtil.getParentOfType(name, AyaPsiGenericDecl.class)) {
          case AyaPsiFnDecl $ -> render(id, SyntaxHighlight.FN_DEF);
          case AyaPsiDataDecl $ -> render(id, SyntaxHighlight.DATA_DEF);
          case AyaPsiStructDecl $ -> render(id, SyntaxHighlight.STRUCT_DEF);
          case AyaPsiDataBody $ -> render(id, SyntaxHighlight.CON_DEF);
          case AyaPsiStructField $ -> render(id, SyntaxHighlight.FIELD_DEF);
          // note: no PrimDecl here because it does not use declNameOrInfix.
          case default, null -> {}
        }
      }
      case AyaPsiPrimName $ -> render(element, SyntaxHighlight.PRIM_DEF);
      case AyaPsiProjFixId $ -> render(element, SyntaxHighlight.FIELD_CALL);
      case AyaPsiNewArgField $ -> render(element, SyntaxHighlight.FIELD_CALL);
      case AyaPsiGeneralizeParamName $ -> render(element, SyntaxHighlight.GENERALIZE);
      default -> {
        // If it is a reference, highlight it by its definition kind.
        if (ReferenceContributor.REFERRING_TERMS.accepts(element)) {
          var def = element.getReferences();
          // this will call AyaPsiReference.resolve()
          if (def.length != 0) switch (def[0].resolve()) {
            case AyaPsiFnDecl $ -> render(element, SyntaxHighlight.FN_CALL);
            case AyaPsiPrimDecl $ -> render(element, SyntaxHighlight.PRIM_CALL);
            case AyaPsiDataDecl $ -> render(element, SyntaxHighlight.DATA_CALL);
            case AyaPsiStructDecl $ -> render(element, SyntaxHighlight.STRUCT_CALL);
            case AyaPsiDataBody $ -> render(element, SyntaxHighlight.CON_CALL);
            // note: no StructField here because it can be highlighted without knowing the definition.
            case default, null -> {}
          }
        }
      }
    }
  }

  @Override public @NotNull HighlightVisitor clone() {
    return new SemanticHighlight();
  }

  private void render(@NotNull PsiElement element, @Nullable TextAttributesKey color) {
    if (color == null) return;
    addInfo(HighlightInfo.newHighlightInfo(SEMANTIC_TYPE)
      .textAttributes(color)
      .range(element)
      .create());
  }
}
