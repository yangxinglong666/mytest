package cn.yang.lucene.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import cn.yang.lucene.dao.BookDao;
import cn.yang.lucene.dao.impl.BookDaoImpl;
import cn.yang.lucene.pojo.Book;

/*
 * Lucene的入门程序
 * 	创建索引
 * 	搜索索引
 * */
public class FirstLucene {
	
	//抽取搜索逻辑
	private void doSearch(Query query) throws IOException {
		// 2. 执行搜索，返回结果集
		// 创建Directory流对象
		Directory directory = FSDirectory.open(new File("E:\\temp\\index"));

		// 创建索引读取对象IndexReader
		IndexReader reader = DirectoryReader.open(directory);

		// 创建索引搜索对象
		IndexSearcher searcher = new IndexSearcher(reader);

		// 使用索引搜索对象，执行搜索，返回结果集TopDocs
		// 第一个参数：搜索对象，第二个参数：返回的数据条数，指定查询结果最顶部的n条数据返回
		TopDocs topDocs = searcher.search(query, 10);

		System.out.println("查询到的数据总条数是：" + topDocs.totalHits);

		// 获取查询结果集
		ScoreDoc[] docs = topDocs.scoreDocs;

		// 解析结果集
		for (ScoreDoc scoreDoc : docs) {
			// 获取文档id
			int docID = scoreDoc.doc;
			Document doc = searcher.doc(docID);

			System.out.println("======================================");

			System.out.println("docID:" + docID);
			System.out.println("bookId:" + doc.get("id"));
			System.out.println("name:" + doc.get("name"));
			System.out.println("price:" + doc.get("price"));
			System.out.println("pic:" + doc.get("pic"));
			// System.out.println("desc:" + doc.get("desc"));
		}
		// 3. 释放资源
		reader.close();
	}


	//创建索引流程
	/*   准备数据 --- 从mysql数据库中获取数据，处理并封装到pojo对象中
	 * 1.采集数据
	 *2.创建文档对象，集合存放多个文档对象
	 *3.分析文档（分词）
	 *4. 创建IndexWriterConfig配置信息类
	  5. 创建Directory对象，声明索引库存储位置
	  6. 创建IndexWriter写入对象
	  7. 把Document写入到索引库中
	  8. 释放资源
	 * */
	@Test
	public void testIndex() throws Exception {
		//采集数据
		BookDao bk = new BookDaoImpl();
		List<Book> terms = bk.queryTerms();
		
		//处理数据
		//集合存放文档对象
		List<Document> doclist = new ArrayList<>();
		for (Book book : terms) {
			//创建文档对象
			Document doc = new Document();	
			//向文档中添加Field域 ，暂时全都设置成文本域  TextField
			//Store.YES 同意保存文档
			//Store.NO 不保存文档
			//pojo对象有几个属性就添加几个域
			
			//图书id
			//不分词  不索引  要存储
			doc.add(new StoredField("id", book.getId()));
			
			doc.add(new TextField("name", book.getName(), Store.YES));
			
			//价格  分词  索引  存储
			doc.add(new FloatField("price", book.getPrice(), Store.YES));
			
			//图片   不分词  不索引  存储
			doc.add(new StoredField("pic", book.getPic()));
			
			//描述内容太长  不保存   分词  索引
			doc.add(new TextField("desc", book.getDesc(), Store.NO));
			
			//添加到集合中 
			doclist.add(doc);
		}
		
		//分析文档，分词处理 标准分析器
		//Analyzer an = new StandardAnalyzer();
		//使用中文分词器
		Analyzer an = new IKAnalyzer();
		//创建Directory对象，声明索引库存储位置
		Directory direct = FSDirectory.open(new File("E:\\temp\\index"));
		//创建IndexWriterConfig 对象  ，设置写入索引配置
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, an);
		//创建IndexWriter对象(流)，将文档写入索引库
		IndexWriter iw = new IndexWriter(direct, config);
		
		//处理文档集合
		for (Document document : doclist) {
			//创建索引
			iw.addDocument(document);
		}
		//释放资源
		iw.close();
	}
	
	/*创建搜索流程
	 *  创建QueryParser搜索解析器
	 1. 创建Query搜索对象
	 2. 创建Directory流对象,声明索引库位置
	 3. 创建索引读取对象IndexReader
	 4. 创建索引搜索对象IndexSearcher
	 5. 使用索引搜索对象，执行搜索，返回结果集TopDocs
	 6. 解析结果集
	 7. 释放资源*/
	
	@Test
	public void testFind() throws Exception{
		
		//创建分词器
		//Analyzer analyzer = new StandardAnalyzer();
		//使用中文分词器
		Analyzer an = new IKAnalyzer();
		//1. 创建QueryParser搜索解析器
		//参数1：默认Field域  参数2：分词器 
		QueryParser qp = new QueryParser("name", an);
		
		//创建Query搜索对象
		//参数：查询语法
		Query q = qp.parse("name:编程思想");
		//创建Directory流对象,声明索引库位置
		Directory direct = FSDirectory.open(new File("E:\\temp\\index"));
		
		//创建索引读取对象IndexReader，从索引库中读取索引
		IndexReader ir = DirectoryReader.open(direct);
		
		//创建索引搜索对象IndexSearcher
		IndexSearcher is = new IndexSearcher(ir);
		
		//使用索引搜索对象，执行搜索，返回结果集TopDocs，执行搜索
		//参数1：搜索对象  参数2：返回的数据条数，指定查询结果最顶部的n条数据返回
		TopDocs topDocs = is.search(q, 10);
		System.out.println("要查询的总条数是："+topDocs.totalHits);
		//解析结果集
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			//获得文档id
			int docId = scoreDoc.doc;
			//根据文档id获得对应文档
			Document document = is.doc(docId);
			
			System.out.println("------------------------");
			System.out.println("docId:"+docId);
			System.out.println("bookId:"+document.get("id"));
			System.out.println("name:"+document.get("name"));
			System.out.println("price:"+document.get("price"));
			System.out.println("pic:"+document.get("pic"));
			//描述信息没有同意保存到索引库中，不获取
		}
		//释放资源
		direct.close();
		
	}
	
	//根据Term删除索引，满足条件的全部删除
	@Test
	public void testDeleteIndex() throws Exception {
		//声明索引库位置
		Directory dt = FSDirectory.open(new File("E:\\temp\\index"));
		//设置写入索引配置
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, null);
		//创建写入对象
		IndexWriter iw = new IndexWriter(dt, config);
		
		//根据Term删除索引
		//iw.deleteDocuments(new Term("name", "java"));
		
		//全部删除
		iw.deleteAll();
		
		iw.close();
	}
	
	//修改索引     先删除再添加  如果更新索引的目标文档对象不存在，则执行添加
	@Test
	public void testUpdateIndex() throws Exception {
		//分词器
		Analyzer analyzer = new IKAnalyzer();
		//声明索引库位置
		Directory dt = FSDirectory.open(new File("E:\\temp\\index"));
		//设置写入索引配置
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		
		IndexWriter iw = new IndexWriter(dt, config);
		
		//生成文档对象
		Document doc = new Document();
		doc.add(new TextField("id", "1002", Store.YES));
		doc.add(new TextField("name", "lucene测试", Store.YES));
		
		//更新操作，把所有符合条件的文档删除，再新增，如果要更新的目标索引的文档对象不存在的话，就是添加
		iw.updateDocument(new Term("name", "test"), doc);
		
		iw.close();
	}
	
	
	//TermQuery词项查询，TermQuery不使用分析器，搜索关键词进行精确匹配
	@Test
	public void testTermQuery() throws Exception {
		
		//创建TermQuery搜索对象
		Query query = new TermQuery(new Term("name", "solr"));
		
		doSearch(query);
	}
	
	//NumericRangeQuery，指定数字范围查询.
	@Test
	public void testNumericRangeQuery() throws Exception {
		
		//参数分别是   域名   最小值  最大值  查询是否包含最小值，查询是否包含最大值
		Query query = NumericRangeQuery.newFloatRange("price", 54f, 56f, false, true);
		
		doSearch(query);
	}
	
	//BooleanQuery，布尔查询，实现组合条件查询。
	@Test
	public void testBooleanQuery() throws Exception {
		
		//TermQuery词项查询
		Query q1 = new TermQuery(new Term("name", "lucene"));
		
		//NumericRangeQuery，指定数字范围查询.
		Query q2 = NumericRangeQuery.newFloatRange("price", 54f, 56f, false, true);
		
		//创建布尔查询对象，组合查询条件
		BooleanQuery bq = new BooleanQuery();
		
		bq.add(q1, Occur.MUST_NOT);
		bq.add(q2, Occur.MUST);
		
		doSearch(bq);
	}

}
