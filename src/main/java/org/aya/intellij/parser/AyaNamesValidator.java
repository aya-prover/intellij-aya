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
    var lexer = AyaParserDefinition.createLexer();
    lexer.start(name);
    var type = lexer.getTokenType();
    return lexer.getTokenEnd() == lexer.getBufferEnd() && AyaParserDefinition.IDENTIFIERS.contains(type);
  }

  public static boolean isAyaIdentifierStart(char c) {
    // ID = {AYA_LETTER} {AYA_LETTER_FOLLOW}* | \- {AYA_LETTER} {AYA_LETTER_FOLLOW}* | \/\\ | \\\/
    return isAyaLetter(c) || c == '-' || c == '/' || c == '\\';
  }

  public static boolean isAyaIdentifierPart(char c) {
    // ID = {AYA_LETTER} {AYA_LETTER_FOLLOW}* | \- {AYA_LETTER} {AYA_LETTER_FOLLOW}*
    return isAyaLetterFollow(c) || c == '\\' || c == '/';
  }

  public static boolean isAyaSimpleLetter(char c) {
    // AYA_SIMPLE_LETTER = [~!@#$%\^&*+=<>?/|\[\]a-zA-Z_u+2200-u+22FF]
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
      || "~!@#$%^&*+=<>?/|[]_".indexOf(c) != -1
      || (c >= '\u2200' && c <= '\u22FF');
  }

  public static boolean isAyaUnicode(char c) {
    // AYA_UNICODE = [u+0080-u+FEFE] | [u+FF00-u+10FFFF]
    // u+10FFFF = u+DBFF u+UDFF which we already covered below
    return (c >= '\u0080' && c <= '\uFEFE') || (c >= '\uFF00');
  }

  public static boolean isAyaLetter(char c) {
    // AYA_LETTER = {AYA_SIMPLE_LETTER} | {AYA_UNICODE}
    return isAyaSimpleLetter(c) || isAyaUnicode(c);
  }

  public static boolean isAyaLetterFollow(char c) {
    // AYA_LETTER_FOLLOW = {AYA_LETTER} | [0-9'-]
    return isAyaLetter(c) || (c == '-' || c == '\'' || (c >= '0' && c <= '9'));
  }
}
