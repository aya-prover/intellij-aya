package org.aya.intellij.lsp;

import com.intellij.diff.util.LineCol;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import kala.control.Option;
import org.aya.util.FileUtil;
import org.aya.util.error.SourceFile;
import org.aya.util.error.SourcePos;
import org.javacs.lsp.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Converts position related data (i.e. lines/columns, offsets) between Aya Compiler and JetBrains.
 *
 * @implNote ALWAYS CONVERT BEFORE USE data across the boundary even though they are directly available.
 */
public interface JB {
  /** @return the return value will be converted to {@link org.aya.lsp.utils.XY} in LSP. */
  static @NotNull Position toXyPosition(@NotNull PsiElement element) {
    var doc = element.getContainingFile().getViewProvider().getDocument();
    var lineCol = LineCol.fromOffset(doc, element.getTextOffset());
    return new Position(lineCol.line, lineCol.column + 1);
  }

  static boolean isEofError(@NotNull SourcePos sourcePos) {
    return sourcePos.tokenStartIndex() == SourcePos.UNAVAILABLE_AND_FUCK_ANTLR4
      && sourcePos.tokenEndIndex() == SourcePos.UNAVAILABLE_AND_FUCK_ANTLR4;
  }

  static @NotNull TextRange toRange(@NotNull SourcePos sourcePos) {
    return new TextRange(startOffset(sourcePos), endOffset(sourcePos));
  }

  static int endOffset(@NotNull SourcePos sourcePos) {
    return sourcePos.tokenEndIndex() + 1;
  }

  static int startOffset(@NotNull SourcePos sourcePos) {
    return sourcePos.tokenStartIndex();
  }

  static boolean fileSupported(@NotNull VirtualFile file) {
    // TODO: support non-local libraries in Aya language server
    return file.isInLocalFileSystem();
  }

  static @NotNull Path canonicalize(@NotNull VirtualFile file) {
    assert fileSupported(file);
    return FileUtil.canonicalize(file.toNioPath());
  }

  static @NotNull Option<PsiFile> fileAt(@NotNull Project project, @NotNull SourceFile sourceFile) {
    return sourceFile.underlying()
      .mapNotNull(path -> VirtualFileManager.getInstance().findFileByNioPath(path))
      .mapNotNull(virtualFile -> PsiManager.getInstance(project).findFile(virtualFile));
  }

  static @Nullable PsiElement elementAt(@NotNull Project project, @NotNull SourcePos pos) {
    return fileAt(project, pos.file())
      .mapNotNull(psiFile -> elementAt(psiFile, pos))
      .getOrNull();
  }

  static @Nullable PsiElement elementAt(@NotNull PsiFile file, @NotNull SourcePos pos) {
    return file.findElementAt(toRange(pos).getStartOffset());
  }

  static <T extends PsiElement> @Nullable T elementAt(@NotNull PsiFile file, @NotNull SourcePos pos, @NotNull Class<T> type) {
    return PsiTreeUtil.getParentOfType(elementAt(file, pos), type);
  }

  static <T extends PsiElement> @Nullable T elementAt(@NotNull Project project, @NotNull SourcePos pos, @NotNull Class<T> type) {
    return PsiTreeUtil.getParentOfType(elementAt(project, pos), type);
  }
}
