package org.aya.intellij.lsp;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.aya.util.FileUtil;
import org.aya.util.error.SourcePos;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;

public interface JB {
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
