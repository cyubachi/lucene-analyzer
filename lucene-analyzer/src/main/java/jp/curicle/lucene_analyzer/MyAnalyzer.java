package jp.curicle.lucene_analyzer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKWidthFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ja.JapaneseBaseFormFilter;
import org.apache.lucene.analysis.ja.JapanesePartOfSpeechStopFilter;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

public class MyAnalyzer extends Analyzer {
  
  Version version;
  
  public MyAnalyzer(Version version) {
    super();
    this.version = version;
  }

  @Override
  protected TokenStreamComponents createComponents(String FieldName,
      Reader reader) {
    // 形態素解析用Tokenizer
    Tokenizer source = new JapaneseTokenizer(reader, // 入力
        null, // ユーザー辞書なし
        true, // 句読点を削る
        JapaneseTokenizer.Mode.SEARCH); // 検索用トークナイズモード

    // 基本形マッチ用フィルター
    TokenStream filter = new JapaneseBaseFormFilter(source);

    // 要らない品詞のタグのSetを作成
    Set<String> stopTagSet = new HashSet<String>();
    try {
      ResourceLoader loader = new ClasspathResourceLoader(
          JapaneseTokenizer.class);
      List<String> tagList = WordlistLoader.getLines(
          loader.openResource("stoptags.txt"), IOUtils.CHARSET_UTF_8);
      for (String tag : tagList) {
        stopTagSet.add(tag);
      }
    } catch (IOException ex) {
      Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
    }

    // 不要な品詞の除去フィルター
    filter = new JapanesePartOfSpeechStopFilter(version, filter,
        stopTagSet);
    // 半角カナ→全角カナその他変換フィルター
    filter = new CJKWidthFilter(filter);
    try {
      ResourceLoader loader = new ClasspathResourceLoader(
          App.class.getClassLoader());
      CharArraySet stopWords = WordlistLoader.getWordSet(new InputStreamReader(
          loader.openResource("stopwords.txt")), version);
      // 禁止ワードフィルター
      filter = new StopFilter(version, filter, stopWords);
    } catch (IOException ex) {
      Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    filter = new LowerCaseFilter(version, filter);
    return new TokenStreamComponents(source, filter);
  }

}