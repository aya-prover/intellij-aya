package org.aya.intellij.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.tree.util.AstUtilKt;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.tree.IElementType;
import kala.collection.immutable.ImmutableSeq;
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
import org.aya.syntax.GenericAyaParser;
import org.aya.syntax.concrete.Expr;
import org.aya.syntax.concrete.stmt.Stmt;
import org.aya.util.error.SourceFile;
import org.aya.util.error.SourcePos;
import org.aya.util.error.WithPos;
import org.aya.util.reporter.Reporter;
import org.jetbrains.annotations.NotNull;

public record AyaIJParserImpl(@NotNull Project project, @NotNull Reporter reporter) implements GenericAyaParser {
  @Override public @NotNull WithPos<Expr> expr(@NotNull String code, @NotNull SourcePos overridingSourcePos) {
    var producer = new AyaProducer(Either.right(overridingSourcePos), reporter);
    var expr = (AyaPsiElement) AyaPsiFactory.expr(project, code);
    return producer.expr(new ASTGenericNode(expr.getNode()));
  }

  @Override
  public @NotNull ImmutableSeq<Stmt> program(@NotNull SourceFile codeFile, @NotNull SourceFile originalFile) {
    return ApplicationManager.getApplication().runReadAction((ThrowableComputable<? extends ImmutableSeq<Stmt>, ? extends RuntimeException>) () -> {
      var psiFile = JB.fileAt(project, codeFile).getOrNull();
      if (!(psiFile instanceof AyaPsiFile ayaFile))
        throw new IllegalArgumentException("File not found in IntelliJ documents: " + codeFile.display());
      // TODO: support literate mode
      var code = ayaFile.getText();
      var updated = new SourceFile(codeFile.display(), codeFile.underlying(), code);
      var producer = new AyaProducer(Either.left(updated), reporter);
      return producer.program(new ASTGenericNode(ayaFile.getNode())).getLeftValue();
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

    @Override public @NotNull Sequence<ASTGenericNode> childrenSequence() {
      return SequencesKt.map(AstUtilKt.children(node), ASTGenericNode::new);
    }
  }
}
