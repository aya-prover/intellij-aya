package org.aya.intellij.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.tree.util.AstUtilKt;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.tree.IElementType;
import kala.control.Either;
import kala.text.StringSlice;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import org.aya.intellij.GenericNode;
import org.aya.intellij.actions.lsp.JB;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.utils.AyaPsiFactory;
import org.aya.producer.AyaProducer;
import org.aya.producer.NodedAyaProgram;
import org.aya.syntax.GenericAyaParser;
import org.aya.syntax.GenericAyaProgram;
import org.aya.syntax.concrete.Expr;
import org.aya.util.position.SourceFile;
import org.aya.util.position.SourcePos;
import org.aya.util.position.WithPos;
import org.aya.util.reporter.Reporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AyaIJParserImpl(@NotNull Project project, @NotNull Reporter reporter) implements GenericAyaParser {
  @Override public @NotNull WithPos<Expr> expr(@NotNull String code, @NotNull SourcePos overridingSourcePos) {
    var producer = new AyaProducer(Either.right(overridingSourcePos), reporter);
    var expr = (AyaPsiElement) AyaPsiFactory.expr(project, code);
    return producer.expr(new ASTGenericNode(expr.getNode()));
  }

  @Override
  public @NotNull GenericAyaProgram program(@NotNull SourceFile codeFile, @NotNull SourceFile originalFile) {
    return ApplicationManager.getApplication().runReadAction((ThrowableComputable<? extends GenericAyaProgram, ? extends RuntimeException>) () -> {
      var psiFile = JB.fileAt(project, codeFile).getOrNull();
      if (!(psiFile instanceof AyaPsiFile ayaFile))
        throw new IllegalArgumentException("File not found in IntelliJ documents: " + codeFile.display());
      // TODO: support literate mode
      var code = ayaFile.getText();
      var updated = new SourceFile(codeFile.display(), codeFile.underlying(), code);
      var producer = new AyaProducer(Either.left(updated), reporter);
      var root = new ASTGenericNode(ayaFile.getNode());
      return new NodedAyaProgram(producer.program(root).getLeftValue(), root);
    });
  }

  public record ASTGenericNode(@NotNull ASTNode node) implements GenericNode<ASTGenericNode> {
    @Override public @NotNull IElementType elementType() {
      return node.getElementType();
    }

    @Override public @NotNull StringSlice tokenText() {
      return StringSlice.of(node.getText());
    }

    @Override public @NotNull TextRange range() {
      return node.getTextRange();
    }

    @Override
    public @Nullable ASTGenericNode parent() {
      var parent = node.getTreeParent();
      if (parent == null) return null;
      return new ASTGenericNode(parent);
    }

    @Override public @NotNull Sequence<ASTGenericNode> childrenSequence() {
      return SequencesKt.map(AstUtilKt.children(node), ASTGenericNode::new);
    }
  }
}
