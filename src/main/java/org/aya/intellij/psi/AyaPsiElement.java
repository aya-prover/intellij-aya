package org.aya.intellij.psi;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import org.aya.intellij.psi.concrete.AyaPsiModule;
import org.jetbrains.annotations.NotNull;

public interface AyaPsiElement extends PsiElement {
  default @NotNull ImmutableSeq<String> getContainingModuleName() {
    var modulePsi = PsiTreeUtil.getParentOfType(this, AyaPsiModule.class);
    var subModule = modulePsi != null
      ? SeqView.of(modulePsi.getWeakId().getText())
      : SeqView.<String>empty();

    var psiFile = getContainingFile();
    var fileModule = SeqView.of(psiFile.getName().replaceAll("\\.aya$", ""));
    var fallback = fileModule.concat(subModule);

    // If the file only exists in memory, the file name represents the module name.
    var virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) return fallback.toImmutableSeq();

    // Otherwise, find the relative path to source root
    var indexer = ProjectFileIndex.getInstance(getProject());
    var sourceRoot = indexer.getSourceRootForFile(virtualFile);
    if (sourceRoot == null) return fallback.toImmutableSeq();

    var relativePath = VfsUtilCore.getRelativePath(virtualFile, sourceRoot, VfsUtilCore.VFS_SEPARATOR_CHAR);
    assert relativePath != null;
    return ImmutableSeq.of(relativePath.split(VfsUtilCore.VFS_SEPARATOR)).view()
      .dropLast(1)
      .concat(fallback)
      .toImmutableSeq();
  }
}
