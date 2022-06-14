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
import org.aya.intellij.psi.AyaPsiElementTypes;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaParserDefinition extends SyntaxHighlighterFactory implements ParserDefinition {
  private final @NotNull IFileElementType FILE = new IFileElementType(AyaLanguage.INSTANCE);
  public static final @NotNull AyaPsiTokenType LINE_COMMENT = new AyaPsiTokenType("LINE_COMMENT");
  public static final @NotNull AyaPsiTokenType BLOCK_COMMENT = new AyaPsiTokenType("BLOCK_COMMENT");
  public static final @NotNull TokenSet COMMENTS = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT, AyaPsiElementTypes.DOC_COMMENT);
  public static final @NotNull TokenSet STRINGS = TokenSet.create(AyaPsiElementTypes.STRING);

  public AyaParserDefinition() {
  }

  @Override
  public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
    return new SyntaxHighlight();
  }

  @Override public @NotNull Lexer createLexer(Project project) {
    return new FlexAdapter(new _AyaPsiLexer());
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
}
