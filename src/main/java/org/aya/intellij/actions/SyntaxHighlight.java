package org.aya.intellij.actions;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.aya.intellij.parser._AyaPsiLexer;
import org.aya.lsp.models.HighlightResult;
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
  public static final @NotNull TextAttributesKey COMMA = TextAttributesKey.createTextAttributesKey("AYA_COMMA", DefaultLanguageHighlighterColors.COMMA);
  public static final @NotNull TextAttributesKey DOT = TextAttributesKey.createTextAttributesKey("AYA_DOT", DefaultLanguageHighlighterColors.DOT);
  public static final @NotNull TextAttributesKey PARENTHESES = TextAttributesKey.createTextAttributesKey("AYA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
  public static final @NotNull TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey("AYA_BRACES", DefaultLanguageHighlighterColors.BRACES);
  public static final @NotNull TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey("AYA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
  public static final @NotNull TextAttributesKey GOAL = TextAttributesKey.createTextAttributesKey("AYA_GOAL", DefaultLanguageHighlighterColors.METADATA);
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
  public static final @NotNull TextAttributesKey LSP = TextAttributesKey.createTextAttributesKey("AYA_SEMANTIC");

  @Override public @NotNull Lexer getHighlightingLexer() {
    return new FlexAdapter(new _AyaPsiLexer());
  }

  @Override public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    return pack(LSP);
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
