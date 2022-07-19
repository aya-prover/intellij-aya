package org.aya.intellij.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiElementTypes;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.psi.concrete.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiImplUtils {
  private static @Nullable PsiElement findChild(@Nullable PsiElement element, @NotNull IElementType type) {
    if (element == null) return null;
    var child = element.getNode().findChildByType(type);
    if (child == null) return null;
    return child.getPsi();
  }

  public static @NotNull PsiElement setName(@NotNull AyaPsiNamedElement element, @NotNull String newName) throws IncorrectOperationException {
    var nameId = getNameIdentifier(element);
    if (nameId == null) throw new IncorrectOperationException("No name identifier found for " + element.getText());
    var newElement = AyaPsiFactory.createLeafFromText(element.getProject(), newName);
    nameId.replace(newElement);
    return element;
  }

  public static @Nullable PsiElement getNameIdentifier(@NotNull AyaPsiNamedElement element) {
    var declNameOrInfix = PsiTreeUtil.findChildOfType(element, AyaPsiDeclNameOrInfix.class);
    var weakId = PsiTreeUtil.findChildOfType(declNameOrInfix, AyaPsiWeakId.class);
    var id = findChild(weakId, AyaPsiElementTypes.ID);
    return id != null ? id : findChild(weakId, AyaPsiElementTypes.REPL_COMMAND);
  }

  public static boolean isLambdaOrForallTele(@NotNull AyaPsiTele tele) {
    // whether the RefExpr in its children should be treated as name identifier owner instead of reference.
    // see AyaProducer: visitLamTelescope and visitForallTelescope
    return PsiTreeUtil.getParentOfType(tele, AyaPsiLambdaExpr.class) != null
      || PsiTreeUtil.getParentOfType(tele, AyaPsiForallExpr.class) != null;
  }

  public static @NotNull PsiElement setName(@NotNull AyaPsiTele tele, @NotNull String newName) {
    // TODO: rename tele
    throw new IncorrectOperationException("Not implemented");
  }

  public static @Nullable PsiElement getNameIdentifier(@NotNull AyaPsiTele tele) {
    var isParamLiteral = isLambdaOrForallTele(tele);
    return switch (tele) {
      case AyaPsiTeleLit lit -> getQualifiedIdInsideLiteral(lit);
      case AyaPsiTeleIm im -> getNameIdentifier(im.getTeleBinder(), isParamLiteral);
      case AyaPsiTeleEx ex -> getNameIdentifier(ex.getTeleBinder(), isParamLiteral);
      default -> null;
    };
  }

  private static @Nullable PsiElement getNameIdentifier(@NotNull AyaPsiTeleBinder binder, boolean isParamLiteral) {
    return switch (binder) {
      case AyaPsiTeleBinderTyped typed -> typed.getIdsNonEmpty(); // TODO: safe to return multiple id?
      // see AyaProducer: visitTele
      case AyaPsiTeleBinderAnonymous anonymous -> isParamLiteral ? getQualifiedIdInsideLiteral(anonymous) : null;
      default -> null;
    };
  }

  private static @Nullable PsiElement getQualifiedIdInsideLiteral(@Nullable AyaPsiElement element) {
    var literal = PsiTreeUtil.getParentOfType(element, AyaPsiLiteral.class);
    return PsiTreeUtil.findChildOfType(literal, AyaPsiQualifiedId.class);
  }
}
