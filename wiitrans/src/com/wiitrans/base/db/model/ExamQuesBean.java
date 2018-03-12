package com.wiitrans.base.db.model;

public class ExamQuesBean {
    private int test_ques_id;
    private int test_id;
    private int question_id;
    private int type;
    private int step;
    private int judge;
    private int score;
    public int getTest_ques_id() {
        return test_ques_id;
    }
    public void setTest_ques_id(int test_ques_id) {
        this.test_ques_id = test_ques_id;
    }
    public int getTest_id() {
        return test_id;
    }
    public void setTest_id(int test_id) {
        this.test_id = test_id;
    }
    public int getQuestion_id() {
        return question_id;
    }
    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    public int getJudge() {
        return judge;
    }
    public void setJudge(int judge) {
        this.judge = judge;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    
    
}
