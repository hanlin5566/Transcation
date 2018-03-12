package com.wiitrans.base.db.model;

import java.util.List;


public interface ExamQuesBeanMapper {
    public int insertExamQues(ExamQuesBean ques);
    public void deleteUnFinishedExamQues(int paperId);
    public ExamQuesBean selectSubjectByStrategyId(int strategy_id);
    public List<ExamQuesBean> selectStrategyByExamId(int paperId);
}
