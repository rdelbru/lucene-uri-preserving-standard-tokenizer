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
 * @author Renaud Delbru [ 20 Apr 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.solr.plugins.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Token;
import org.junit.Test;

public class CustomStandardTokenizerTest {

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.CustomStandardTokenizerTest#CustomStandardTokenizer(java.io.Reader)}.
   * @throws IOException
   */
  @Test
  public void testCustomStandardTokenizer() throws IOException {
    final StringReader reader = new StringReader("Some Text with Acronym I.B.M. and email renaud@delbru.fr and web address http://renaud.delbru.fr/ ");
    final CustomStandardTokenizer tokenStream = new CustomStandardTokenizer(reader);

    Token token = new Token();
    while ((token = tokenStream.next(token)) != null) {
      //System.out.println(token.termBuffer());
      //System.out.println(token.type());
      System.out.println(token);
    }
    reader.close();
  }

  @Test
  public void testDashedURIParsing() throws Exception {
    final StringReader reader = new StringReader("hello http://semantic-conference.com world");
    final CustomStandardTokenizer tokenStream = new CustomStandardTokenizer(reader);

    Token token = tokenStream.next();
    assertEquals("hello", tokenString(token));
    token = tokenStream.next();
    assertEquals("http://semantic-conference.com", tokenString(token));
    assertEquals(CustomStandardTokenizerImpl.TOKEN_TYPES[CustomStandardTokenizer.URI], token.type());
    token = tokenStream.next();
    assertEquals("world", tokenString(token));
    token = tokenStream.next();
    assertNull(token);
  }

  @Test
  public void testColons() throws Exception {
    final CustomStandardTokenizer tokenStream = new CustomStandardTokenizer(new StringReader("hello:world"));

    Token token = tokenStream.next();
    assertEquals("hello:world", tokenString(token));
    assertEquals(CustomStandardTokenizerImpl.TOKEN_TYPES[CustomStandardTokenizer.URI], token.type());
    token = tokenStream.next();
    assertNull(token);
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.CustomStandardTokenizerTest#CustomStandardTokenizer(java.io.Reader)}.
   * @throws IOException
   */
  @Test
  public void testCustomStandardTokenizerURI() throws IOException {
    final String[] uris = {
        "http://renaud.delbru.fr/",
        "http://renaud.delbru.fr",
        "http://user@renaud.delbru.fr",
        "http://user:passwd@renaud.delbru.fr",
        "http://user:passwd@renaud.delbru.fr:8080",
        "http://renaud.delbru.fr:8080",
        "http://renaud.delbru.fr/page.html",
        "http://renaud.delbru.fr/subdir/page.html",
        "http://renaud.delbru.fr/subdir/",
        "http://renaud.delbru.fr/subdir",
        "http://renaud.delbru.fr/page.html#fragment",
        "http://renaud.delbru.fr/page.html?query=a+query&hl=en&start=20&sa=N",
        "https://renaud.delbru.fr/",
        "ftp://renaud.delbru.fr/",
        "mailto:renaud@delbru.fr",
        "http://example.com/New%20File.txt"
    };

    for (final String uri : uris) {
      final StringReader reader = new StringReader(uri);
      final CustomStandardTokenizer tokenStream = new CustomStandardTokenizer(reader);

      Token token = new Token();
      while ((token = tokenStream.next(token)) != null) {
        assertEquals(token.type(), CustomStandardTokenizerImpl.TOKEN_TYPES[CustomStandardTokenizer.URI]);
        assertEquals(new String(token.termBuffer(), 0, token.termLength()), uri);
        System.out.println(token);
      }
    }
  }

  private static String tokenString(final Token token) {
    return new String(token.termBuffer(), 0, token.termLength());
  }
}
