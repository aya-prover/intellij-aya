package org.aya.intellij.lsp;

import kala.collection.immutable.ImmutableSeq;
import org.aya.concrete.Expr;
import org.aya.concrete.GenericAyaParser;
import org.aya.concrete.stmt.Stmt;
import org.aya.util.error.SourceFile;
import org.aya.util.error.SourcePos;
import org.aya.util.reporter.Reporter;
import org.jetbrains.annotations.NotNull;

public record AyaIJParserImpl(@NotNull Reporter reporter) implements GenericAyaParser {
  @Override public @NotNull Expr expr(@NotNull String code, @NotNull SourcePos overridingSourcePos) {
    return new Expr.LitStringExpr(overridingSourcePos, "TODO");
  }

  @Override public @NotNull ImmutableSeq<Stmt> program(@NotNull SourceFile sourceFile) {
    return ImmutableSeq.empty();
  }
}
