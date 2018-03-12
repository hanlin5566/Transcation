package com.wiitrans.base.http;

public class ICIBAWord {
	public String source;// 原文
	public String trans;// 译文
	public String pl;// 复数
	public String past;// 过去式
	public String done;// 过去分词
	public String ing;// 现在分词
	public String third;// 第三人称单数
	public String er;// 比较级
	public String est;// 最高级
	public boolean isWord;// 是否是单词
	public String iciba_url;
	public String auth_key;

	public ICIBAWord(String source, String url, String key) {
		this.source = source;
		this.iciba_url = url;
		this.auth_key = key;
	}
}
