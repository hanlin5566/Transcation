package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.TransGradeBean;
import com.wiitrans.base.db.model.TransGradeBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TransGradeDAO extends CommonDAO {

	private TransGradeBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(TransGradeBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<TransGradeBean> SelectForTrans(int uid) {
		return _mapper.SelectForTrans(uid);
	}

	public TransGradeBean SelectForTransPair(TransGradeBean grade) {
		return _mapper.SelectForTransPair(grade);
	}

	public List<TransGradeBean> SelectAll() {
		return _mapper.SelectAll();
	}

	public List<TransGradeBean> SelectAllForGradeNew() {
		return _mapper.SelectAllForGradeNew();
	}

	public List<TransGradeBean> SelectByTransID(int transID) {
		return _mapper.SelectByTransID(transID);
	}

	public List<TransGradeBean> SelectForGradeNewByTransID(int transID) {
		return _mapper.SelectForGradeNewByTransID(transID);
	}

}
