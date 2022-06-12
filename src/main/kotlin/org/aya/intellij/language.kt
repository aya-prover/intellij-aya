package org.aya.intellij

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory
import org.antlr.intellij.adaptor.lexer.RuleIElementType
import org.antlr.intellij.adaptor.lexer.TokenIElementType
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.aya.parser.AyaLexer
import org.aya.parser.AyaParser
import javax.swing.*


object AyaLanguage : Language("Aya")

object AyaIcons {
  // TODO: make a file type icon
  val FILE = AllIcons.FileTypes.Java
}

object AyaFileType : LanguageFileType(AyaLanguage) {
  override fun getName(): String = "Aya File"
  override fun getDescription(): String = "Aya Prover source file"
  override fun getDefaultExtension(): String = "aya"
  override fun getIcon(): Icon = AyaIcons.FILE
}

class AyaParserDefinition : ParserDefinition {
  override fun createLexer(project: Project?): Lexer {
    val lexer = AyaLexer(null)
    return ANTLRLexerAdaptor(AyaLanguage, lexer)
  }

  override fun createParser(project: Project?): PsiParser {
    val parser = AyaParser(null)
    return object : ANTLRParserAdaptor(AyaLanguage, parser) {
      override fun parse(parser: Parser, root: IElementType?): ParseTree {
        return (parser as AyaParser).program()
      }
    }
  }

  override fun getWhitespaceTokens(): TokenSet = WHITESPACE
  override fun getCommentTokens(): TokenSet = COMMENTS
  override fun getStringLiteralElements(): TokenSet = STRING

  override fun spaceExistanceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
    return ParserDefinition.SpaceRequirements.MAY
  }

  override fun getFileNodeType(): IFileElementType = FILE

  /** Create the root of your PSI tree (a PsiFile).
   *
   * From IntelliJ IDEA Architectural Overview:
   * "A PSI (Program Structure Interface) file is the root of a structure
   * representing the contents of a file as a hierarchy of elements
   * in a particular programming language."
   *
   * PsiFile is to be distinguished from a FileASTNode, which is a parse
   * tree node that eventually becomes a PsiFile. From PsiFile, we can get
   * it back via: [PsiFile.getNode].
   */
  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return AyaPSIFileRoot(viewProvider)
  }

  /** Convert from *NON-LEAF* parse node (AST they call it)
   * to PSI node. Leaves are created in the AST factory.
   * Rename re-factoring can cause this to be
   * called on a TokenIElementType since we want to rename ID nodes.
   * In that case, this method is called to create the root node
   * but with ID type. Kind of strange, but we can simply create a
   * ASTWrapperPsiElement to make everything work correctly.
   *
   * RuleIElementType.  Ah! It's that ID is the root
   * IElementType requested to parse, which means that the root
   * node returned from parsetree->PSI conversion.  But, it
   * must be a CompositeElement! The adaptor calls
   * rootMarker.done(root) to finish off the PSI conversion.
   * See [ANTLRParserAdaptor.parse]
   *
   * If you don't care to distinguish PSI nodes by type, it is
   * sufficient to create a [ANTLRPsiNode] around
   * the parse tree node
   */
  override fun createElement(node: ASTNode): PsiElement {
    val type = node.elementType
    if (type is TokenIElementType || type !is RuleIElementType) return ANTLRPsiNode(node)
    return when (type.ruleIndex) {
      AyaParser.RULE_fnDecl -> AyaDecl(node, type, "fnDecl/declNameOrInfix/ID")
      AyaParser.RULE_primDecl -> AyaDecl(node, type, "primDecl/ID")
      AyaParser.RULE_dataDecl -> AyaDecl(node, type, "dataDecl/declNameOrInfix/ID")
      AyaParser.RULE_structDecl -> AyaDecl(node, type, "structDecl/declNameOrInfix/ID")
      AyaParser.RULE_tele -> AyaTele(node, type)
      else -> ANTLRPsiNode(node)
    }
  }

  companion object {
    val FILE: IFileElementType = IFileElementType(AyaLanguage)
    var ID: TokenIElementType? = null

    init {
      PSIElementTypeFactory.defineLanguageIElementTypes(
        AyaLanguage,
        AyaParser.tokenNames,
        AyaParser.ruleNames,
      )
      val tokenIElementTypes = PSIElementTypeFactory.getTokenIElementTypes(AyaLanguage)
      ID = tokenIElementTypes[AyaLexer.ID]
    }

    val COMMENTS: TokenSet = PSIElementTypeFactory.createTokenSet(
      AyaLanguage,
      AyaLexer.COMMENT,
      AyaLexer.LINE_COMMENT,
      AyaLexer.DOC_COMMENT,
    )
    val WHITESPACE: TokenSet = PSIElementTypeFactory.createTokenSet(
      AyaLanguage,
      AyaLexer.WS,
    )
    val STRING: TokenSet = PSIElementTypeFactory.createTokenSet(
      AyaLanguage,
      AyaLexer.STRING,
    )
  }
}
