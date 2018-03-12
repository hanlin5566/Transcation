package com.wiitrans.automation.logic;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.json.JSONObject;

import ring.middleware.content_type__init;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.db.GenericSQLDAO;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.mail.MailSender;
import com.wiitrans.base.mail.template.Mail;
import com.wiitrans.base.misc.Util;

public class SendMailLogicImpl implements Logic{
	/**
	 * @param jsonObject
	 * mailTemplate:对应mail.xml中 邮件模板的key
	 * 其他参数对应 mail.xml中 #{uid} 此种标识的值
	 */
	@Override
	public void invoke(JSONObject jsonObject) throws Exception{
		Log4j.log("start invoke SendMailLogicImpl");
		JSONObject paramJson = jsonObject.getJSONObject("param");
		String mailTemplate = paramJson.getString("mailTemplate");
		if(!BundleConf.MAIL_TEMPLATE.containsKey(mailTemplate)){
			//此处抛出的异常会记录到mysql的task表中
			Log4j.error("mailTemplate not found! mailTemplate:"+mailTemplate);
			throw new Exception("mailTemplate not found! mailTemplate:"+mailTemplate);
		}else{
			Mail mail = BundleConf.MAIL_TEMPLATE.get(mailTemplate);
			String recipient = mail.getRecipient();
			String subject = mail.getSubject();
			String content = mail.getContent();
			String nickname = mail.getNickname();
			MailSender sender = new MailSender(mail.getHostname(), mail.getUsername(), mail.getPwd());
			GenericSQLDAO sqlDAO = new GenericSQLDAO();
			sqlDAO.Init(true);
			Map<String, Object> param = Util.convert(paramJson);
			Map<String, Object> map = sqlDAO.selectOne(mail.getSql(), param);
			sqlDAO.UnInit();
			subject = Util.parseContent(subject, map,false);
			content = Util.parseContent(content, map,false);
			Log4j.info("recipient:"+recipient+" subject:"+subject+" nickname:"+nickname+" content:"+content);
			if("true".equals(mail.getSend())){
				sender.send(recipient,subject,nickname, content);
			}
			
//			TranslatorBean translatorBean = null;
//			Map<String, Object> orderBean = null;
//			if(uid != null && !"personnalQuotationOrder".equals(mailTemplate) && !"userPayment".equals(mailTemplate)){
//				TranslatorDAO translatorDAO = new TranslatorDAO();
//				translatorDAO.Init(true);
//				translatorBean = translatorDAO.Select(Integer.parseInt(uid));
//				translatorDAO.UnInit();
//			}
//			if(oid != null){
//				OrderDAO orderDAO = new OrderDAO();
//				orderDAO.Init(true);
//				if("personnalQuotationOrder".equals(mailTemplate)){
//					orderBean = orderDAO.SelectPersonnalQuotationOrder(oid);
//				}else if("userPayment".equals(mailTemplate)){
//					int order_id = Integer.parseInt(oid);
//					orderBean = orderDAO.SelectUserPayment(order_id);
//				}
//				orderDAO.UnInit();
//			}
//			
//			switch (mailTemplate) {
//			case "newTranslater"://新译员注册
//				/**
//				 *  @新译员注册
//					邮件标题：新译员注册-昵称
//					邮件内容：
//					客户信息
//					类别：（译员，企业客户，个人客户）
//					邮箱：
//					昵称：
//					日期：年月日时分
//
//				 */
//				if(translatorBean!=null){
//					subject = "新译员注册-"+translatorBean.nickname;
//					sBuffer.append("<b>客户信息</b></br>");
//					sBuffer.append("类别：译员</br>");
//					sBuffer.append("邮箱："+translatorBean.email+"</br>");
//					sBuffer.append("昵称："+translatorBean.nickname+"</br>");
//					sBuffer.append("日期："+sdf.format((translatorBean.create_time*1000L))+"</br>");
//				}else{
//					Log4j.error("user not found! uid:"+uid);
//					throw new Exception("user not found! uid:"+uid);
//				}
//				break;
//			case "newEnterpriseCustomer"://新企业用户注册
//				/**
//				 * @新企业客户注册
//					邮件标题：新企业客户注册-昵称
//					邮件内容：
//					客户信息
//					类别：（译员，企业客户，个人客户）
//					邮箱：
//					昵称：
//					日期：年月日时分
//				 */
//				if(translatorBean!=null){
//					subject = "新个人客户注册-"+translatorBean.nickname;
//					sBuffer.append("<b>客户信息</b></br>");
//					sBuffer.append("类别：企业客户</br>");
//					sBuffer.append("邮箱："+translatorBean.email+"</br>");
//					sBuffer.append("昵称："+translatorBean.nickname+"</br>");
//					sBuffer.append("日期："+sdf.format((translatorBean.create_time*1000L))+"</br>");
//				}else{
//					Log4j.error("user not found! uid:"+uid);
//					throw new Exception("user not found! uid:"+uid);
//				}
//				break;
//			case "newPersonalCustomer"://新个人用户注册
//				/**
//				 * @新个人客户注册
//					邮件标题：新个人客户注册-昵称
//					邮件内容：
//					客户信息
//					类别：（译员，企业客户，个人客户）
//					邮箱：
//					昵称：
//					日期：年月日时分
//				 */
//				if(translatorBean!=null){
//					subject = "新个人客户注册-"+translatorBean.nickname;
//					sBuffer.append("<b>客户信息</b></br>");
//					sBuffer.append("类别：个人客户</br>");
//					sBuffer.append("邮箱："+translatorBean.email+"</br>");
//					sBuffer.append("昵称："+translatorBean.nickname+"</br>");
//					sBuffer.append("日期："+sdf.format((translatorBean.create_time*1000L))+"</br>");
//				}else{
//					Log4j.error("user not found! uid:"+uid);
//					throw new Exception("user not found! uid:"+uid);
//				}
//				break;
//			case "personnalQuotationOrder"://人工报价订单
//				/**
//				 * @人工报价订单
//					邮件标题：人工报价订单
//					邮件内容：
//					订单基本信息
//					创建时间：年月日时分
//					订单号:
//					语言对:
//					价位:
//					领域:
//					备注:
//					客户：
//				 */
//				if(orderBean != null){
//					subject = "人工报价订单";
//					sBuffer.append("<b>订单基本信息</b></br>");
//					sBuffer.append("创建时间："+sdf.format((Long.parseLong(""+orderBean.get("create_time"))*1000L))+"</br>");
//					sBuffer.append("订单号："+orderBean.get("code")+"</br>");
//					sBuffer.append("语言对："+orderBean.get("s_language_name") +"-"+ orderBean.get("t_language_name")+"</br>");
//					sBuffer.append("价位："+orderBean.get("level")+"</br>");
//					sBuffer.append("领域："+orderBean.get("industry_name")+"</br>");
//					sBuffer.append("备注："+orderBean.get("description")+"</br>");
//					sBuffer.append("客户："+orderBean.get("email")+"</br>");
//				}else{
//					Log4j.error("order not found! oid:"+oid);
//					throw new Exception("order not found! oid:"+oid);
//				}
//				break;
//			case "userPayment"://用户下单并立即付款
//				/**
//				 *  @用户下单并立即付款
//					邮件标题：新订单XXX-已付款RMBXXX
//					邮件内容：
//					订单基本信息
//					创建时间：年月日时分
//					订单号:
//					语言对:
//					字数:
//					价位:
//					订单金额:
//					领域:
//					备注:
//					客户：
//					T预计交付时间：
//					E预计交付时间：（如果没有E，留空即可）
//				 */
//				if(orderBean != null){
//					subject = "新订单"+orderBean.get("code")+"-已付款RMB"+orderBean.get("total_money");
//					sBuffer.append("<b>订单基本信息</b></br>");
//					sBuffer.append("创建时间："+sdf.format((Long.parseLong(""+orderBean.get("create_time"))*1000L))+"</br>");
//					sBuffer.append("订单号："+orderBean.get("code")+"</br>");
//					sBuffer.append("语言对："+orderBean.get("s_language_name") +"-"+ orderBean.get("t_language_name")+"</br>");
//					sBuffer.append("字数："+orderBean.get("word_count")+"</br>");
//					sBuffer.append("价位："+orderBean.get("level")+"</br>");
//					sBuffer.append("订单金额："+orderBean.get("total_money")+"</br>");
//					sBuffer.append("领域："+orderBean.get("industry_name")+"</br>");
//					sBuffer.append("备注："+orderBean.get("description")+"</br>");
//					sBuffer.append("客户："+orderBean.get("email")+"</br>");
//					sBuffer.append("T预计交付时间："+sdf.format((Long.parseLong(""+orderBean.get("expected_delivery_time_t"))*1000L))+"</br>");
//					if(!orderBean.get("expected_delivery_time_e").equals(0)){
//						sBuffer.append("E预计交付时间："+sdf.format((Long.parseLong(""+orderBean.get("expected_delivery_time_e"))*1000L))+"</br>");
//					}
//				}else{
//					Log4j.error("order not found! oid:"+oid);
//					throw new Exception("order not found! oid:"+oid);
//				}
//				break;
//			default:
//				break;
//			}
//			sender.send(recipient,subject, sBuffer.toString());
		}
	}
}
