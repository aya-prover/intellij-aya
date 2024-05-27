package org.aya.intellij.actions;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.aya.intellij.psi.AyaPsiGenericDecl;
import org.aya.intellij.psi.concrete.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SemanticHighlight implements Annotator {
  @Override public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    switch (element) {
      case AyaPsiDeclNameOrInfix name -> {
        var id = name.getWeakId();
        switch (PsiTreeUtil.getParentOfType(name, AyaPsiGenericDecl.class)) {
          case AyaPsiFnDecl $ -> render(holder, id, SyntaxHighlight.FN_DEF);
          case AyaPsiDataDecl $ -> render(holder, id, SyntaxHighlight.DATA_DEF);
          case AyaPsiClassDecl $ -> render(holder, id, SyntaxHighlight.STRUCT_DEF);
          case AyaPsiDataBody $ -> render(holder, id, SyntaxHighlight.CON_DEF);
          case AyaPsiClassMember $ -> render(holder, id, SyntaxHighlight.FIELD_DEF);
          // note: no PrimDecl here because it does not use declNameOrInfix.
          case default, null -> {}
        }
      }
      case AyaPsiPrimName $ -> render(holder, element, SyntaxHighlight.PRIM_DEF);
      case AyaPsiProjFixId $ -> render(holder, element, SyntaxHighlight.FIELD_CALL);
      case AyaPsiNewArgField $ -> render(holder, element, SyntaxHighlight.FIELD_CALL);
      case AyaPsiGeneralizeParamName $ -> render(holder, element, SyntaxHighlight.GENERALIZE);
      default -> {
        // If it is a reference, highlight it by its definition kind.
        if (ReferenceContributor.REFERRING_TERMS.accepts(element)) {
          var def = element.getReferences();
          // this will call AyaPsiReference.resolve()
          if (def.length != 0) switch (def[0].resolve()) {
            case AyaPsiFnDecl $ -> render(holder, element, SyntaxHighlight.FN_CALL);
            case AyaPsiPrimDecl $ -> render(holder, element, SyntaxHighlight.PRIM_CALL);
            case AyaPsiDataDecl $ -> render(holder, element, SyntaxHighlight.DATA_CALL);
            case AyaPsiClassDecl $ -> render(holder, element, SyntaxHighlight.STRUCT_CALL);
            case AyaPsiDataBody $ -> render(holder, element, SyntaxHighlight.CON_CALL);
            // note: no ClassMember here because it can be highlighted without knowing the definition.
            case null, default -> {}
          }
        }
      }
    }
  }

  private void render(@NotNull AnnotationHolder holder, @NotNull PsiElement element, @Nullable TextAttributesKey color) {
    if (color == null) return;
    holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
      .textAttributes(color)
      .range(element)
      .create();
  }
}
