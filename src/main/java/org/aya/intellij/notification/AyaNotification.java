package org.aya.intellij.notification;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import org.jetbrains.annotations.NotNull;

public class AyaNotification {
  public static final @NotNull String ID_BALLOON = "org.aya.intellij.notification.AyaNotification.BALLOON";
  public static final @NotNull String ID_TOOL_WINDOW = "org.aya.intellij.notification.AyaNotification.TOOL_WINDOW";
  public static final @NotNull NotificationGroup BALLOON = NotificationGroupManager.getInstance()
    .getNotificationGroup(ID_BALLOON);
  public static final @NotNull NotificationGroup TOOL_WINDOW = NotificationGroupManager.getInstance()
    .getNotificationGroup(ID_TOOL_WINDOW);
}
