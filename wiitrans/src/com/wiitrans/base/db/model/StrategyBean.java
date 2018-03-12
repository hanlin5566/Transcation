package com.wiitrans.base.db.model;

public class StrategyBean {
    private int strategy_id;
    private int subjective_id;
    private String answer;
    private int auto_judge;
    private int judge_time;
    private int create_time;
    
    public int getStrategy_id() {
        return strategy_id;
    }
    public void setStrategy_id(int strategy_id) {
        this.strategy_id = strategy_id;
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
    public int getAuto_judge() {
        return auto_judge;
    }
    public void setAuto_judge(int auto_judge) {
        this.auto_judge = auto_judge;
    }
    public int getJudge_time() {
        return judge_time;
    }
    public void setJudge_time(int judge_time) {
        this.judge_time = judge_time;
    }
    public int getCreate_time() {
        return create_time;
    }
    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }
    
}
