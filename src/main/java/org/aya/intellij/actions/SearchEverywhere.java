package org.aya.intellij.actions;

import com.intellij.navigation.ChooseByNameContributorEx2;
import com.intellij.navigation.NavigationItem;
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
import org.aya.intellij.AyaFileType;
import org.aya.intellij.lsp.AyaLsp;
import org.aya.intellij.lsp.JB;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiGenericDecl;
import org.aya.intellij.ui.AyaNavItem;
import org.aya.ref.DefVar;
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

  private static @NotNull SeqView<Tuple2<AyaPsiFile, SeqView<DefVar<?, ?>>>> search(@NotNull FindSymbolParameters parameters) {
    var project = parameters.getProject();
    var manager = PsiManager.getInstance(project);
    var indexed = FileTypeIndex.getFiles(AyaFileType.INSTANCE, parameters.getSearchScope());
    return AyaLsp.use(project, SeqView::empty, lsp -> ImmutableSeq.from(indexed).view()
      .map(manager::findFile)
      .filterIsInstance(AyaPsiFile.class)
      .map(file -> Tuple.of(file, lsp.symbolsInFile(file)))
      .map(tup -> Tuple.of(tup._1, tup._2.filter(s -> s.concrete != null))));
  }

  private static @NotNull SeqView<Tuple2<DefVar<?, ?>, AyaPsiGenericDecl>> searchGenericDecl(@NotNull FindSymbolParameters parameters) {
    return search(parameters).flatMap(tup -> tup._2.mapNotNull(defVar -> {
      var psi = JB.elementAt(tup._1, defVar.concrete.sourcePos(), AyaPsiGenericDecl.class);
      return psi == null ? null : Tuple.of(defVar, psi);
    }));
  }

  class Symbol implements SearchEverywhere {
    @Override public void processNames(
      @NotNull Processor<? super String> processor,
      @NotNull FindSymbolParameters parameters
    ) {
      searchGenericDecl(parameters).forEach(psi -> processor.process(psi._2.nameOrEmpty()));
    }

    @Override public void processElementsWithName(
      @NotNull String name,
      @NotNull Processor<? super NavigationItem> processor,
      @NotNull FindSymbolParameters parameters
    ) {
      searchGenericDecl(parameters)
        .filter(psi -> psi._1.name().equals(name))
        .map(t -> new AyaNavItem(t._2, true))
        .forEach(processor::process);
    }
  }
}
