# Something You Might Need

Breakpoint at `AbstractExternalSystemTask#withExecutionProgressManager` when you got an external system exception which
stack trace is not printed.

External System Manager of
Gradle: https://github.com/JetBrains/intellij-community/blob/6022bf76d7f5fb1cc97b80bb9778f994de0a3f5c/plugins/gradle/src/org/jetbrains/plugins/gradle/GradleManager.java

For auto import tracking, see `AutoImportProjectTracker#updateProjectNotification`
and `AutoImportProjectNotificationAware#notificationNotify`

Since external system is under development,
you may delete `.idea` and re-open the aya project when you get some exception.
Make sure you delete `.idea` **after** closing the project, as idea will write `.idea` when closing projects.
