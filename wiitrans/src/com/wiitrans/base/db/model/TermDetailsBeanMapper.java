package com.wiitrans.base.db.model;

public interface TermDetailsBeanMapper {
	public TermDetailsBean SelectTermDetails(TermDetailsBean termDetails);

	public void Insert(TermDetailsBean termDetails);
}
