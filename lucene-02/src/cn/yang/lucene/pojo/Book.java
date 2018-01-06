package cn.yang.lucene.pojo;

public class Book {
	
	// 图书ID
	private Integer id;
	// 图书名称
	private String name;
	// 图书价格
	private Float price;
	// 图书图片
	private String pic;
	// 图书描述
	private String desc;
	
	public Book() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public Book(Integer id, String name, Float price, String pic, String desc) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.pic = pic;
		this.desc = desc;
	}


	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Float getPrice() {
		return price;
	}
	public void setPrice(Float price) {
		this.price = price;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	


}
