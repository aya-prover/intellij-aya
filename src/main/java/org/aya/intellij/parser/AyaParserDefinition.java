package org.aya.intellij.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.aya.intellij.AyaLanguage;
import org.aya.intellij.actions.SyntaxHighlight;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.types.AyaPsiElementTypes;
import org.aya.intellij.psi.types.AyaPsiTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaParserDefinition extends SyntaxHighlighterFactory implements ParserDefinition {
  private final @NotNull IFileElementType FILE = new IFileElementType(AyaLanguage.INSTANCE);

  public static @NotNull Lexer createLexer() {
    return new FlexAdapter(new _AyaPsiLexer(null));
  }

  @Override
  public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
    return new SyntaxHighlight();
  }

  @Override public @NotNull Lexer createLexer(Project project) {
    return createLexer();
  }

  @Override public @NotNull PsiParser createParser(Project project) {
    return new AyaPsiParser();
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

  @Override public @NotNull PsiElement createElement(@NotNull ASTNode node) {
    return AyaPsiElementTypes.Factory.createElement(node);
  }

  @Override public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override public @NotNull TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @Override public @NotNull TokenSet getStringLiteralElements() {
    return STRINGS;
  }

  @Override public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  public static final @NotNull AyaPsiTokenType LINE_COMMENT = new AyaPsiTokenType("LINE_COMMENT");
  public static final @NotNull AyaPsiTokenType BLOCK_COMMENT = new AyaPsiTokenType("BLOCK_COMMENT");
  public static final @NotNull TokenSet COMMENTS = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT, AyaPsiElementTypes.DOC_COMMENT);
  public static final @NotNull TokenSet STRINGS = TokenSet.create(AyaPsiElementTypes.STRING);
  public static final @NotNull TokenSet MARKERS = TokenSet.create(
    AyaPsiElementTypes.COLON,
    AyaPsiElementTypes.DEFINE_AS,
    AyaPsiElementTypes.TO,
    AyaPsiElementTypes.BAR,
    AyaPsiElementTypes.IMPLIES,
    AyaPsiElementTypes.LARROW,
    AyaPsiElementTypes.SUCHTHAT
  );
  public static final @NotNull TokenSet KEYWORDS = TokenSet.create(
    AyaPsiElementTypes.KW_AS,
    AyaPsiElementTypes.KW_BIND,
    AyaPsiElementTypes.KW_CODATA,
    AyaPsiElementTypes.KW_COERCE,
    AyaPsiElementTypes.KW_COMPLETED,
    AyaPsiElementTypes.KW_COUNTEREXAMPLE,
    AyaPsiElementTypes.KW_DATA,
    AyaPsiElementTypes.KW_DEF,
    AyaPsiElementTypes.KW_DO,
    AyaPsiElementTypes.KW_EXAMPLE,
    AyaPsiElementTypes.KW_EXTENDS,
    AyaPsiElementTypes.KW_FORALL,
    AyaPsiElementTypes.KW_HIDING,
    AyaPsiElementTypes.KW_IMPORT,
    AyaPsiElementTypes.KW_IN,
    AyaPsiElementTypes.KW_INFIX,
    AyaPsiElementTypes.KW_INFIXL,
    AyaPsiElementTypes.KW_INFIXR,
    AyaPsiElementTypes.KW_INLINE,
    AyaPsiElementTypes.KW_INTERVAL,
    AyaPsiElementTypes.KW_LAMBDA,
    AyaPsiElementTypes.KW_LAND,
    AyaPsiElementTypes.KW_LET,
    AyaPsiElementTypes.KW_LOOSER,
    AyaPsiElementTypes.KW_LOR,
    AyaPsiElementTypes.KW_MATCH,
    AyaPsiElementTypes.KW_MODULE,
    AyaPsiElementTypes.KW_NEW,
    AyaPsiElementTypes.KW_OPAQUE,
    AyaPsiElementTypes.KW_OPEN,
    AyaPsiElementTypes.KW_OVERLAP,
    AyaPsiElementTypes.KW_PATTERN,
    AyaPsiElementTypes.KW_PI,
    AyaPsiElementTypes.KW_PRIM,
    AyaPsiElementTypes.KW_PRIVATE,
    AyaPsiElementTypes.KW_PUBLIC,
    AyaPsiElementTypes.KW_SIGMA,
    AyaPsiElementTypes.KW_STRUCT,
    AyaPsiElementTypes.KW_TIGHTER,
    AyaPsiElementTypes.KW_TYPE,
    AyaPsiElementTypes.KW_ULIFT,
    AyaPsiElementTypes.KW_USING,
    AyaPsiElementTypes.KW_VARIABLE
  );
}
