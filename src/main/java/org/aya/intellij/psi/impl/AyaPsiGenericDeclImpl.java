package org.aya.intellij.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.aya.intellij.psi.AyaPsiGenericDecl;
import org.aya.intellij.psi.concrete.AyaPsiDeclNameOrInfix;
import org.aya.intellij.psi.utils.AyaPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiGenericDeclImpl extends AyaPsiNamedElementImpl implements AyaPsiGenericDecl {
  public AyaPsiGenericDeclImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override public @Nullable PsiElement getNameIdentifier() {
    var declNameOrInfix = PsiTreeUtil.findChildOfType(this, AyaPsiDeclNameOrInfix.class);
    return declNameOrInfix != null
      ? AyaPsiUtils.getNameIdFromWeakIdChild(declNameOrInfix)
      // primitive decls directly use weakId, instead of declNameOrInfix.
      : AyaPsiUtils.getNameIdFromWeakIdChild(this);
  }

  @Override public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
    var nameId = getNameIdentifier();
    return AyaPsiUtils.setNameIdToWeakIdChild(this, name, nameId);
  }
}
