package org.aya.intellij.lsp;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.aya.generic.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaStartup implements StartupActivity {
  private static final @NotNull Key<AyaLSP> AYA_LSP = Key.create("intellij.aya.lsp");

  @Override public void runActivity(@NotNull Project project) {
    var ayaJson = findAyaJson(project);
    if (ayaJson != null) {
      if (!JB.fileSupported(ayaJson)) return;
      var lsp = new AyaLSP();
      lsp.service().registerLibrary(JB.canonicalize(ayaJson.getParent()));
      project.putUserData(AYA_LSP, lsp);
      System.out.println("[intellij-aya] Hello, this is Aya Language Server");
    }
  }

  public static @Nullable AyaLSP of(@NotNull Project project) {
    return project.getUserData(AYA_LSP);
  }

  private @Nullable VirtualFile findAyaJson(@NotNull Project project) {
    var mods = ModuleManager.getInstance(project).getModules();
    if (mods.length != 1) return null;
    var mod = mods[0];
    var contentRoots = ModuleRootManager.getInstance(mod).getContentRoots();
    if (contentRoots.length != 1) return null;
    var root = contentRoots[0];
    return root.findChild(Constants.AYA_JSON);
  }
}
