package com.hzcf.edge.common.entity;


/**
 * 
 * 用来封装参数
 *	的Page
 * @param <T>
 */

public class Page{
	//总页数
	@SuppressWarnings("unused")
	private Integer countPage;
	//当前页
	private Integer currPage = 1;
	//每页显示记录数
	private Integer pageSize=5;
	//开始位置
	private Integer startIndex;
	//总记录数
	private Integer countRecord;
	//Oracle数据库的结束索引
	private Integer endIndex;
	
	public Integer getCountRecord() {
		return countRecord;
	}

	public void setCountRecord(Integer countRecord) {
		this.countRecord = countRecord;
	}

	/**
	 * MySQL数据库的构造函数
	 * @param currPage
	 * @param pageSize
	 */
	public Page(Integer currPage, Integer pageSize) {
		this.currPage = currPage;
		this.pageSize = pageSize;
		this.startIndex = (currPage-1)*pageSize;
	}
	public Integer getEndIndex() {
		this.endIndex = getStartIndex()+getPageSize();
		return endIndex;
	}

	public void setEndIndex(Integer endIndex) {
		this.endIndex = endIndex;
	}

	/**
	 * Oracle数据库的构造函数
	 * @param currPage
	 * @param pageSize
	 * @param startIndex
	 */
	public Page(Integer currPage, Integer pageSize,Integer startIndex) {
		this.currPage = currPage;
		this.pageSize = pageSize;
		this.startIndex = startIndex;
		this.endIndex = startIndex+pageSize;
	}
	
	public Page() {
		 //Page(currPage,pageSize);
	}

	public Integer getCountPage() {
		return countPage = ((countRecord%pageSize)!=0) ? countRecord/pageSize+1 : countRecord/pageSize;
	}

	public void setCountPage(Integer countPage) {
		this.countPage = countPage;
	}

	public Integer getCurrPage() {
		return currPage;
	}

	public void setCurrPage(Integer currPage) {
		this.currPage = currPage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getStartIndex() {
		this.startIndex = (getCurrPage()-1)*getPageSize();
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

}
