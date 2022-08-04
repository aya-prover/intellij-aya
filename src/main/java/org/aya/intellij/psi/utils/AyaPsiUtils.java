package org.aya.intellij.psi.utils;

import com.intellij.ide.util.EditSourceUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.concrete.AyaPsiWeakId;
import org.aya.intellij.psi.types.AyaPsiElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaPsiUtils {
  private static @Nullable PsiElement findChild(@Nullable PsiElement element, @NotNull IElementType type) {
    if (element == null) return null;
    var child = element.getNode().findChildByType(type);
    if (child == null) return null;
    return child.getPsi();
  }

  public static @NotNull AyaPsiElement setNameIdToWeakIdChild(@NotNull AyaPsiElement element, @NotNull String newName, @Nullable PsiElement nameId) {
    if (nameId == null) throw new IncorrectOperationException("No name identifier found for " + element.getText());
    var newElement = AyaPsiFactory.leaf(element.getProject(), newName);
    nameId.replace(newElement);
    return element;
  }

  public static @Nullable PsiElement getNameIdFromWeakIdChild(@Nullable AyaPsiElement element) {
    var weakId = PsiTreeUtil.findChildOfType(element, AyaPsiWeakId.class);
    var id = findChild(weakId, AyaPsiElementTypes.ID);
    return id != null ? id : findChild(weakId, AyaPsiElementTypes.REPL_COMMAND);
  }

  public static void navigate(@NotNull PsiElement element, boolean requestFocus) {
    var descriptor = EditSourceUtil.getDescriptor(element);
    if (descriptor != null && descriptor.canNavigate()) descriptor.navigate(requestFocus);
  }
}
