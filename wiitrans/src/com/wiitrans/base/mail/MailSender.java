package com.wiitrans.base.mail;

import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import com.wiitrans.base.log.Log4j;


public class MailSender {
	//发送邮件的props
	private final transient Properties props = System.getProperties();
	
	private transient MailAuthenticator authenticator;
	
	private transient Session session;
	
	public MailSender(final String stmpHostName,final String username,final String pwd) {
		init(stmpHostName,username,pwd);
	}
	
	public void init(String stmpHostName,String username,String pwd){
		props.put("mail.stmp.auth", "true");
		props.put("mail.stmp.host", stmpHostName);
		//验证
		authenticator = new MailAuthenticator(username, pwd);
		//创建session
		session = Session.getDefaultInstance(props);
	}
	
	public void send(String recipient,String subject,String senderNickname,Object content) throws Exception{
		
		final MimeMessage message = new MimeMessage(session);
		String nickName = authenticator.getUsername();
		//设置发件人昵称
		try {
			nickName = MimeUtility.encodeText(senderNickname);
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
		}
		//设置发件人
		message.setFrom(new InternetAddress(nickName+"<"+authenticator.getUsername()+">"));
		//设置收件人
		message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
		//设置主题
		message.setSubject(subject);
		//设置邮件内容
		message.setContent(content.toString(),"text/html;charset=utf-8");
		//发送
		Transport.send(message);
	}
}
