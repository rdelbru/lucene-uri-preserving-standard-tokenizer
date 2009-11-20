/**
 * Copyright 2009, Renaud Delbru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * @project siren
 * @author Renaud Delbru [ 20 Nov 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.solr.plugins.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/** A grammar-based tokenizer constructed with JFlex
 *
 * <p> This should be a good tokenizer for most European-language documents:
 *
 * <ul>
 *   <li>Splits words at punctuation characters, removing punctuation. However, a 
 *     dot that's not followed by whitespace is considered part of a token.
 *   <li>Splits words at hyphens, unless there's a number in the token, in which case
 *     the whole token is interpreted as a product number and is not split.
 *   <li>Recognizes email addresses and URI (using grammar from RFC3986) as one token.
 * </ul>
 *
 * This code is based on the StandardAnalyzer of Lucene 2.3. The deprecated
 * invalid acronym case has been removed, and the detection of HOST address has 
 * been replaced by the detection of absolute URI.
 */

public class CustomStandardTokenizer extends Tokenizer {

  /** A private instance of the JFlex-constructed scanner */
  private final CustomStandardTokenizerImpl scanner;

  public static final int ALPHANUM          = 0;
  public static final int APOSTROPHE        = 1;
  public static final int ACRONYM           = 2;
  public static final int COMPANY           = 3;
  public static final int EMAIL             = 4;
  public static final int URI               = 5;
  public static final int NUM               = 6;
  public static final int CJ                = 7;
  
  public static final String getTokenType(int type) {
    return CustomStandardTokenizerImpl.TOKEN_TYPES[type];
  }

  void setInput(Reader reader) {
    this.input = reader;
  }

  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /** Set the max allowed token length.  Any token longer
   *  than this is skipped. */
  public void setMaxTokenLength(int length) {
    this.maxTokenLength = length;
  }

  /** @see #setMaxTokenLength */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  /**
   * Creates a new instance of the {@link CustomStandardTokenizer}. Attaches the
   * <code>input</code> to a newly created JFlex scanner.
   */
  public CustomStandardTokenizer(Reader input) {
    this.input = input;
    this.scanner = new CustomStandardTokenizerImpl(input);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#next()
   */
  public Token next(Token result) throws IOException {
    int posIncr = 1;

    while(true) {
      int tokenType = scanner.getNextToken();

      if (tokenType == CustomStandardTokenizerImpl.YYEOF) {
        return null;
      }

      if (scanner.yylength() <= maxTokenLength) {
        result.clear();
        result.setPositionIncrement(posIncr);
        scanner.getText(result);
        final int start = scanner.yychar();
        result.setStartOffset(start);
        result.setEndOffset(start+result.termLength());
        result.setType(CustomStandardTokenizerImpl.TOKEN_TYPES[tokenType]);
        return result;
      } else
        // When we skip a too-long term, we still increment the
        // position increment
        posIncr++;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#reset()
   */
  public void reset() throws IOException {
    super.reset();
    scanner.yyreset(input);
  }

  public void reset(Reader reader) throws IOException {
    input = reader;
    reset();
  }
}
