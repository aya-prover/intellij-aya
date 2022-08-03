package org.aya.intellij.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "org.aya.intellij.settings",
  storages = @Storage("IntellijAya.xml")
)
public class AyaSettingService implements PersistentStateComponent<AyaSettingService> {
  public boolean useAyaLsp = true;
  public boolean autoScrollToSource = true;
  public boolean autoScrollFromSource = true;

  public static @NotNull AyaSettingService getInstance() {
    return ApplicationManager.getApplication().getService(AyaSettingService.class);
  }

  @Override public @Nullable AyaSettingService getState() {
    return this;
  }

  @Override public void loadState(@NotNull AyaSettingService state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
