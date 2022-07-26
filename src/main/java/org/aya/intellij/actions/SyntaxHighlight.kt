package org.aya.intellij.actions

import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.aya.intellij.parser.AyaParserDefinition
import org.aya.intellij.parser._AyaPsiLexer
import org.aya.intellij.psi.types.AyaPsiElementTypes

class SyntaxHighlight : SyntaxHighlighterBase() {
  override fun getHighlightingLexer() = FlexAdapter(_AyaPsiLexer())

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
    in AyaParserDefinition.KEYWORDS -> pack(KEYWORD)
    in AyaParserDefinition.MARKERS -> pack(KEYWORD)

    AyaParserDefinition.BLOCK_COMMENT -> pack(BLOCK_COMMENT)
    AyaParserDefinition.LINE_COMMENT -> pack(LINE_COMMENT)
    AyaPsiElementTypes.DOC_COMMENT -> pack(DOC_COMMENT)

    AyaPsiElementTypes.ID -> pack(ID)
    AyaPsiElementTypes.NUMBER -> pack(NUMBER)
    AyaPsiElementTypes.STRING -> pack(STRING)
    AyaPsiElementTypes.DOT -> pack(DOT)
    AyaPsiElementTypes.COMMA -> pack(COMMA)
    AyaPsiElementTypes.LPAREN, AyaPsiElementTypes.RPAREN -> pack(PARENTHESES)
    AyaPsiElementTypes.LBRACE, AyaPsiElementTypes.RBRACE -> pack(BRACES)
    AyaPsiElementTypes.LARRAY, AyaPsiElementTypes.RARRAY -> pack(BRACKETS)
    AyaPsiElementTypes.LGOAL, AyaPsiElementTypes.RGOAL -> pack(GOAL)

    TokenType.BAD_CHARACTER -> pack(HighlighterColors.BAD_CHARACTER)
    else -> pack(SEMANTICS)
  }

  companion object {
    @JvmField
    val ID = TextAttributesKey.createTextAttributesKey("AYA_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField
    val KEYWORD = TextAttributesKey.createTextAttributesKey("AYA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField
    val STRING = TextAttributesKey.createTextAttributesKey("AYA_STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField
    val NUMBER = TextAttributesKey.createTextAttributesKey("AYA_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField
    val LINE_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField
    val BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    @JvmField
    val DOC_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    @JvmField
    val COMMA = TextAttributesKey.createTextAttributesKey("AYA_COMMA", DefaultLanguageHighlighterColors.COMMA)
    @JvmField
    val DOT = TextAttributesKey.createTextAttributesKey("AYA_DOT", DefaultLanguageHighlighterColors.DOT)
    @JvmField
    val PARENTHESES = TextAttributesKey.createTextAttributesKey("AYA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    @JvmField
    val BRACES = TextAttributesKey.createTextAttributesKey("AYA_BRACES", DefaultLanguageHighlighterColors.BRACES)
    @JvmField
    val BRACKETS = TextAttributesKey.createTextAttributesKey("AYA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField
    val GOAL = TextAttributesKey.createTextAttributesKey("AYA_GOAL", DefaultLanguageHighlighterColors.METADATA)
    @JvmField
    val FN_DEF = TextAttributesKey.createTextAttributesKey("AYA_FN_DEF", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    @JvmField
    val FN_CALL = TextAttributesKey.createTextAttributesKey("AYA_FN_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    @JvmField
    val PRIM_DEF = TextAttributesKey.createTextAttributesKey("AYA_PRIM_DEF", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    @JvmField
    val PRIM_CALL = TextAttributesKey.createTextAttributesKey("AYA_PRIM_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    @JvmField
    val DATA_DEF = TextAttributesKey.createTextAttributesKey("AYA_DATA_DEF", DefaultLanguageHighlighterColors.CLASS_NAME)
    @JvmField
    val DATA_CALL = TextAttributesKey.createTextAttributesKey("AYA_DATA_CALL", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
    @JvmField
    val STRUCT_DEF = TextAttributesKey.createTextAttributesKey("AYA_STRUCT_DEF", DefaultLanguageHighlighterColors.CLASS_NAME)
    @JvmField
    val STRUCT_CALL = TextAttributesKey.createTextAttributesKey("AYA_STRUCT_CALL", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
    @JvmField
    val FIELD_DEF = TextAttributesKey.createTextAttributesKey("AYA_FIELD_DEF", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField
    val FIELD_CALL = TextAttributesKey.createTextAttributesKey("AYA_FIELD_CALL", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField
    val CON_DEF = TextAttributesKey.createTextAttributesKey("AYA_CON_DEF", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField
    val CON_CALL = TextAttributesKey.createTextAttributesKey("AYA_CON_CALL", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField
    val GENERALIZE = TextAttributesKey.createTextAttributesKey("AYA_GENERALIZE", JavaHighlightingColors.TYPE_PARAMETER_NAME_ATTRIBUTES)
    @JvmField
    val SEMANTICS = TextAttributesKey.createTextAttributesKey("AYA_SEMANTIC")
  }
}
