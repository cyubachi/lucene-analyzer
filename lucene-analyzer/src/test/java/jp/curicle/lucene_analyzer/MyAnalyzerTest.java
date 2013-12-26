package jp.curicle.lucene_analyzer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class MyAnalyzerTest extends MyAnalyzer {

  MyAnalyzer analyzer;

  public MyAnalyzerTest() {
    super(Version.LUCENE_46);
  }

  @Before
  public void setUp() {
    Version version = Version.LUCENE_46;
    analyzer = new MyAnalyzer(version);
  }

  @Test
  public void testCreateComponents() throws IOException {
    TokenStreamComponents tsc = analyzer.createComponents("text",
        new StringReader("今日は晴れですが、明日は雨です。ABC"));
    Tokenizer tokenizer = tsc.getTokenizer();
    tokenizer.reset();
    System.out.println("*** tokenizerのテスト ***");
    System.out.println("原文：今日は晴れですが、明日は雨です。ABC");
    // Tokenizerの想定値
    List<String> expectedList = new ArrayList<String>() {
      {
        add("今日");
        add("は");
        add("晴れ");
        add("です");
        add("が");
        add("明日");
        add("は");
        add("雨");
        add("です");
        add("ABC");
      }
    };
    Iterator expectedIterator = expectedList.iterator();
    // Tokenizerは加工、除去する前のそのまま。句読点は除去
    while (tokenizer.incrementToken()) {
      String actual = tokenizer.getAttribute(CharTermAttribute.class)
          .toString();
      String expected = (String) expectedIterator.next();
      assertThat(actual, is(expected));
      System.out.println("実値:" + actual + " = 想定値:" + expected);
    }
    assertFalse(expectedIterator.hasNext());
    tokenizer.end();
    tokenizer.close();
    
    // tokenStreamはtokenizerを含むため先にtokenizerがcloseされると困るため
    // ここでもう一度createComponentsをしてnewする。
    tsc = analyzer.createComponents("text",
        new StringReader("今日は晴れですが、明日は雨です。ABC"));
    TokenStream tokenStream = tsc.getTokenStream();
    
    System.out.println("*** tokenFilterのテスト ***");
    System.out.println("原文：今日は晴れですが、明日は雨です。ABC");

    // tokenStreamはStopFilter、KatakanaStemFilter、LowerCaseFilter他が効いている
    expectedList = new ArrayList<String>() {
      {
        add("今日");
        // add("晴れ"); // 晴れはStopFilterで除去される
        add("明日");
        add("雨");
        add("abc");
      }
    };
    expectedIterator = expectedList.iterator();
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      String actual = tokenStream.getAttribute(CharTermAttribute.class)
          .toString();
      String expected = (String) expectedIterator.next();
      assertThat(actual, is(expected));
      System.out.println("実値:" + actual + " = 想定値:" + expected);
    }
    assertFalse(expectedIterator.hasNext());
    tokenStream.end();
    tokenStream.close();
    System.out.println("*** 終了 ***");

  }

}
