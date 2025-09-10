package org.aya.intellij.actions.lsp.library;

import kala.collection.SeqView;
import kala.collection.mutable.MutableList;
import org.aya.cli.library.json.LibraryConfig;
import org.aya.cli.library.source.LibraryOwner;
import org.aya.cli.library.source.LibrarySource;
import org.aya.cli.library.source.MutableLibraryOwner;
import org.aya.util.position.SourceFileLocator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/// looks like a DisLibraryOwner but with [IJLibrarySource], maybe we can elim this class by extending [org.aya.cli.library.source.DiskLibraryOwner]
public record IJLibraryOwner(
  @NotNull MutableLibraryOwner delegate,
  @Override @NotNull MutableList<LibrarySource> librarySourcesMut     // in fact, this is `MutableList<IJLibrarySource>`
) implements MutableLibraryOwner {
  public IJLibraryOwner(@NotNull MutableLibraryOwner owner) {
    this(owner, MutableList.create());

    // transfer owner
    owner.librarySources()
      .map(it -> new IJLibrarySource(this, it, null))
      .forEach(librarySourcesMut::append);
  }

  @Override
  public @NotNull MutableList<Path> modulePathMut() {
    return delegate.modulePathMut();
  }

  @Override
  public @NotNull MutableList<LibraryOwner> libraryDepsMut() {
    return delegate.libraryDepsMut();
  }

  @Override
  public @NotNull SeqView<Path> modulePath() {
    return delegate.modulePath();
  }

  @Override
  public @NotNull SeqView<LibraryOwner> libraryDeps() {
    return delegate.libraryDeps();
  }

  @Override
  public @NotNull SourceFileLocator locator() {
    return delegate.locator();
  }

  @Override
  public @NotNull LibraryConfig underlyingLibrary() {
    return delegate.underlyingLibrary();
  }

  @Override
  public void addModulePath(@NotNull Path newPath) {
    delegate.addModulePath(newPath);
  }
}
