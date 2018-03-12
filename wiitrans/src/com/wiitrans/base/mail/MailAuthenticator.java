package com.wiitrans.base.mail;

import javax.mail.Authenticator;

public class MailAuthenticator extends Authenticator{
	private String username;
	private String pwd;
	public MailAuthenticator(String username, String pwd) {
		super();
		this.username = username;
		this.pwd = pwd;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	
}
