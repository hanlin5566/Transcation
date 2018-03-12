package com.wiitrans.base.term;

import java.util.ArrayList;

public class Term {
	public int _pair_id = 0;
	// public int _sourcelang = 0;
	// public int _targetlang = 0;
	public int _industryId = 0;
	public String _term = null; // 原文
	public int create_time = 0;
	// public HashMap<Integer, TermMeta> _meta = null;
	public ArrayList<TermMeta> _meta = null;

	public int term_id = 0;
	public int check_begin = -1;
	public int check_end = -1;
	public int check_count = 1;
	//public boolean check = false;
	public int check_number = 0;//匹配个数
	public String check_meaning = null; // 译文
	public String check_usage = null; // 用法
	public String check_remark = null; // 备注

	public void Uninit() {
		_term = null;
		check_meaning = null;
		check_usage = null;
		check_remark = null;
	}
}
