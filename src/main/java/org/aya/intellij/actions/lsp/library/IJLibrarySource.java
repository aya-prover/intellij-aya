package org.aya.intellij.actions.lsp.library;

import org.aya.cli.library.source.LibraryOwner;
import org.aya.cli.library.source.LibrarySource;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.util.position.SourceFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

/// TODO: we may use IJLibrarySource all the time, that makes thing easier
///       The current solution intends to save time if the user doesn't really make any changes, however, that won't happens.
///       As the timestamp will be updated regardless if the content is changed.
///
/// The case that "[#psiFile] set to null and the timestamp of the original file doesn't change" doesn't matter,
/// as `recompile` is only triggered when: some file is changed / some action is taken and in memory file is changed.
/// Re: Well... maybe does matter.
public class IJLibrarySource extends LibrarySource {
  /// In memory replacement for [#underlyingFile], the user should not use this field in most case.
  ///
  /// Assumption: [org.aya.intellij.actions.lsp.AyaLsp#dispatcher] is idle \iff [#psiFile] == null
  ///
  /// @see org.aya.intellij.actions.lsp.UtilsKt#useLsp
  public @Nullable AyaPsiFile psiFile;

  public IJLibrarySource(@NotNull LibraryOwner owner, @NotNull Path underlying, @Nullable AyaPsiFile psiFile, boolean isLiterate) {
    super(owner, underlying, isLiterate);
    this.psiFile = psiFile;
  }

  @Override
  public @NotNull SourceFile originalFile() throws IOException {
    // The caller (either direct or indirect) should wrap the lsp action in a read/write action
    if (psiFile == null) return super.originalFile();
    return originalFile(psiFile.getFileDocument().getText());
  }
}
