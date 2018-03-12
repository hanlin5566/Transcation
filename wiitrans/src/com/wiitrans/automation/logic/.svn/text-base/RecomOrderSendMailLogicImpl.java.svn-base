package com.wiitrans.automation.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.ProcRecomOrderEmailDAO;
import com.wiitrans.base.db.model.ProcRecomOrderBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.mail.MailSender;
import com.wiitrans.base.mail.template.Mail;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class RecomOrderSendMailLogicImpl implements Logic {

	@Override
	public void invoke(JSONObject jsonObject) throws Exception {
		JSONObject paramJson = jsonObject.getJSONObject("param");
		int order_id;
		String ordercode;
		String mailTemplate;
		if (paramJson == null) {
			Log4j.log("start stop invoke RecomOrderSendMail paramJson is null");
			return;
		} else {
			order_id = Util.GetIntFromJSon("order_id", paramJson);
			ordercode = Util.GetStringFromJSon("ordercode", paramJson);
			mailTemplate = paramJson.getString("mailTemplate");
			if (!BundleConf.MAIL_TEMPLATE.containsKey(mailTemplate)) {
				// 此处抛出的异常会记录到mysql的task表中
				Log4j.error("mailTemplate not found! mailTemplate:"
						+ mailTemplate);
				throw new Exception("mailTemplate not found! mailTemplate:"
						+ mailTemplate);
			} else {
				if (order_id <= 0) {
					Log4j.log("start end invoke RecomOrderSendMail order_id <=0 ");
					return;
				}
				if (ordercode == null) {
					Log4j.log("start end invoke RecomOrderSendMail ordercode is null ");
					return;
				}
			}
		}
		Log4j.log("start invoke RecomOrderSendMail order_id is " + order_id
				+ " ordercode is " + ordercode);
		Mail mail = BundleConf.MAIL_TEMPLATE.get(mailTemplate);
		ProcRecomOrderEmailDAO dao = null;
		try {
			// AppConfig app = new AppConfig();
			// app.Parse();
			// BundleParam param = app._bundles.get("recomTopo");
			BundleParam param = WiitransConfig.getInstance(0).RECOM;
			HashMap map = new HashMap<String, Object>();
			int p_node_id = BundleConf.DEFAULT_NID;
			int p_order_id = order_id;
			int p_maxcount = param.BUNDLE_TRANSLATOR_ORDER_MAXCOUNT;
			boolean p_match_industry = BundleConf.BUNDLE_MATCH_INDUSTRY;
			Log4j.info("[p_node_id]: " + p_node_id + " [p_order_id]: "
					+ p_order_id + " [p_maxcount]: " + p_maxcount
					+ " [p_match_industry]" + p_match_industry);
			map.put("p_node_id", p_node_id);
			map.put("p_order_id", p_order_id);
			map.put("p_maxcount", p_maxcount);
			map.put("p_match_industry", p_match_industry);

			dao = new ProcRecomOrderEmailDAO();
			dao.Init(false);
			List<ProcRecomOrderBean> list = dao.RecomOrderEmail(map);
			dao.UnInit();

			if (list != null && list.size() > 0) {
				Map<String, Object> parammap = new HashMap<String, Object>();
				parammap.put("code", ordercode);

				Log4j.info("recom order count:" + list.size());
				for (ProcRecomOrderBean item : list) {
					String recipient = item.email;
					parammap.put("email", item.email);
					String nickname = item.nickname;
					parammap.put("nickname", item.nickname);
					String subject = Util.parseContent(mail.getSubject(),
							parammap, true);
					String content = Util.parseContent(mail.getContent(),
							parammap, true);

					MailSender sender = new MailSender(mail.getHostname(),
							mail.getUsername(), mail.getPwd());
					if ("true".equals(mail.getSend())) {
						Log4j.info("[recipient]: " + recipient + " [subject]: "
								+ subject + " [nickname]: " + nickname
								+ " [content]: " + content);
						sender.send(recipient, subject, nickname, content);
					}
				}
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}

		Log4j.log("stop invoke RecomOrderSendMail order_id is " + order_id
				+ " ordercode is " + ordercode);
	}
}
