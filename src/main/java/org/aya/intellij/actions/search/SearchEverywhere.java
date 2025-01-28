package org.aya.intellij.actions.search;

import com.intellij.navigation.ChooseByNameContributorEx2;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import kala.tuple.Tuple;
import kala.tuple.Tuple2;
import org.aya.intellij.actions.lsp.AyaLsp;
import org.aya.intellij.actions.lsp.JB;
import org.aya.intellij.language.AyaFileType;
import org.aya.intellij.psi.AyaNavItem;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiGenericDecl;
import org.aya.syntax.ref.DefVar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  static @NotNull SeqView<Tuple2<AyaPsiFile, SeqView<DefVar<?, ?>>>> search(@NotNull Project project, @NotNull GlobalSearchScope searchScope) {
    var manager = PsiManager.getInstance(project);
    var indexed = FileTypeIndex.getFiles(AyaFileType.INSTANCE, searchScope);
    return AyaLsp.use(project, SeqView::empty, lsp -> ImmutableSeq.from(indexed).view()
      .map(manager::findFile)
      .filterIsInstance(AyaPsiFile.class)
      .map(file -> Tuple.of(file, lsp.symbolsInFile(file)))
      .map(tup -> Tuple.of(tup.component1(), tup.component2())));
  }

  record SearchItem(
    DefVar<?, ?> defVar,
    AyaPsiGenericDecl decl
  ) {}

  static @NotNull SeqView<SearchItem> searchGenericDecl(@NotNull Project project, @NotNull GlobalSearchScope searchScope) {
    return search(project, searchScope).flatMap(tup -> tup.component2().mapNotNull(defVar -> {
      var psi = JB.elementAt(tup.component1(), defVar.concrete.sourcePos(), AyaPsiGenericDecl.class);
      return psi == null ? null : new SearchItem(defVar, psi);
    }));
  }

  private static @NotNull SeqView<SearchItem> searchGenericDecl(@NotNull FindSymbolParameters parameters) {
    return searchGenericDecl(parameters.getProject(), parameters.getSearchScope());
  }

  class Symbol implements SearchEverywhere {
    @Override public void processNames(
      @NotNull Processor<? super String> processor,
      @NotNull FindSymbolParameters parameters
    ) {
      searchGenericDecl(parameters).forEach(psi -> processor.process(psi.decl.nameOrEmpty()));
    }

    @Override public void processElementsWithName(
      @NotNull String name,
      @NotNull Processor<? super NavigationItem> processor,
      @NotNull FindSymbolParameters parameters
    ) {
      searchGenericDecl(parameters)
        .filter(psi -> psi.defVar.name().equals(name))
        .map(t -> new AyaNavItem(t.decl, true))
        .forEach(processor::process);
    }
  }
}
