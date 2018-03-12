package com.wiitrans.base.db.model;

public interface TermBeanMapper {
	public TermBean SelectByTerm(TermBean term);

	public void Insert(TermBean term);
}
