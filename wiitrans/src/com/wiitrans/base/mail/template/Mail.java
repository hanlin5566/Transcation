package com.wiitrans.base.mail.template;

public class Mail {
	private String name;
	private String hostname;
	private String username;
	private String pwd;
	private String nickname;
	private String recipient;
	private String subject;
	private String content;
	private String sql;
	private String send;
	
	public Mail(String name, String hostname, String username, String pwd,
			String nickname, String recipient, String subject, String content, String sql,String send) {
		super();
		this.name = name;
		this.hostname = hostname;
		this.username = username;
		this.pwd = pwd;
		this.nickname = nickname;
		this.recipient = recipient;
		this.subject = subject;
		this.content = content;
		this.sql = sql;
		this.send = send;
	}
	
	
	
	public String getSend() {
		return send;
	}

	public String getName() {
		return name;
	}

	public String getHostname() {
		return hostname;
	}

	public String getUsername() {
		return username;
	}

	public String getPwd() {
		return pwd;
	}

	public String getNickname() {
		return nickname;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public String getSql() {
		return sql;
	}
	
	
}
