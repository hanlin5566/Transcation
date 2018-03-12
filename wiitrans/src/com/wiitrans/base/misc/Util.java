/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.misc;

import io.netty.buffer.ByteBuf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ibatis.io.ResolverUtil.IsA;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.file.FileConst;
import com.wiitrans.base.log.Log4j;

public class Util {

	// 语言对
	public static String GetRedisKey(int pair_id) {
		return Const.PREFIX_LANGPAIR + pair_id;
	}

	// 语言对 领域
	public static String GetRedisKeyForIndustry(int pair_id, int industry_id) {
		return Const.PREFIX_LANGPAIR_INDUSTRY + pair_id + "_" + industry_id;
	}

	// 语言对 级别
	public static String GetRedisKeyForGrade(int pair_id, int grade_id) {
		return Const.PREFIX_LANGPAIR_GRADE + pair_id + "_" + grade_id;
	}

	// 语言对 级别 领域
	public static String GetRedisKeyForGradeIndustry(int pair_id, int grade_id,
			int industry_id) {
		return Const.PREFIX_LANGPAIR_GRADE_INDUSTRY + pair_id + "_" + grade_id
				+ "_" + industry_id;
	}

	public static String GetMyOrderListKey(int translator_id) {
		return Const.PREFIX_TRANS_MYORDER_LIST + translator_id;
	}

	public static String GetOrderTransListKey(String ordercode) {
		return Const.PREFIX_ORDRE_TRANS_LIST + "_" + ordercode;
	}

	public static String GetOrderListTKey(int translator_id) {
		return Const.PREFIX_TRANS_ORDER_LIST_T + "_" + translator_id;
	}

	public static String GetOrderListEKey(int translator_id) {
		return Const.PREFIX_TRANS_ORDER_LIST_E + "_" + translator_id;
	}

	public static Set<String> tagSet;
	public static Map<String, String> _tagAtt;

	public static boolean FindInSet(int id, String str, char separator) {
		if (str == null || str.trim().length() == 0) {
			return false;
		}
		String[] ids = str.trim().split(String.valueOf(separator));
		for (String string : ids) {
			if (Util.String2Int(string) == id) {
				return true;
			}
		}
		return false;
	}

	public static boolean FindInSet(int id, String str) {
		if (str == null || str.trim().length() == 0) {
			return false;
		}
		String[] ids = str.trim().split(",");
		for (String string : ids) {
			if (Util.String2Int(string) == id) {
				return true;
			}
		}
		return false;
	}

	public static long String2Long(String num) {
		long ret = FileConst.INVALID_INT64_COUNT;

		try {

			if (num != null && !num.isEmpty()) {
				ret = Long.parseLong(num);
			}
		} catch (NumberFormatException e) {
			Log4j.error(e);
		}

		return ret;
	}

	public static long[] LonglistToAry(ArrayList<Long> list) {
		long[] ary = new long[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ary[i] = list.get(i);
		}
		return ary;
	}

	public static int[] IntegerlistToAry(ArrayList<Integer> list) {
		int[] ary = new int[list.size()];
		for (int i = 0; i < ary.length; i++) {
			ary[i] = list.get(i);
		}
		return ary;
	}

	public static Process exeCmd(String commandStr) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(commandStr);
		} catch (IOException e) {
			Log4j.error(e);
		}
		return p;
	}

	public static ArrayList<String> exeCmdForResult(String commandStr) {
		ArrayList<String> ary = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec(commandStr);
			InputStream in = p.getInputStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				ary.add(line);
			}
			p.waitFor();
			in.close();
			reader.close();
			p.destroy();
		} catch (Exception e) {
			Log4j.error(e);
		}
		return ary;
	}

	public static int String2Int(String num) {
		int ret = FileConst.INVALID_INT32_COUNT;

		try {

			if (num != null && !num.isEmpty()) {
				ret = Integer.parseInt(num);
			}
		} catch (NumberFormatException e) {
			Log4j.error(e);
		}

		return ret;
	}

	public static float String2Float(String num) {
		float ret = FileConst.INVALID_FLOAT_COUNT;

		try {

			if (num != null && !num.isEmpty()) {
				ret = Float.parseFloat(num);
			}
		} catch (NumberFormatException e) {
			Log4j.error(e);
		}

		return ret;
	}

	public static short String2Short(String num) {
		short ret = FileConst.INVALID_INT16_COUNT;

		try {

			if (num != null && !num.isEmpty()) {
				ret = Short.parseShort(num);
			}
		} catch (NumberFormatException e) {
			Log4j.error(e);
		}

		return ret;
	}

	public static boolean String2Bool(String num) {
		return (num != null && num.equalsIgnoreCase("true"));
	}

	public static JSONObject ByteBufToJSon(ByteBuf bb) {
		JSONObject jObj = null;
		if (bb != null) {
			String msg = bb.toString(Charset.forName(Const.DEFAULT_CHARSET));
			jObj = new JSONObject(msg);
		}

		return jObj;
	}

	public static String GetStringFromJSon(String key, JSONObject obj) {
		String val = null;

		if ((obj != null) && (obj.has(key))) {
			val = obj.getString(key);
		}

		return val;
	}

	public static float GetFloatFromJSon(String key, JSONObject obj) {
		String val = null;

		if ((obj != null) && (obj.has(key))) {
			val = obj.getString(key);
		}

		return String2Float(val);
	}

	public static int GetIntFromJSon(String key, JSONObject obj) {
		String val = null;

		if ((obj != null) && (obj.has(key))) {
			val = obj.getString(key);
		}

		return String2Int(val);
	}

	public static JSONObject GetJSonFromJSon(String key, JSONObject obj) {
		JSONObject val = null;

		if ((obj != null) && (obj.has(key))) {
			val = obj.getJSONObject(key);
		}

		return val;
	}

	public static int CheckTermCount(String text, String pattern, int maxcount) {
		int index = -1;
		int count = 0;
		int begin = 0;
		if (maxcount <= 0) {
			return 0;
		}
		for (int i = 0; i < maxcount; i++) {
			index = text.indexOf(pattern, begin);
			if (index >= 0) {
				count++;
				begin = index + pattern.length();
			}
		}

		return count;
	}

	public static long GetHashCode(String str) {
		long code = 0;

		for (char ch : str.toCharArray()) {
			code = code * 131313 + ch;
		}
		return code;
	}

	public static int GetIntFromNow() {

		return (int) (Calendar.getInstance().getTimeInMillis() / 1000L);
	}

	public static int GetIntFromSDate(String sdate) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date date = sdf.parse(sdate);
			return (int) (date.getTime() / 1000L);
		} catch (Exception e) {
			Log4j.error(e);
		}
		return 0;
	}

	public static Date GetDateFromInt(int seconds) {

		return new Date(seconds * 1000L);
	}

	public static Map<String, Object> convert(JSONObject jsonObject)
			throws Exception {
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (String key : jsonObject.keySet()) {
			Object value = jsonObject.get(key);
			if (value instanceof JSONArray) {
				retMap.put(key, convert((JSONArray) value));
			} else if (value instanceof JSONObject) {
				retMap.put(key, convert((JSONObject) value));
			} else {
				retMap.put(key, value);
			}

		}
		return retMap;
	}

	public static List<Object> convert(JSONArray jsonObject) throws Exception {
		List<Object> retList = new ArrayList<Object>();
		for (int i = 0; i < jsonObject.length(); i++) {
			Object value = jsonObject.get(i);
			if (value instanceof JSONArray) {
				retList.add(convert((JSONArray) value));
			} else if (value instanceof JSONObject) {
				retList.add(convert((JSONObject) value));
			} else {
				retList.add(value);
			}
		}
		return retList;
	}

	private static String _ttx_tags = "ttx_tags";

	public static String clearTag(String str) {
		String ret = str;
		try {
			Pattern tagPattern = Pattern.compile(".*?\\<[^\\>]*\\>.*?");
			Matcher tagMatch = tagPattern.matcher(str);
			// 匹配到标签
			if (tagMatch.matches()) {
				// 过滤文本
				Pattern pattern = Pattern.compile("\\<[^\\>]*\\>");
				Matcher match = pattern.matcher(str);
				// System.out.println();
				while (match.find()) {
					String tag = match.group();
					String tag_ = tag.indexOf(" ") > 0 ? tag.split(" ")[0]
							+ ">" : tag;
					if (!tagSet.contains(tag_)) {
						tagSet.add(tag_);
						RedisCache cache = new RedisCache();
						cache.Init(BundleConf.BUNDLE_REDIS_IP);
						cache.sadd(_ttx_tags, tag_);
						cache.UnInit();
					}
					// System.out.print(tag);
				}
				// 过滤标签
			}
			ret = str.replaceAll("\\<.*?\\>", "");
			// 转换特殊的#&lt #&gt 为 < >
			ret = ret.replaceAll("#&lt", "<");
			ret = ret.replaceAll("#&gt", ">");
		} catch (Exception e) {
			Log4j.error(e);
		}
		return ret;
	}

	private static void getDfAtt(Node node, Map<String, String> attMap) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			// 过滤标签的属性
			Pattern tagPattern = Pattern.compile("\\<.*?\\>");
			Matcher tagMatch = tagPattern.matcher(node.getTextContent());
			// 匹配不到标签才拿属性
			if ("df".equalsIgnoreCase(node.getNodeName())
					&& node.hasAttributes() && !tagMatch.matches()) {
				NamedNodeMap att = node.getAttributes();
				for (int i = 0; i < att.getLength(); i++) {
					Node attNode = att.item(i);
					if (!attMap.containsKey(attNode.getNodeName())) {
						attMap.put(attNode.getNodeName(),
								attNode.getNodeValue());
					}
				}
			}
		}
		// 遍历子节点
		if (node.hasChildNodes()) {
			NodeList childList = node.getChildNodes();
			for (int i = 0; i < childList.getLength(); i++) {
				getDfAtt(childList.item(i), attMap);
			}
		}
	}

	private static void setDF(String content, Node sourceNode, Node targetNode,
			String lang) {
		try {
			// modify by hanson 2016/03/15 添加特殊字符替换
			content = deCodeSpecialChart(content);
			// df
			Element df = targetNode.getOwnerDocument().createElement("df");
			if (sourceNode == null) {
				// 取默认属性
				String lang_atts = _tagAtt.get(lang);
				JSONObject lang_atts_json = new JSONObject(lang_atts);
				for (String key : lang_atts_json.keySet()) {
					df.setAttribute(key, (String) lang_atts_json.get(key));
				}
			} else {
				// 取源属性
				Map<String, String> attMap = new HashMap<String, String>();
				getDfAtt(sourceNode, attMap);
				for (String key : attMap.keySet()) {
					df.setAttribute(key, (String) attMap.get(key));
				}
			}
			// add tag content
			df.appendChild(targetNode.getOwnerDocument()
					.createTextNode(content));
			targetNode.appendChild(df);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static void setUT(String tag, Node sourceNode, Node targetNode,
			String lang) {
		try {
			String tagName = tag.indexOf(" ") > 0 ? tag.split(" ")[0]
					.replaceAll("\\<", "") : tag.replaceAll("\\<|\\>", "");
			// df
			Element df = targetNode.getOwnerDocument().createElement("df");
			if (sourceNode == null) {
				// 取默认属性
				String lang_atts = _tagAtt.get(lang);
				JSONObject lang_atts_json = new JSONObject(lang_atts);
				for (String key : lang_atts_json.keySet()) {
					df.setAttribute(key, (String) lang_atts_json.get(key));
				}
			} else {
				// 取源属性
				Map<String, String> attMap = new HashMap<String, String>();
				getDfAtt(sourceNode, attMap);
				for (String key : attMap.keySet()) {
					df.setAttribute(key, (String) attMap.get(key));
				}
			}
			// 标签需要 ut
			Element ut = targetNode.getOwnerDocument().createElement("ut");
			JSONObject tag_atts_json = null;
			if (_tagAtt.containsKey(tagName)
					&& _tagAtt.get(tagName).length() > 0) {
				tag_atts_json = new JSONObject(_tagAtt.get(tagName));
			} else {
				Log4j.error(tagName + " con't found att,att[" + tagName
						+ "],find by redis!");
				RedisCache cache = new RedisCache();
				cache.Init(BundleConf.BUNDLE_REDIS_IP);
				Map<String, String> tagAtt_redis = cache.hmget(_ttx_tags_att);
				cache.UnInit();
				if (tagAtt_redis.containsKey(tagName)) {
					String tagAtt = tagAtt_redis.get(tagName);
					if (tagAtt.length() > 0) {
						tag_atts_json = new JSONObject(tagAtt);
						// 放入缓存
						_tagAtt.put(tagName, tagAtt);
					}
				}
			}
			if (tag_atts_json != null) {
				for (String key : tag_atts_json.keySet()) {
					ut.setAttribute(key, (String) tag_atts_json.get(key));
				}
			} else {
				Log4j.error(tagName + " con't found att,att[" + tagName + "]");
			}
			// add tag content
			ut.appendChild(targetNode.getOwnerDocument().createTextNode(tag));
			// df add ut
			df.appendChild(ut);
			// targetNode add df
			targetNode.appendChild(df);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("tag:" + tag);
			Log4j.error(e);
		}
	}

	private static String _ttx_tags_att = "ttx_tags_att";
	static {
		initTag();
	}

	private static void initTag() {
		if (tagSet == null) {
			// 初始化tag
			// Log4j.info("########### init tagSet ###########");
			RedisCache cache = new RedisCache();
			cache.Init(BundleConf.BUNDLE_REDIS_IP);
			tagSet = cache.smembers(_ttx_tags);
			// for (String string : tagSet) {
			// Log4j.info("########### tagSet value " + string + "###########");
			// }
			cache.UnInit();
		}
		if (_tagAtt == null) {
			// 初始化tag属性
			// Log4j.info("########### init tagAtt ###########");
			RedisCache cache = new RedisCache();
			cache.Init(BundleConf.BUNDLE_REDIS_IP);
			_tagAtt = cache.hmget(_ttx_tags_att);
			// for (String string : _tagAtt.keySet()) {
			// Log4j.info("########### tagAtt key " + string + " value "
			// + _tagAtt.get(string) + "###########");
			// }
			// 添加默认字体属性
			JSONObject lang_att_json = new JSONObject();
			lang_att_json.put("Font", "宋体");
			_tagAtt.put("ZH-CN", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "Arial");
			_tagAtt.put("EN-US", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "Arial");
			_tagAtt.put("ES-EM", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "Arial");
			_tagAtt.put("DE-DE", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "標楷體");
			_tagAtt.put("ZH-TW", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "MS UI Gothic");
			_tagAtt.put("JA", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "Gulim");
			_tagAtt.put("KO-KR", lang_att_json.toString());

			lang_att_json = new JSONObject();
			lang_att_json.put("Font", "Arial");
			_tagAtt.put("FR-FR", lang_att_json.toString());

			cache.UnInit();
		}
	}

	public static void addTagAtt(String key, JSONObject json) {
		String tagName = key.indexOf(" ") > 0 ? key.split(" ")[0].replaceAll(
				"\\<", "") : key.replaceAll("\\<|\\>", "");
		if (!_tagAtt.containsKey(tagName)) {
			RedisCache cache = new RedisCache();
			cache.Init(BundleConf.BUNDLE_REDIS_IP);
			cache.hset(_ttx_tags_att, tagName, json.toString());
			cache.UnInit();
			_tagAtt.put(tagName, json.toString());
		}
	}

	public static void setTagAttBySource(String str, Node sourceNode,
			Node targetNode) {
		try {
			splitTrans(str, sourceNode, targetNode);
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		}
	}

	public static void setTag(String str, Node targetNode) {
		try {
			splitTrans(str, null, targetNode);
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		}
	}

	public static String deCodeSpecialChart(String str) {
		// if(str.indexOf("\\\n")>0){
		// str = str.replaceAll("\\\n", "");
		// }
		if (str == null) {
			return null;
		}
		if (str.indexOf("#&gt") >= 0) {
			str = str.replaceAll("#&gt", ">");
		}
		if (str.indexOf("#&lt") >= 0) {
			str = str.replaceAll("#&lt", "<");
		}
		if (str.indexOf("#&amp") >= 0) {
			str = str.replaceAll("#&amp", "&");
		}
		return str;
	}

	public static String enCodeSpecialChart(String str) {
		StringBuffer sb = new StringBuffer();
		// #3198 剔除<
		Pattern tagPattern = Pattern.compile("\\<[^\\>\\<]*\\>");
		Matcher tagMatch = tagPattern.matcher(str);
		if (Pattern.matches("(?s).*?\\<[^\\>]*\\>.*?", str)) {
			// 匹配到标签
			int pre_end = 0;
			while (tagMatch.find()) {
				int s = tagMatch.start();
				int e = tagMatch.end();
				if (s != pre_end) {
					// 中间内容
					String content = str.substring(pre_end, s);
					sb.append(replaceCodeSpecialChat(content));
				}
				// 标签不做转换处理
				String tag = str.substring(s, e);
				String tagName = tag.indexOf(" ") > 0 ? tag.split(" ")[0]
						.replaceAll("\\<", "") : tag.replaceAll("\\<|\\>", "");
				// 如果标签库中没有此标签属性则按照内容处理转换
				if (!_tagAtt.containsKey(tagName)) {
					sb.append(replaceCodeSpecialChat(tag));
				} else {
					sb.append(tag);
				}
				pre_end = e;
			}
			// 补尾
			if (pre_end < str.length()) {
				String endContent = str.substring(pre_end, str.length());
				sb.append(replaceCodeSpecialChat(endContent));
			}
		} else {
			return str;
		}
		return sb.toString();
	}

	public static String replaceCodeSpecialChat(String str) {
		if (str == null) {
			return null;
		}
		if (str.indexOf("&") >= 0) {
			str = str.replaceAll("&", "#&amp");
		}
		if (str.indexOf("<") >= 0) {
			str = str.replaceAll("<", "#&lt");
		}
		if (str.indexOf(">") >= 0) {
			str = str.replaceAll(">", "#&gt");
		}
		return str;
	}

	public static String cleanSpecialChat(String str) {
		if (str == null) {
			return null;
		}
		if (str.indexOf("&") >= 0) {
			str = str.replaceAll("&", "");
		}
		if (str.indexOf("<") >= 0) {
			str = str.replaceAll("<", "");
		}
		if (str.indexOf(">") >= 0) {
			str = str.replaceAll(">", "");
		}
		if (str.indexOf("/") >= 0) {
			str = str.replaceAll("/", "");
		}
		return str;
	}

	public static void replaceAllSoftEnt(Node sourceMrk) {
		NodeList nodeList = sourceMrk.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				String content = node.getTextContent();
				if (content.indexOf("\n") >= 0) {
					node.setTextContent(content.replaceAll("\\n", "\\r"));
				}
			}
		}
	}

	private static void splitTrans(String str, Node sourceNode, Node targetNode)
			throws Exception {
		String lang = targetNode.getAttributes().getNamedItem("Lang")
				.getNodeValue();
		Pattern tagPattern = Pattern.compile("\\<[^\\>\\<]*\\>");
		Matcher tagMatch = tagPattern.matcher(str);
		if (Pattern.matches("(?s).*?\\<[^\\>]*\\>.*?", str)) {
			// 匹配到标签
			int pre_end = 0;
			while (tagMatch.find()) {
				int s = tagMatch.start();
				int e = tagMatch.end();
				if (s != pre_end) {
					// 中间内容
					String content = str.substring(pre_end, s);
					setDF(content, sourceNode, targetNode, lang);
					// System.out.println(content);
				}
				String tag = str.substring(s, e);
				// System.out.println(tag);
				// modify by hanson 2016/03/15 添加如果不是标签的带有尖叫号内容按照内容处理
				// if(tagSet.contains(tag)){
				setUT(tag, sourceNode, targetNode, lang);
				// }else{
				// setDF(tag,sourceNode, targetNode, lang);
				// }
				pre_end = e;
			}
			// 补尾
			if (pre_end < str.length()) {
				String endContent = str.substring(pre_end, str.length());
				// System.out.println(endContent);
				setDF(endContent, sourceNode, targetNode, lang);
			}
		} else {
			// 匹配不到标签 直接添加df
			// df
			// System.out.println(str);
			setDF(str, sourceNode, targetNode, lang);
		}
	}

	/**
	 * 
	 * @param originalContent
	 * @param param
	 * @param escape
	 *            是否转义
	 * @return
	 */
	public static String parseContent(String originalContent,
			Map<String, Object> param, boolean escape) {
		try {
			String openToken = "${";
			String closeToken = "}";
			String text = originalContent;
			StringBuilder builder = new StringBuilder();
			if (text != null && text.length() > 0) {
				char[] src = text.toCharArray();
				int offset = 0;
				int start = text.indexOf(openToken, offset);
				while (start > -1) {
					if (start > 0 && src[start - 1] == '\\') {
						// the variable is escaped. remove the backslash.
						builder.append(src, offset, start - offset - 1).append(
								openToken);
						offset = start + openToken.length();
					} else {
						int end = text.indexOf(closeToken, start);
						if (end == -1) {
							builder.append(src, offset, src.length - offset);
							offset = src.length;
						} else {
							builder.append(src, offset, start - offset);
							offset = start + openToken.length();
							String content = new String(src, offset, end
									- offset);
							// modify by hanson 返回原有结果，不对时间格式处理，改由SQL函数
							// FROM_UNIXTIME(u.create_time,'%Y-%m-%d %H:%i:%s')
							// 处理
							// if(content.indexOf("time")>0 &&
							// param.containsKey(content)){
							// SimpleDateFormat sdf = new
							// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							// builder.append(Integer.parseInt(""+param.get(content))!=0?sdf.format((Long.parseLong(""+param.get(content))*1000L)):"");
							// }else{
							String param_string = param.containsKey(content)
									&& param.get(content) != null ? param.get(
									content).toString() : "";
							if (escape) {
								if (param_string.indexOf("\\") > 0) {
									param_string = param_string.replaceAll(
											"\\\\", "\\\\\\\\");
								}
								builder.append(StringEscapeUtils
										.escapeSql(param_string));
							} else {
								builder.append(param_string);
							}
							// }
							offset = end + closeToken.length();
						}
					}
					start = text.indexOf(openToken, offset);
				}
				if (offset < src.length) {
					builder.append(src, offset, src.length - offset);
				}
			}
			originalContent = builder.toString();
		} catch (Exception e) {
			Log4j.error(" CONTENT:" + originalContent + " param:" + param
					+ " case:" + e);
		}
		return originalContent;
	}
}
