package com.wiitrans.base.misc;

public class FailUtil {
	public final static String SERVICE_ANALYSIS = "anal";
	public final static String SERVICE_AUTOMATION = "auto";
	public final static String SERVICE_EXAM = "exam";
	public final static String SERVICE_FRAGMENTATION = "frag";
	public final static String SERVICE_MATCH = "matc";
	public final static String SERVICE_MOBILE = "mobi";
	public final static String SERVICE_MSG = "msg_";
	public final static String SERVICE_OPERATION = "oper";
	public final static String SERVICE_RECOMMEND = "reco";
	public final static String SERVICE_STATE = "stat";
	public final static String SERVICE_TERM = "term";
	public final static String SERVICE_TM = "tm__";
	public final static String SERVICE_TMSVR = "tmsv";
	public final static String SERVICE_ORDERCENTER = "ocen";
	public final static String SERVICE_WIITRANS = "wiit";

	public final static String FAIL_PREFIX = "FAIL";
	public final static String SUCCESS = "OK";

	public final static String CLASS_SPOUT = "SPOU";
	public final static String CLASS_BUNDLE = "BUND";
	public final static String CLASS_TRANSLATOR_BOLT = "TBOL";
	public final static String CLASS_ORDER_BOLT = "TBOL";
	public final static String CLASS_STATE_QUEUE = "QUEU";
	// public final static String CLASS_BUNDLE = "BUND";
	// public final static String CLASS_BUNDLE = "BUND";
	
	public final static int RESERVE_ORDER_NOT_AUTH = 98;//没有抢单权限
	public final static int RESERVE_ORDER_MORE_THAN_ORDER_MAX = 99;//超出最大可抢单数
	public final static int RESERVE_ORDER_LOCKED = 100;//订单被锁定
	public final static int RESERVE_ORDER_RESERVEED = 101;//订单已被抢
	public final static int RESERVE_ORDER_SAME_TRANSLATOR = 103;//同一个译员抢单

	public final static int FAIL_TYPE_ID = 1;

	public static String GetFailedMsg(String service, String classname,
			int fail_type) {
		return FAIL_PREFIX + service + classname + "----"
				+ ("0000" + fail_type).substring(4);
	}

	public static String GetFailedMsg(String service, String classname,
			Exception ex) {
		String name = null;
		if (ex != null) {
			Class c = ex.getClass();
			if (c != null) {
				name = c.getName() + "----";
			}
		}
		if (name == null) {
			return FAIL_PREFIX + service + classname + "erro----";
		} else {
			return FAIL_PREFIX + service + classname + "erro"
					+ name.substring(0, 4);
		}

	}
}
