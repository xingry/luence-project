package cn.xingry.luence;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKQueryParser;
import org.wltea.analyzer.lucene.IKSimilarity;

/**
 * 对本地文件夹中文件进行检索
 * 
 * @author xingruyi
 * 
 */
public class LocalFileIndex {

	public static void main(String[] args) throws IOException {
		buildIndex();
		searchIK();
	}

	/**
	 * 为本地目录创建索引
	 * 
	 * @throws IOException
	 */
	public static void buildIndex() throws IOException {

		long startTime = new Date().getTime();

		// 索引存放目录
		File indexDir = new File("/home/xingruyi/.luence/");

		// 被检索目录
		File dataDir = new File("/home/xingruyi/workbench/mynote/");

		// 创建词法分析器（使用Luence内置）
		Analyzer luceneAnalyzer = new StandardAnalyzer();

		// IndexWriter用于将Document添加至索引库中
		// 第三个参数true表示创建新索引，false表示在原索引上操作。
		IndexWriter indexWriter = new IndexWriter(indexDir, luceneAnalyzer,
				true);

		// 遍历所有文件
		File[] dataFiles = dataDir.listFiles();
		for (int i = 0; i < dataFiles.length; i++) {
			// 只为.txt文件创建索引
			if (dataFiles[i].isFile()
					&& dataFiles[i].getName().endsWith(".txt")) {
				Document document = new Document();
				Reader reader = new FileReader(dataFiles[i]);
				document.add(Field.Text("path", dataFiles[i].getCanonicalPath()));
				document.add(Field.Text("contents", reader));
				indexWriter.addDocument(document);
			}
		}
		indexWriter.optimize();
		indexWriter.close();

		long endTime = new Date().getTime();

		System.out.println(dataDir.getPath() + " 已建立索引， 用时 "
				+ (endTime - startTime) + "毫秒");

	}

	/**
	 * 检索,使用Lunce内置分词
	 * 
	 * @throws IOException
	 */
	public static void search() throws IOException {
		String queryStr = "java";

		// 索引存放目录
		File indexDir = new File("/home/xingruyi/.luence/");
		if (!indexDir.exists()) {
			return;
		}

		FSDirectory directory = FSDirectory.getDirectory(indexDir, false);

		// IndexSearcher 是用来在建立好的索引上进行搜索的。它只能以只读的方式打开一个索引，所以可以有多个 IndexSearcher
		// 的实例在一个索引上进行操作。
		IndexSearcher indexSearcher = new IndexSearcher(directory);

		Term term = new Term("contents", queryStr.toLowerCase());

		TermQuery luenceQuery = new TermQuery(term);

		// Hits 是用来保存搜索的结果的。
		Hits hits = indexSearcher.search(luenceQuery);

		for (int i = 0; i < hits.length(); i++) {
			Document document = hits.doc(i);
			System.out.println("Found : " + document.get("path"));
		}
	}

	/**
	 * 搜索（使用IK分词器）
	 */
	public static void searchIK() throws IOException {
		//Luence domain的域名
		String fieldName ="text";
		//检索的关键字
		String queryStr = "java";
		// 索引存放目录
		File indexDir = new File("/home/xingruyi/.luence/");
		if (!indexDir.exists()) {
			return;
		}
		//创建IK分词器
		IKAnalyzer ikAnalyzer = new IKAnalyzer();
		FSDirectory directory = FSDirectory.getDirectory(indexDir, false);
		IndexSearcher indexSearcher = new IndexSearcher(directory);
		//在索引器中设置IKSimilarity相似度评估器
		indexSearcher.setSimilarity(new IKSimilarity());
		
		Query query = IKQueryParser.parse(fieldName,queryStr);
		
		//获取相似度最高的5条记录
		TopDocs topDocs = indexSearcher.search(query,null,5);
		System.out.println("命中：" + topDocs.totalHits);
		
		//输出结果
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for(int i=0;i<topDocs.totalHits; i++) {
			Document targetDoc = indexSearcher.doc(scoreDocs[i].doc);
			System.out.println("内容：" + targetDoc.toString());
			
		}
		
		
	}

}
