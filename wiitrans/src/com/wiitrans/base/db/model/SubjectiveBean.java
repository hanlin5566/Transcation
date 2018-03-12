package com.wiitrans.base.db.model;

public class SubjectiveBean {
    private int subjective_id;
    private int pair_id;
    private int industry_id;
    private String question;
    public int getSubjective_id() {
        return subjective_id;
    }
    public void setSubjective_id(int subjective_id) {
        this.subjective_id = subjective_id;
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
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    
}
