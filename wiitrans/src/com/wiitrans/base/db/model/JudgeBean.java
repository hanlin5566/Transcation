package com.wiitrans.base.db.model;

public class JudgeBean {
	private int judge_id;
	private int subjective_id;
	private String answer;
	private int judge;
	public int getJudge_id() {
	    return judge_id;
	}
	public void setJudge_id(int judge_id) {
	    this.judge_id = judge_id;
	}
	public int getSubjective_id() {
	    return subjective_id;
	}
	public void setSubjective_id(int subjective_id) {
	    this.subjective_id = subjective_id;
	}
	public String getAnswer() {
	    return answer;
	}
	public void setAnswer(String answer) {
	    this.answer = answer;
	}
	public int getJudge() {
	    return judge;
	}
	public void setJudge(int judge) {
	    this.judge = judge;
	}
	
}
