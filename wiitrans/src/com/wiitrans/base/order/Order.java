package com.wiitrans.base.order;

import com.wiitrans.base.translator.Translator;

public class Order {
	// NORMAL：抢订单 VIP：申请接单
	public enum TYPE {
		NONE, NORMAL, VIP
	};

	public TYPE _type = TYPE.NONE;

	public int _langpair = 0;

	public int _industry_id = 0;

	public String _code = null;

	public String _sid = null;

	public String _uid = null;

	public String _nid = null;

	public Translator _translator = null; // 抢单成功的T译员
	// public ArrayList<Translator> _tranWaiters = new ArrayList<Translator>();
	// // 申请接单T译员列表

	public Translator _editor = null; // 抢单成功的E译员
	// public ArrayList<Translator> _editWaiters = new ArrayList<Translator>();
	// // 申请接单E译员列表
}