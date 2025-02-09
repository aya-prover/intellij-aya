/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aya.intellij.language;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordOccurrence;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * Adapted from {@link com.intellij.lang.cacheBuilder.DefaultWordsScanner}
 * for supporting identifier rules in Aya.
 */
public class AyaWordsScanner extends DefaultWordsScanner {
  private final @NotNull Lexer myLexer;
  private final @NotNull TokenSet myIdentifierTokenSet;
  private final @NotNull TokenSet myCommentTokenSet;
  private final @NotNull TokenSet myLiteralTokenSet;

  /**
   * Creates a new instance of the words scanner.
   *
   * @param lexer   the lexer used for breaking the text into tokens.
   * @param id      the set of token types which represent identifiers.
   * @param comment the set of token types which represent comment.
   * @param literal the set of token types which represent literals.
   */
  public AyaWordsScanner(@NotNull Lexer lexer, @NotNull TokenSet id, @NotNull TokenSet comment, @NotNull TokenSet literal) {
    super(lexer, id, comment, literal);
    myLexer = lexer;
    myIdentifierTokenSet = id;
    myCommentTokenSet = comment;
    myLiteralTokenSet = literal;
  }

  @Override
  public void processWords(@NotNull CharSequence fileText, @NotNull Processor<? super WordOccurrence> processor) {
    myLexer.start(fileText);
    WordOccurrence occurrence = new WordOccurrence(fileText, 0, 0, null); // shared occurrence

    IElementType type;
    while ((type = myLexer.getTokenType()) != null) {

      if (myIdentifierTokenSet.contains(type)) {
        occurrence.init(fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.CODE);
        if (!processor.process(occurrence)) return;
        if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.CODE, occurrence))
          return;
      } else if (myCommentTokenSet.contains(type)) {
        if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.COMMENTS, occurrence))
          return;
      } else if (myLiteralTokenSet.contains(type)) {
        if (!stripWords(processor, fileText, myLexer.getTokenStart(), myLexer.getTokenEnd(), WordOccurrence.Kind.LITERALS, occurrence))
          return;
      }
      myLexer.advance();
    }
  }

  public static boolean stripWords(
    @NotNull Processor<? super WordOccurrence> processor,
    @NotNull CharSequence text, int from, int to,
    @NotNull WordOccurrence.Kind kind,
    @NotNull WordOccurrence occur
  ) {
    return stripWords(processor, text, from, to, kind, occur, false);
  }
}
