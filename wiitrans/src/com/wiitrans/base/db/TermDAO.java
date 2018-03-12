package com.wiitrans.base.db;

import com.wiitrans.base.db.model.TermBean;
import com.wiitrans.base.db.model.TermBeanMapper;
import com.wiitrans.base.db.model.TermDetailsBean;
import com.wiitrans.base.db.model.TermDetailsBeanMapper;
import com.wiitrans.base.db.model.TermDetailsEvaBean;
import com.wiitrans.base.db.model.TermDetailsEvaBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TermDAO extends CommonDAO {

	private TermBeanMapper _termmapper = null;
	private TermDetailsBeanMapper _termdetailsmapper = null;
	private TermDetailsEvaBeanMapper _termdetailsevamapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_termmapper = _session.getMapper(TermBeanMapper.class);
				_termdetailsmapper = _session
						.getMapper(TermDetailsBeanMapper.class);
				_termdetailsevamapper = _session
						.getMapper(TermDetailsEvaBeanMapper.class);

				if (_termmapper != null && _termdetailsmapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public TermBean SelectTermByTerm(TermBean term) {
		return _termmapper.SelectByTerm(term);
	}

	public void InsertTerm(TermBean term) {
		_termmapper.Insert(term);
	}

	public TermDetailsBean SelectTermDetails(TermDetailsBean termDetails) {
		return _termdetailsmapper.SelectTermDetails(termDetails);
	}

	public void InsertTermDetails(TermDetailsBean termDetails) {
		_termdetailsmapper.Insert(termDetails);
	}

	public TermDetailsEvaBean SelectTermDetailsEva(
			TermDetailsEvaBean termDetailsEva) {
		return _termdetailsevamapper.Select(termDetailsEva);
	}

	public void InsertTermDetailsEva(TermDetailsEvaBean termDetailsEva) {
		_termdetailsevamapper.Insert(termDetailsEva);
	}
	
	public void UpdateTermDetailsEva(TermDetailsEvaBean termDetailsEva) {
		_termdetailsevamapper.Update(termDetailsEva);
	}
	
	public void DeleteTermDetailsEva(int term_evaluation_id) {
		_termdetailsevamapper.Delete(term_evaluation_id);
	}
}
