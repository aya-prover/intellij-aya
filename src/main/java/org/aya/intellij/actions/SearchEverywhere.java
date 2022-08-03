package org.aya.intellij.actions;

import com.intellij.navigation.ChooseByNameContributorEx2;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.AyaFileType;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiGenericDecl;
import org.aya.ref.DefVar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SearchEverywhere extends ChooseByNameContributorEx2 {
  default @Override void processNames(
    @NotNull Processor<? super String> processor,
    @NotNull FindSymbolParameters parameters
  ) {
    processNames(processor, parameters.getSearchScope(), parameters.getIdFilter());
  }

  default @Override void processNames(
    @NotNull Processor<? super String> processor,
    @NotNull GlobalSearchScope scope,
    @Nullable IdFilter filter
  ) {
    throw new UnsupportedOperationException();
  }

  default @Override void processElementsWithName(
    @NotNull String name,
    @NotNull Processor<? super NavigationItem> processor,
    @NotNull FindSymbolParameters parameters
  ) {
    throw new UnsupportedOperationException();
  }

  private static void search(
    @NotNull FindSymbolParameters parameters,
    @NotNull BiConsumer<AyaPsiFile, DefVar<?, ?>> consumer
  ) {
    var project = parameters.getProject();
    AyaLsp.use(project, lsp -> {
      var m = PsiManager.getInstance(project);
      ImmutableSeq.from(FileTypeIndex.getFiles(AyaFileType.INSTANCE, parameters.getSearchScope()))
        .view()
        .map(m::findFile)
        .filterIsInstance(AyaPsiFile.class)
        .forEach(file -> lsp.symbolsInFile(file).forEach(s -> {
          if (s.concrete == null) return;
          consumer.accept(file, s);
        }));
    });
  }

  private static void searchPsi(
    @NotNull FindSymbolParameters parameters,
    @NotNull Consumer<AyaPsiGenericDecl> consumer
  ) {
    search(parameters, (file, defVar) -> {
      var decl = JB.elementAt(file, defVar.concrete.sourcePos(), AyaPsiGenericDecl.class);
      if (decl != null) consumer.accept(decl);
    });
  }

  class Symbol implements SearchEverywhere {
    @Override public void processNames(
      @NotNull Processor<? super String> processor,
      @NotNull FindSymbolParameters parameters
    ) {
      searchPsi(parameters, decl -> processor.process(decl.nameOrEmpty()));
    }

    @Override public void processElementsWithName(
      @NotNull String name,
      @NotNull Processor<? super NavigationItem> processor,
      @NotNull FindSymbolParameters parameters
    ) {
      searchPsi(parameters, processor::process);
    }
  }
}
