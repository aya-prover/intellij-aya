package org.aya.intellij.parser;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.aya.parser.GeneratedLexerTokens;
import org.jetbrains.annotations.NotNull;

public class AyaNamesValidator implements NamesValidator {
  @Override public boolean isKeyword(@NotNull String name, Project project) {
    // TODO: generate unicode keywords in build tasks
    return GeneratedLexerTokens.KEYWORDS.containsValue(name);
  }

  @Override public boolean isIdentifier(@NotNull String name, Project project) {
    return !isKeyword(name, project);
  }
}
