package org.aya.intellij

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import org.aya.intellij.parser.AyaLexer
import org.aya.intellij.parser.AyaParser
import org.aya.intellij.parser.GeneratedLexerTokens


/** A highlighter is really just a mapping from token type to
 * some text attributes using [.getTokenHighlights].
 * The reason that it returns an array, TextAttributesKey[], is
 * that you might want to mix the attributes of a few known highlighters.
 * A [TextAttributesKey] is just a name for that a theme
 * or IDE skin can set. For example, [com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD]
 * is the key that maps to what identifiers look like in the editor.
 * To change it, see dialog: Editor > Colors & Fonts > Language Defaults.
 *
 * From [doc](http://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/syntax_highlighting_and_error_highlighting.html):
 * "The mapping of the TextAttributesKey to specific attributes used
 * in an editor is defined by the EditorColorsScheme class, and can
 * be configured by the user if the plugin provides an appropriate
 * configuration interface.
 * ...
 * The syntax highlighter returns the [TextAttributesKey]
 * instances for each token type which needs special highlighting.
 * For highlighting lexer errors, the standard TextAttributesKey
 * for bad characters HighlighterColors.BAD_CHARACTER can be used."
 */
class AyaSyntaxHighlighter : SyntaxHighlighterBase() {
  override fun getHighlightingLexer(): Lexer {
    val lexer = AyaLexer(null)
    return ANTLRLexerAdaptor(AyaLanguage, lexer)
  }

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey?> {
    if (tokenType !is TokenIElementType) return EMPTY_KEYS
    val type = tokenType.antlrTokenType
    if (GeneratedLexerTokens.KEYWORDS.containsKey(type)) return arrayOf(KEYWORD)
    return when (type) {
      AyaLexer.ID -> arrayOf(ID)
      AyaLexer.STRING -> arrayOf(STRING)
      AyaLexer.COMMENT -> arrayOf(LINE_COMMENT)
      AyaLexer.LINE_COMMENT -> arrayOf(BLOCK_COMMENT)
      else -> EMPTY_KEYS
    }
  }

  companion object {
    private val EMPTY_KEYS = arrayOfNulls<TextAttributesKey>(0)
    val ID = TextAttributesKey.createTextAttributesKey("AYA_ID", DefaultLanguageHighlighterColors.IDENTIFIER)
    val KEYWORD = TextAttributesKey.createTextAttributesKey("AYA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val STRING = TextAttributesKey.createTextAttributesKey("AYA_STRING", DefaultLanguageHighlighterColors.STRING)
    val LINE_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("AYA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)

    init {
      PSIElementTypeFactory.defineLanguageIElementTypes(
        AyaLanguage,
        AyaParser.tokenNames,
        AyaParser.ruleNames,
      )
    }
  }
}

class AyaSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
    AyaSyntaxHighlighter()
}
