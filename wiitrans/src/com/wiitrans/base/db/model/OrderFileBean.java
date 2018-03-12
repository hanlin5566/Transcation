package com.wiitrans.base.db.model;

public class OrderFileBean {
	public int file_id = 0;

	public int node_id = 0;

	public int order_id = 0;

	public String originalFileName = "";

	public String source_file_id = "";

	public String b_file_id = "";

	public String preprocess_file_id = "";

	public String trans_file_id = "";

	public String edit_file_id = "";

	public String tmx_file_id = "";

	public int edit_score = 0;

	public int finished_word_count_t = 0;

	public int finished_word_count_e = 0;

	public String preview = "";

	public byte status = 0;

	public boolean analyse;

}
