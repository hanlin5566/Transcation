package com.wiitrans.base.db.model;

import java.util.Date;

public class TransGradeBean {

	public int translator_grade_id;
	// 译员ID
	public int translator_id;
	// 级别ID
	public int grade_id;

	public boolean editor;
	// 语言对ID
	public int pair_id;

	public int slang_id = 0;

	public int tlang_id = 0;
	// 考试通过时间
	public Date qualified_time;

	public int effective_word_count = 0;

	public int pair_word_count = 0;

	public int industry_id = 0;

	public String industry_ids = "";

	public String industry_grade_editor = "";
}
