package org.aya.intellij.lsp;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.tree.IElementType;
import kala.collection.Seq;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Either;
import org.aya.cli.parse.AyaGKProducer;
import org.aya.concrete.Expr;
import org.aya.concrete.GenericAyaParser;
import org.aya.concrete.stmt.Stmt;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.utils.AyaPsiFactory;
import org.aya.parser.GenericNode;
import org.aya.util.error.SourceFile;
import org.aya.util.error.SourcePos;
import org.aya.util.reporter.Reporter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record AyaIJParserImpl(@NotNull Project project, @NotNull Reporter reporter) implements GenericAyaParser {
  @Override public @NotNull Expr expr(@NotNull String code, @NotNull SourcePos overridingSourcePos) {
    var producer = new AyaGKProducer(Either.right(overridingSourcePos), reporter);
    var expr = (AyaPsiElement) AyaPsiFactory.expr(project, code);
    return producer.expr(new ASTGenericNode(expr.getNode()));
  }

  @Override public @NotNull ImmutableSeq<Stmt> program(@NotNull SourceFile sourceFile) {
    return ApplicationManager.getApplication().runReadAction((ThrowableComputable<? extends ImmutableSeq<Stmt>, ? extends RuntimeException>) () -> {
      var psiFile = JB.fileAt(project, sourceFile).getOrNull();
      if (!(psiFile instanceof AyaPsiFile ayaFile))
        throw new IllegalArgumentException("File not found in IntelliJ documents: " + sourceFile);
      var producer = new AyaGKProducer(Either.left(sourceFile), reporter);
      return producer.program(new ASTGenericNode(ayaFile.getNode())).getLeftValue();
    });
  }

  public record ASTGenericNode(@NotNull ASTNode node) implements GenericNode<ASTGenericNode> {
    @Override public @NotNull IElementType elementType() {
      return node.getElementType();
    }

    @Override public @NotNull String tokenText() {
      return node.getText();
    }

    @Override public @NotNull TextRange range() {
      return node.getTextRange();
    }

    @Override public boolean isTerminalNode() {
      return false;
    }

    @Override public @NotNull SeqView<ASTGenericNode> childrenView() {
      return Seq.wrapJava(Arrays.asList(node.getChildren(null))).view().map(ASTGenericNode::new);
    }
  }
}
