package org.aya.intellij.actions.lsp.library;

import org.aya.cli.library.source.LibrarySource;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.util.position.SourceFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class IJLibrarySource extends LibrarySource {
  public final @NotNull LibrarySource origin;
  public final @Nullable AyaPsiFile psiFile;

  public IJLibrarySource(@NotNull IJLibraryOwner owner, @NotNull LibrarySource origin, @Nullable AyaPsiFile psiFile) {
    super(owner, origin.underlyingFile, origin.isLiterate);
    assert owner == origin.owner;

    this.origin = origin;
    this.psiFile = psiFile;
  }

  @Override
  public @NotNull SourceFile originalFile() throws IOException {
    if (psiFile == null) return origin.originalFile();
    return originalFile(psiFile.getFileDocument().getText());
  }
}
