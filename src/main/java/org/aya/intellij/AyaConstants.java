package org.aya.intellij;

import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import org.aya.generic.Constants;
import org.jetbrains.annotations.NotNull;

public interface AyaConstants {
  @NotNull String AYA_NAME = AyaBundle.INSTANCE.message("aya.name");
  @NotNull String BUILD_FILE_NAME = Constants.AYA_JSON;
  @NotNull ProjectSystemId SYSTEM_ID = new ProjectSystemId("AYA");
}
