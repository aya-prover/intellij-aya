package org.aya.intellij.parser;

import com.intellij.lang.ASTNode;
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
import org.aya.intellij.actions.SyntaxHighlight;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.utils.AyaPsiElementFactory;
import org.aya.parser.AyaLanguage;
import org.aya.parser.AyaParserDefinitionBase;
import org.aya.parser._AyaPsiLexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AyaParserDefinition extends AyaParserDefinitionBase {
  public static @NotNull Lexer createIJLexer() {
    return new FlexAdapter(new _AyaPsiLexer(false));
  }

  public static final class SyntaxHighlightFactory extends SyntaxHighlighterFactory {
    @Override
    public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
      return new SyntaxHighlight();
    }
  }
  private final @NotNull IFileElementType FILE = new IFileElementType(AyaLanguage.INSTANCE);

  @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new AyaPsiFile(viewProvider);
  }

  @Override public @NotNull PsiElement createElement(@NotNull ASTNode node) {
    return AyaPsiElementFactory.createElement(node);
  }

  @Override public @NotNull TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @Override public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }
}
