package org.aya.intellij.actions;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.aya.intellij.language.AyaLanguage;
import org.aya.lsp.models.HighlightResult;
import org.aya.parser.AyaLexer;
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
  public static final @NotNull TextAttributesKey ID = TextAttributesKey.createTextAttributesKey("AYA_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final @NotNull TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("AYA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  public static final @NotNull TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("AYA_STRING", DefaultLanguageHighlighterColors.STRING);
  public static final @NotNull TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("AYA_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final @NotNull TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final @NotNull TextAttributesKey BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final @NotNull TextAttributesKey DOC_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
  public static final @NotNull TextAttributesKey FN_DEF = TextAttributesKey.createTextAttributesKey("AYA_FN_DEF", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final @NotNull TextAttributesKey FN_CALL = TextAttributesKey.createTextAttributesKey("AYA_FN_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

  public static final @NotNull TextAttributesKey PRIM_DEF = TextAttributesKey.createTextAttributesKey("AYA_PRIM_DEF", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final @NotNull TextAttributesKey PRIM_CALL = TextAttributesKey.createTextAttributesKey("AYA_PRIM_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

  public static final @NotNull TextAttributesKey DATA_DEF = TextAttributesKey.createTextAttributesKey("AYA_DATA_DEF", DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final @NotNull TextAttributesKey DATA_CALL = TextAttributesKey.createTextAttributesKey("AYA_DATA_CALL", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

  public static final @NotNull TextAttributesKey STRUCT_DEF = TextAttributesKey.createTextAttributesKey("AYA_STRUCT_DEF", DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final @NotNull TextAttributesKey STRUCT_CALL = TextAttributesKey.createTextAttributesKey("AYA_STRUCT_CALL", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

  public static final @NotNull TextAttributesKey FIELD_DEF = TextAttributesKey.createTextAttributesKey("AYA_FIELD_DEF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final @NotNull TextAttributesKey FIELD_CALL = TextAttributesKey.createTextAttributesKey("AYA_FIELD_CALL", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

  public static final @NotNull TextAttributesKey CON_DEF = TextAttributesKey.createTextAttributesKey("AYA_CON_DEF", DefaultLanguageHighlighterColors.INSTANCE_METHOD);
  public static final @NotNull TextAttributesKey CON_CALL = TextAttributesKey.createTextAttributesKey("AYA_CON_CALL", DefaultLanguageHighlighterColors.INSTANCE_METHOD);


  public static final @NotNull TextAttributesKey SEMANTIC = TextAttributesKey.createTextAttributesKey("AYA_SEMANTIC");

  @Override public @NotNull Lexer getHighlightingLexer() {
    var lexer = new AyaLexer(null);
    return new ANTLRLexerAdaptor(AyaLanguage.INSTANCE, lexer);
  }

  @Override public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    if (!(tokenType instanceof TokenIElementType antlrTokenType)) return TextAttributesKey.EMPTY_ARRAY;
    var type = antlrTokenType.getANTLRTokenType();
    if (GeneratedLexerTokens.KEYWORDS.containsKey(type)) return pack(KEYWORD);
    return switch (type) {
      case AyaLexer.ID -> pack(ID);
      case AyaLexer.NUMBER -> pack(NUMBER);
      case AyaLexer.STRING -> pack(STRING);
      case AyaLexer.COMMENT -> pack(BLOCK_COMMENT);
      case AyaLexer.LINE_COMMENT -> pack(LINE_COMMENT);
      case AyaLexer.DOC_COMMENT -> pack(DOC_COMMENT);
      case AyaLexer.ERROR_CHAR -> pack(HighlighterColors.BAD_CHARACTER);
      default -> pack(SEMANTIC);
    };
  }

  public static @Nullable TextAttributesKey choose(@Nullable HighlightResult.Kind kind) {
    return switch (kind) {
      case FnDef -> FN_DEF;
      case DataDef -> DATA_DEF;
      case StructDef -> STRUCT_DEF;
      case ConDef -> CON_DEF;
      case FieldDef -> FIELD_DEF;
      case PrimDef -> PRIM_DEF;
      case FnCall -> FN_CALL;
      case DataCall -> DATA_CALL;
      case StructCall -> STRUCT_CALL;
      case ConCall -> CON_CALL;
      case FieldCall -> FIELD_CALL;
      case PrimCall -> PRIM_CALL;
      case default, null -> null;
    };
  }
}
