package com.wiitrans.base.translator;

import java.util.HashMap;
import com.wiitrans.base.order.Order;

public class Translator {

	public String _sid = null;
	public String _uid = null;
	public String _nid = null;
	public HashMap<String, TranslatorGrade> gradeeditor;// key为语言对ID，value存储校对权限及翻译级别
	public int _orderListCount = 0;
	// public int _wartOrderListCount = 0;
	public int _myOrderListCount = 0;

	// 推荐T抢单订单列表
	public HashMap<String, Order> _VIPOrderList_t = new HashMap<String, Order>();

	// 推荐E抢单订单列表
	public HashMap<String, Order> _VIPOrderList_e = new HashMap<String, Order>();

	// public HashMap<String, Order> _VIPOrderWaitList_t = new HashMap<String,
	// Order>();

	// public HashMap<String, Order> _VIPOrderWaitList_e = new HashMap<String,
	// Order>();

	// 推荐抢单订单列表
	public HashMap<String, Order> _normalOrderList = new HashMap<String, Order>();

	// 译员已抢到的订单
	public HashMap<String, Order> _myOrders = new HashMap<String, Order>();
}
