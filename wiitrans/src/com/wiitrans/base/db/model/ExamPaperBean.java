package com.wiitrans.base.db.model;

public class ExamPaperBean {
    private int test_id;
    private int translator_id;
    private int pair_id;
    private int industry_id;
    private int score;
    private int step;
    private int times;
    private int create_time;
    
    public int getTranslator_id() {
        return translator_id;
    }
    public void setTranslator_id(int translator_id) {
        this.translator_id = translator_id;
    }
    public int getTest_id() {
        return test_id;
    }
    public void setTest_id(int test_id) {
        this.test_id = test_id;
    }
    public int getPair_id() {
        return pair_id;
    }
    public void setPair_id(int pair_id) {
        this.pair_id = pair_id;
    }
    public int getIndustry_id() {
        return industry_id;
    }
    public void setIndustry_id(int industry_id) {
        this.industry_id = industry_id;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    public int getTimes() {
        return times;
    }
    public void setTimes(int times) {
        this.times = times;
    }
    public int getCreate_time() {
        return create_time;
    }
    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }
}
