package org.aya.intellij.actions;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.aya.intellij.language.AyaLanguage;
import org.aya.parser.AyaLexer;
import org.aya.parser.AyaParser;
import org.aya.parser.GeneratedLexerTokens;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A highlighter is really just a mapping from token type to
 * some text attributes using [.getTokenHighlights].
 * The reason that it returns an array, TextAttributesKey[], is
 * that you might want to mix the attributes of a few known highlighters.
 * A {@link TextAttributesKey} is just a name for that a theme
 * or IDE skin can set. For example, {@link com.intellij.openapi.editor.DefaultLanguageHighlighterColors#KEYWORD}
 * is the key that maps to what identifiers look like in the editor.
 * To change it, see dialog: Editor > Colors & Fonts > Language Defaults.
 * <p>
 * From <a href="http://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/syntax_highlighting_and_error_highlighting.html">doc</a>:
 * "The mapping of the TextAttributesKey to specific attributes used
 * in an editor is defined by the EditorColorsScheme class, and can
 * be configured by the user if the plugin provides an appropriate
 * configuration interface.
 * ...
 * The syntax highlighter returns the {@link TextAttributesKey}
 * instances for each token type which needs special highlighting.
 * For highlighting lexer errors, the standard TextAttributesKey
 * for bad characters HighlighterColors.BAD_CHARACTER can be used."
 */
public class SyntaxHighlight extends SyntaxHighlighterBase {
  private final @NotNull TextAttributesKey ID = TextAttributesKey.createTextAttributesKey("AYA_ID", DefaultLanguageHighlighterColors.IDENTIFIER);
  private final @NotNull TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("AYA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  private final @NotNull TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("AYA_STRING", DefaultLanguageHighlighterColors.STRING);
  private final @NotNull TextAttributesKey INTEGER = TextAttributesKey.createTextAttributesKey("AYA_INTEGER", DefaultLanguageHighlighterColors.NUMBER);
  private final @NotNull TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  private final @NotNull TextAttributesKey BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

  public SyntaxHighlight() {
    PSIElementTypeFactory.defineLanguageIElementTypes(AyaLanguage.INSTANCE, AyaParser.tokenNames, AyaParser.ruleNames);
  }

  @Override public @NotNull Lexer getHighlightingLexer() {
    var lexer = new AyaLexer(null);
    return new ANTLRLexerAdaptor(AyaLanguage.INSTANCE, lexer);
  }

  @Override public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    if (!(tokenType instanceof TokenIElementType antlrTokenType)) return new TextAttributesKey[0];
    var type = antlrTokenType.getANTLRTokenType();
    if (GeneratedLexerTokens.KEYWORDS.containsKey(type)) return new TextAttributesKey[]{KEYWORD};
    return switch (type) {
      case AyaLexer.ID -> new TextAttributesKey[]{ID};
      case AyaLexer.NUMBER -> new TextAttributesKey[]{INTEGER};
      case AyaLexer.STRING -> new TextAttributesKey[]{STRING};
      case AyaLexer.COMMENT -> new TextAttributesKey[]{LINE_COMMENT};
      case AyaLexer.LINE_COMMENT -> new TextAttributesKey[]{BLOCK_COMMENT};
      default -> new TextAttributesKey[0];
    };
  }

  public static final class Factory extends SyntaxHighlighterFactory {
    @Override public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
      return new SyntaxHighlight();
    }
  }
}
