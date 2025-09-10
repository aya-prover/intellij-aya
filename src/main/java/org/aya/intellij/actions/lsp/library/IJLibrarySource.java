package org.aya.intellij.actions.lsp.library;

import com.intellij.openapi.project.Project;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.aya.cli.library.source.LibrarySource;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.util.position.SourceFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/// TODO: we may use IJLibrarySource all the time, that makes thing easier
///       The current solution intends to save time if the user doesn't really make any changes, however, that won't happens.
///       As the timestamp will be updated regardless if the content is changed.
///
/// The case that "[#psiFile] set to null and the timestamp of the original file doesn't change" doesn't matter,
/// as `recompile` is only triggered when: some file is changed / some action is taken and in memory file is changed.
/// Re: Well... maybe does matter.
public class IJLibrarySource extends LibrarySource {
  public final @NotNull LibrarySource origin;

  /// In memory replacement for [#origin], the user should not use this field in most case.
  ///
  /// Assumption: [org.aya.intellij.actions.lsp.AyaLsp#dispatcher] is idle \iff [#psiFile] == null
  ///
  /// @see org.aya.intellij.actions.lsp.UtilsKt#useLsp(Project, AyaPsiFile, Function1, Function2, Continuation)
  public @Nullable AyaPsiFile psiFile;

  public IJLibrarySource(@NotNull IJLibraryOwner owner, @NotNull LibrarySource origin, @Nullable AyaPsiFile psiFile) {
    super(owner, origin.underlyingFile, origin.isLiterate);

    this.origin = origin;
    this.psiFile = psiFile;
  }

  @Override
  public @NotNull SourceFile originalFile() throws IOException {
    if (psiFile == null) return origin.originalFile();
    return originalFile(psiFile.getFileDocument().getText());
  }
}
