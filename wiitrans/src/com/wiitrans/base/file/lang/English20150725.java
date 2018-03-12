package com.wiitrans.base.file.lang;

import java.util.ArrayList;

import org.apache.xmlbeans.impl.xb.xsdschema.WhiteSpaceDocument.WhiteSpace;

import com.sun.swing.internal.plaf.basic.resources.basic;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

/*
 public class English20150725 extends Language {
 private LANGUAGE_NAME _name = LANGUAGE_NAME.ENGLISH;
 private LANGUAGE_TYPE _type = LANGUAGE_TYPE.LETTER;

 private ArrayList<Character> ignoreList;
 private ArrayList<Character> symbolForSentence;
 private ArrayList<Character> spaceListForWord;
 private ArrayList<Character> endList;

 public English20150725() {
 ignoreList = new ArrayList<Character>();
 symbolForSentence = new ArrayList<Character>();
 spaceListForWord = new ArrayList<Character>();
 endList = new ArrayList<Character>();

 ignoreList.add(new Character('☂'));
 symbolForSentence.add(new Character('.'));
 symbolForSentence.add(new Character('!'));
 symbolForSentence.add(new Character('?'));
 spaceListForWord.add(new Character(','));
 spaceListForWord.add(new Character(';'));
 spaceListForWord.add(new Character('"'));
 spaceListForWord.add(new Character('('));
 spaceListForWord.add(new Character(')'));
 spaceListForWord.add(new Character('['));
 spaceListForWord.add(new Character(']'));
 spaceListForWord.add(new Character('{'));
 spaceListForWord.add(new Character('}'));
 spaceListForWord.add(new Character(':'));
 spaceListForWord.add(new Character('&'));
 spaceListForWord.add(new Character('='));
 spaceListForWord.add(new Character('/'));
 spaceListForWord.add(new Character('_'));
 spaceListForWord.add(new Character('.'));
 spaceListForWord.add(new Character('!'));
 spaceListForWord.add(new Character('?'));
 endList.add(new Character(' '));
 endList.add(new Character('\r'));
 endList.add(new Character('\n'));
 endList.add(new Character('\0'));
 }

 public English20150725(ArrayList<Character> ignoreList,
 ArrayList<Character> symbolForSentence,
 ArrayList<Character> spaceListForWord, ArrayList<Character> endList) {
 this.ignoreList = ignoreList;
 this.symbolForSentence = symbolForSentence;
 this.spaceListForWord = spaceListForWord;
 this.endList = endList;
 }

 @Override
 public LANGUAGE_NAME GetName() {
 // TODO Auto-generated method stub
 return _name;
 }

 @Override
 public LANGUAGE_TYPE GetType() {
 // TODO Auto-generated method stub
 return _type;
 }

 @Override
 public ArrayList<String> AnalyseSentence(String text) {
 // TODO Auto-generated method stub
 if (text == null || text.isEmpty()) {
 return new ArrayList<String>();
 }
 ArrayList<String> sentences = new ArrayList<String>();

 StringBuilder builder = new StringBuilder();
 StringBuilder endbuilder;
 char[] chs = text.toCharArray();
 // for (char c : text.toCharArray()) {
 for (int i = 0; i < chs.length; ++i) {
 builder.append(chs[i]);
 if (symbolForSentence.contains(chs[i])) {
 endbuilder = new StringBuilder();
 int j = 0;
 while (i + ++j < chs.length) {
 if (ignoreList.contains(chs[i + j])) {
 endbuilder.append(chs[i + j]);
 continue;
 } else if (endList.contains(chs[i + j])) {
 builder.append(endbuilder).append(chs[i + j]);
 sentences.add(builder.toString());
 builder = new StringBuilder();
 i += j;
 break;
 } else {
 break;
 }
 }

 }
 }
 if (builder.length() > 0) {
 sentences.add(builder.toString());
 }

 return sentences;
 }

 @Override
 public ArrayList<Word> AnalyseWord(String sentence) {
 // TODO Auto-generated method stub

 ArrayList<Word> wordsList = new ArrayList<Word>();
 if (sentence != null) {
 for (Character c : ignoreList) {
 sentence = sentence.replace(c.toString(), "");
 }

 char[] array = sentence.toCharArray();
 for (int i = array.length - 1; i >= 0; --i) {
 if (endList.contains(array[array.length - 1])) {
 if (endList.contains(array[i])) {
 continue;
 } else if (symbolForSentence.contains(array[i])) {
 sentence = sentence.substring(0, i);
 break;
 }
 } else {
 break;
 }
 }

 for (Character c : spaceListForWord) {
 sentence = sentence.replace(c, ' ');
 }

 String[] swords = sentence.split(" ");
 Word word;
 int charindex = 0;
 int wordindex = 0;
 for (String sword : swords) {
 if (sword != null) {
 if (sword.trim().length() > 0) {
 try {
 Double.parseDouble(sword);
 } catch (Exception e) {
 word = new Word();
 word.word = sword;
 word.charindex = charindex;
 word.wordindex = wordindex++;
 wordsList.add(word);
 charindex += sword.length();
 }
 }
 // 空格占用位置
 ++charindex;
 }
 }
 }
 return wordsList;
 }

 @Override
 public int IsLetter(char c) {
 // TODO Auto-generated method stub
 return 0;
 }

 @Override
 public boolean ValidateWord(String word, int maxLen) {
 // TODO Auto-generated method stub
 return false;
 }

 @Override
 public String GetWordByHansonCode(long wordID) {
 // TODO Auto-generated method stub
 return null;
 }

 @Override
 public long GetHansonCodeByWord(String word) {
 // TODO Auto-generated method stub
 return 0;
 }

 }
 */
