package com.wiitrans.base.db.model;

public class OrderBean {
	public int order_id = 0;

	public int node_id = 0;

	public String level = "";

	public int pair_id = 0;

	public int industry_id = 0;

	public int tnid = 0;

	public int translator_id = 0;

	public int enid = 0;

	public int editor_id = 0;

	public int customer_id = 0;

	public int price_level_id = 0;

	public String code = "";

	public byte status = 0;

	public int create_time = 0;

	public int expected_delivery_time = 0;

	public int need_time = 0;

	public int get_time_t = 0;

	public int get_time_e = 0;

	public int payment_time = 0;

	public boolean preprocess = false;

	public int currency_id = 1;

	public double total_money = 0;

	public double actual_money_t = 0;

	public double actual_money_e = 0;

	public int word_count = 0;

	public int tm_id = 0;

	public int match_type = 0;

	public String recom_t;

	public String recom_e;

	public boolean add_recom_score = false;

	public boolean defnode;

	public String order_type = ""; // my已抢订单，nt普通订单t,vt 高级订单t,ve高级订单e,rt推送t，rte推送t+e

}
