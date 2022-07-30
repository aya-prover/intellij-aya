package org.aya.intellij.settings;

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
public class AyaSettingsState implements PersistentStateComponent<AyaSettingsState> {
  public boolean useAyaLsp = true;
  public boolean autoScrollToSource = true;
  public boolean autoScrollFromSource = true;

  public static @NotNull AyaSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(AyaSettingsState.class);
  }

  @Nullable @Override public AyaSettingsState getState() {
    return this;
  }

  @Override public void loadState(@NotNull AyaSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
