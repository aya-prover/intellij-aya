package org.aya.intellij.actions.lsp.library;

import kala.collection.SeqView;
import kala.collection.mutable.MutableList;
import kala.collection.mutable.MutableMap;
import kala.control.Option;
import org.aya.cli.library.json.LibraryConfig;
import org.aya.cli.library.source.LibraryOwner;
import org.aya.cli.library.source.LibrarySource;
import org.aya.cli.library.source.MutableLibraryOwner;
import org.aya.util.position.SourceFileLocator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/// @param inMemorySourcesMut sources that are replaced with real time content, do not manually modify this.
public record IJLibraryOwner(
  @NotNull MutableLibraryOwner delegate,
  @NotNull MutableMap<LibrarySource, IJLibrarySource> inMemorySourcesMut,
  @Override @NotNull MutableList<LibrarySource> librarySourcesMut
) implements MutableLibraryOwner {
  public IJLibraryOwner(@NotNull MutableLibraryOwner owner) {
    this(owner, MutableMap.create(), MutableList.create());

    // transfer owner
    owner.librarySources()
      .map(it -> LibrarySource.create(this, it.underlyingFile))
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
  public @NotNull SeqView<LibrarySource> librarySources() {
    var map = MutableMap.from(librarySourcesMut
      .associateBy(k -> k.underlyingFile));

    map.replaceAll((p, s) ->
      Option.<LibrarySource>narrow(inMemorySourcesMut.getOption(s)).getOrDefault(s));

    return SeqView.narrow(map.valuesView().toSeq().view());
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
