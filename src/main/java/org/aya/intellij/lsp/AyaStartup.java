package org.aya.intellij.lsp;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.aya.generic.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AyaStartup implements StartupActivity {
  private static final @NotNull Key<AyaLSP> AYA_LSP = Key.create("intellij.aya.lsp");

  private static boolean useLSP() {
    // TODO: use IDEA Settings page
    return false;
  }

  @Override public void runActivity(@NotNull Project project) {
    if (!useLSP()) return;
    var ayaJson = findAyaJson(project);
    if (ayaJson != null) {
      if (!JB.fileSupported(ayaJson)) return;
      var lsp = new AyaLSP();
      lsp.registerLibrary(ayaJson.getParent());
      project.putUserData(AYA_LSP, lsp);
      project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
        @Override public void after(@NotNull List<? extends VFileEvent> events) {
          lsp.fireVfsEvent(events);
        }
      });
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
