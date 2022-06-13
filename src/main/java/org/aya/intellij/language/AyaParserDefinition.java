package org.aya.intellij.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.aya.intellij.actions.SyntaxHighlight;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.parser.AyaLexer;
import org.aya.parser.AyaParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaParserDefinition extends SyntaxHighlighterFactory implements ParserDefinition {
  private final @NotNull IFileElementType FILE = new IFileElementType(AyaLanguage.INSTANCE);
  public final @NotNull TokenIElementType ID;
  private final @NotNull TokenSet COMMENTS;
  private final @NotNull TokenSet WHITESPACE;
  private final @NotNull TokenSet STRING;

  public AyaParserDefinition() {
    PSIElementTypeFactory.defineLanguageIElementTypes(AyaLanguage.INSTANCE, AyaParser.tokenNames, AyaParser.ruleNames);
    var types = PSIElementTypeFactory.getTokenIElementTypes(AyaLanguage.INSTANCE);
    ID = types.get(AyaParser.ID);
    COMMENTS = PSIElementTypeFactory.createTokenSet(AyaLanguage.INSTANCE,
      AyaParser.COMMENT, AyaParser.LINE_COMMENT, AyaParser.DOC_COMMENT);
    WHITESPACE = PSIElementTypeFactory.createTokenSet(AyaLanguage.INSTANCE,
      AyaParser.WS);
    STRING = PSIElementTypeFactory.createTokenSet(AyaLanguage.INSTANCE,
      AyaParser.STRING);
  }

  @Override
  public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
    return new SyntaxHighlight();
  }

  @Override public @NotNull Lexer createLexer(Project project) {
    var lexer = new AyaLexer(null);
    return new ANTLRLexerAdaptor(AyaLanguage.INSTANCE, lexer);
  }

  @Override public @NotNull PsiParser createParser(Project project) {
    var ayaParser = new AyaParser(null);
    return new ANTLRParserAdaptor(AyaLanguage.INSTANCE, ayaParser) {
      @Override
      protected ParseTree parse(Parser unused, IElementType root) {
        return ayaParser.program();
      }
    };
  }

  /**
   * Create the root of your PSI tree (a PsiFile).
   * <p>
   * From IntelliJ IDEA Architectural Overview:
   * "A PSI (Program Structure Interface) file is the root of a structure
   * representing the contents of a file as a hierarchy of elements
   * in a particular programming language."
   * <p>
   * PsiFile is to be distinguished from a FileASTNode, which is a parse
   * tree node that eventually becomes a PsiFile. From PsiFile, we can get
   * it back via: {@link PsiFile#getNode()}
   */
  @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new AyaPsiFile(viewProvider);
  }

  /**
   * Convert from *NON-LEAF* parse node (AST they call it)
   * to PSI node. Leaves are created in the AST factory.
   * Rename re-factoring can cause this to be
   * called on a TokenIElementType since we want to rename ID nodes.
   * In that case, this method is called to create the root node
   * but with ID type. Kind of strange, but we can simply create a
   * ASTWrapperPsiElement to make everything work correctly.
   * <p>
   * RuleIElementType.  Ah! It's that ID is the root
   * IElementType requested to parse, which means that the root
   * node returned from parsetree->PSI conversion.  But, it
   * must be a CompositeElement! The adaptor calls
   * rootMarker.done(root) to finish off the PSI conversion.
   * See {@link ANTLRParserAdaptor#parse(IElementType, PsiBuilder)}
   * <p>
   * If you don't care to distinguish PSI nodes by type, it is
   * sufficient to create a {@link ANTLRPsiNode} around
   * the parse tree node
   */
  @Override public @NotNull PsiElement createElement(@NotNull ASTNode node) {
    return new AyaPsiElement(node);
  }

  @Override public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override public @NotNull TokenSet getWhitespaceTokens() {
    return WHITESPACE;
  }

  @Override public @NotNull TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @Override public @NotNull TokenSet getStringLiteralElements() {
    return STRING;
  }

  @Override public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }
}
