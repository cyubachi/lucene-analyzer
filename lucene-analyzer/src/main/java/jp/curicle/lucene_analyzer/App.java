package jp.curicle.lucene_analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * Hello world!
 * 
 */
public class App {
  public static void main(String[] argv) throws IOException, InterruptedException{
    Version version = Version.LUCENE_46;
    Runtime r = Runtime.getRuntime();
    Process p = r.exec("pwd");
    p.waitFor();
    InputStream o = p.getInputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(o));
    String line = br.readLine();
    System.out.println(line);
    Directory d = FSDirectory.open(new File("./indexDir"));
    Document doc = new Document();
    Field id = new StringField("id", "cyubachi", Field.Store.YES);
    Field text = new TextField("text", "今日は晴れです", Field.Store.YES);
    doc.add(id);
    doc.add(text);
  
    Analyzer myAnalyzer = new MyAnalyzer(version);

    IndexWriterConfig iwc = new IndexWriterConfig(version, myAnalyzer);
    IndexWriter iw = new IndexWriter(d, iwc);
    iw.addDocument(doc);
    iw.commit();
    iw.close();

  }
  
}
