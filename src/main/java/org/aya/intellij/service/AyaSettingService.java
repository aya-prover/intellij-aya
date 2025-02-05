package org.aya.intellij.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application level settings.<br/>
 * For project level settings, see {@link org.aya.intellij.externalSystem.settings.AyaSettings}
 */
@State(
  name = "org.aya.intellij.settings",
  storages = @Storage("IntellijAya.xml")
)
public class AyaSettingService implements PersistentStateComponent<AyaSettingService> {
  public enum AyaState {
    Disable("Disable"),
    Enable("Enable"),
    UseIntegration("Use Integration");    // Enable external system integration

    public final @NotNull String text;

    AyaState(@Nls @NotNull String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public AyaState ayaLspState = AyaState.UseIntegration;
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
