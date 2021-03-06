package org.sindice.solr.plugins.analysis;

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

import org.apache.lucene.analysis.Token;

%%

%class CustomStandardTokenizerImpl
%unicode
%integer
%function getNextToken
%pack
%char

%{

public static final int ALPHANUM          = CustomStandardTokenizer.ALPHANUM;
public static final int APOSTROPHE        = CustomStandardTokenizer.APOSTROPHE;
public static final int ACRONYM           = CustomStandardTokenizer.ACRONYM;
public static final int COMPANY           = CustomStandardTokenizer.COMPANY;
public static final int EMAIL             = CustomStandardTokenizer.EMAIL;
public static final int URI             	= CustomStandardTokenizer.URI;
public static final int NUM               = CustomStandardTokenizer.NUM;
public static final int CJ                = CustomStandardTokenizer.CJ;

public static final String [] TOKEN_TYPES = new String [] {
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<COMPANY>",
    "<EMAIL>",
    "<URI>",
    "<NUM>",
    "<CJ>"
};

public final int yychar()
{
    return yychar;
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(Token t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}
%}

// basic word: a sequence of digits & letters
ALPHANUMBASE = {LETTER}|{DIGIT}|{KOREAN} 
ALPHANUM   = {ALPHANUMBASE}+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possesives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+

// acronyms: U.S.A., I.B.M., etc.
// use a post-filter to remove dots
ACRONYM    =  {LETTER} "." ({LETTER} ".")+

// company names like AT&T and Excite@Home.
COMPANY    =  {ALPHA} ("&"|"@") {ALPHA}

// email addresses
EMAIL      =  {ALPHANUM} (("."|"-"|"_") {ALPHANUM})* "@" {ALPHANUM} (("."|"-") {ALPHANUM})+

// Absolute URI (Partial BNF from RFC3986)
URI      	 =  {ALPHA}+ ":" "//"? {AUTHORITY} {PATH} ("?" {QUERY})? ("#" {FRAGMENT})?
AUTHORITY	 =  ({USERINFO} "@")? {HOST} (":" {PORT})?
QUERY			 =  ({SEGMENT}|"/"|"?")*
FRAGMENT   =  ({SEGMENT}|"/"|"?")*
USERINFO	 =  {USERNAME} (":" {PASSWD})?
USERNAME   =  {UNRESERVED}+
PASSWD		 =  ({UNRESERVED}|":"|{SUBDELIMS})+
HOST       =  {DOMAINLABEL} ("." {DOMAINLABEL})*
DOMAINLABEL = {ALPHANUMBASE} (("-" | {ALPHANUMBASE})* {ALPHANUMBASE})?
PORT			 =  {DIGIT}+
PATH			 =  ("/" {SEGMENT})*
SEGMENT    =  ({UNRESERVED}|{PCT_ENCODED}|{SUBDELIMS}|":"|"@")*
UNRESERVED =  ({ALPHANUM}|"-"|"."|"_"|"~")
SUBDELIMS  =  ("!"|"$"|"&"|"'"|"("|")"|"*"|"+"|","|";"|"=")
PCT_ENCODED = "%" {HEXDIG} {HEXDIG}

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

// punctuation
P	         = ("_"|"-"|"/"|"."|",")

// at least one digit
HAS_DIGIT  =
    ({LETTER}|{DIGIT})*
    {DIGIT}
    ({LETTER}|{DIGIT})*

ALPHA      = ({LETTER})+


LETTER     = [\u0041-\u005a\u0061-\u007a\u00c0-\u00d6\u00d8-\u00f6\u00f8-\u00ff\u0100-\u1fff\uffa0-\uffdc]

DIGIT      = [\u0030-\u0039\u0660-\u0669\u06f0-\u06f9\u0966-\u096f\u09e6-\u09ef\u0a66-\u0a6f\u0ae6-\u0aef\u0b66-\u0b6f\u0be7-\u0bef\u0c66-\u0c6f\u0ce6-\u0cef\u0d66-\u0d6f\u0e50-\u0e59\u0ed0-\u0ed9\u1040-\u1049]

HEXDIG     = ({DIGIT}|"A"|"B"|"C"|"D"|"E"|"F"|"a"|"b"|"c"|"d"|"e"|"f")

KOREAN     = [\uac00-\ud7af\u1100-\u11ff]

// Chinese, Japanese
CJ         = [\u3040-\u318f\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

WHITESPACE = \r\n | [ \r\n\t\f]

%%

{ALPHANUM}                                                     { return ALPHANUM; }
{APOSTROPHE}                                                   { return APOSTROPHE; }
{ACRONYM}                                                      { return ACRONYM; }
{COMPANY}                                                      { return COMPANY; }
{EMAIL}                                                        { return EMAIL; }
{URI}                                                          { return URI; }
{NUM}                                                          { return NUM; }
{CJ}                                                           { return CJ; }

/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
