package com.wiitrans.base.db.model;

public interface TermDetailsEvaBeanMapper {
	public TermDetailsEvaBean Select(TermDetailsEvaBean termEvaluation);

	public void Insert(TermDetailsEvaBean termEvaluation);
	public void Update(TermDetailsEvaBean termEvaluation);
	public void Delete(int term_evaluation_id);
}
