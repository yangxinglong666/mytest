package cn.yang.lucene.dao;

import java.util.List;

import cn.yang.lucene.pojo.Book;

public interface BookDao {
	
	public List<Book> queryTerms();

}
