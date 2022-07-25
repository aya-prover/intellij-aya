package org.aya.intellij.lsp;

import com.intellij.diff.util.LineCol;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.aya.util.FileUtil;
import org.aya.util.error.SourcePos;
import org.eclipse.lsp4j.Position;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
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

  static @NotNull TextRange toRange(@NotNull SourcePos sourcePos) {
    return new TextRange(sourcePos.tokenStartIndex(), sourcePos.tokenEndIndex() + 1);
  }

  static boolean fileSupported(@NotNull VirtualFile file) {
    // TODO: support non-local libraries in Aya language server
    return file.isInLocalFileSystem();
  }

  static @NotNull Path canonicalize(@NotNull VirtualFile file) {
    assert fileSupported(file);
    return FileUtil.canonicalize(file.toNioPath());
  }

  static @NotNull Path canonicalize(@NotNull String uri) {
    return FileUtil.canonicalize(Path.of(URI.create(uri)));
  }
}
