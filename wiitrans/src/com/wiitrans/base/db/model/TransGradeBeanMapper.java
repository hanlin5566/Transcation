package com.wiitrans.base.db.model;

import java.util.List;

public interface TransGradeBeanMapper {
	public List<TransGradeBean> SelectForTrans(int uid);

	public TransGradeBean SelectForTransPair(TransGradeBean grade);

	public List<TransGradeBean> SelectAll();

	public List<TransGradeBean> SelectAllForGradeNew();

	public List<TransGradeBean> SelectByTransID(int transID);

	public List<TransGradeBean> SelectForGradeNewByTransID(int transID);
}
