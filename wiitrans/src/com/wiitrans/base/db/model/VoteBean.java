package com.wiitrans.base.db.model;

public class VoteBean {
    private int mapping_id;
    private int test_ques_id;
    private int type;
    private int strategy_id;
    private int judge;
    public int getMapping_id() {
        return mapping_id;
    }
    public void setMapping_id(int mapping_id) {
        this.mapping_id = mapping_id;
    }
    public int getTest_ques_id() {
        return test_ques_id;
    }
    public void setTest_ques_id(int test_ques_id) {
        this.test_ques_id = test_ques_id;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getStrategy_id() {
        return strategy_id;
    }
    public void setStrategy_id(int strategy_id) {
        this.strategy_id = strategy_id;
    }
    public int getJudge() {
        return judge;
    }
    public void setJudge(int judge) {
        this.judge = judge;
    }
    
}
