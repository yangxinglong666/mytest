package cn.yang.lucene.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import cn.yang.lucene.dao.BookDao;
import cn.yang.lucene.pojo.Book;

//准备数据
public class BookDaoImpl implements BookDao{

	//原始jdbc连接数据库
	@Override
	public List<Book> queryTerms() {
		 //连接数据库
		 Connection connection = null;
		 //预编译语句
		 PreparedStatement ps = null;
		 //结果集
		 ResultSet resultSet = null;
		 //list集合存放查询得到的数据
		 List<Book> list = new ArrayList<>();	
		 
		try {
		   //注册驱动
		  Class.forName("com.mysql.jdbc.Driver");
		  //获得连接
		  connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lucene", "root", "root");
		  //创建sql语句
		  String sql = "select * from book";
		  //生成预编译语句
		  ps = connection.prepareStatement(sql);
		  //获得结果集
		  resultSet = ps.executeQuery();
		  //处理结果集
		while (resultSet.next()) {
			//book对象，封装查询结果
			Book book = new Book();
			book.setId(resultSet.getInt("id"));
			book.setName(resultSet.getString("name"));
			book.setPrice(resultSet.getFloat("price"));
			book.setPic(resultSet.getString("pic"));
			book.setDesc(resultSet.getString("description"));
			list.add(book);
			
		}
		  
		} catch (Exception e) {
			
			e.printStackTrace();
		}  
		
		//返回
		return list;
	}
	
	
	

}
