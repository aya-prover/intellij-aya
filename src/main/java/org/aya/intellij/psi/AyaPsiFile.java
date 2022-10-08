package org.aya.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.FileViewProvider;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.language.AyaFileType;
import org.aya.parser.AyaLanguage;
import org.jetbrains.annotations.NotNull;

public class AyaPsiFile extends PsiFileBase implements AyaPsiElement {
  public AyaPsiFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, AyaLanguage.INSTANCE);
  }

  @Override public @NotNull FileType getFileType() {
    return AyaFileType.INSTANCE;
  }

  @Override public @NotNull ImmutableSeq<String> containingFileModule() {
    var fileModule = SeqView.of(getName().replaceAll("\\.aya$", ""));

    // If the file only exists in memory, the file name represents the module name.
    var virtualFile = getVirtualFile();
    if (virtualFile == null) return fileModule.toImmutableSeq();

    // Otherwise, find the relative path to source root
    var indexer = ProjectFileIndex.getInstance(getProject());
    var sourceRoot = indexer.getSourceRootForFile(virtualFile);
    if (sourceRoot == null) return fileModule.toImmutableSeq();

    var relativePath = VfsUtilCore.getRelativePath(virtualFile, sourceRoot, VfsUtilCore.VFS_SEPARATOR_CHAR);
    assert relativePath != null;
    return ImmutableSeq.of(relativePath.split(VfsUtilCore.VFS_SEPARATOR)).view()
      .dropLast(1)
      .concat(fileModule)
      .toImmutableSeq();
  }
}
