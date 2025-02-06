package org.aya.intellij;

import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import org.aya.generic.Constants;
import org.jetbrains.annotations.NotNull;

public interface AyaConstants {
  @NotNull String AYA_NAME = AyaBundle.INSTANCE.message("aya.name");
  @NotNull String AYA_PROVER_NAME = AyaBundle.INSTANCE.message("aya.group.name");
  @NotNull String BUILD_FILE_NAME = Constants.AYA_JSON;
  @NotNull ProjectSystemId SYSTEM_ID = new ProjectSystemId("AYA", AYA_PROVER_NAME);
  @NotNull String AYA_ES_SETTINGS = "aya.xml";

  @NotNull String IDEA_PROJECT_FILE_DIR = ".idea";
}
