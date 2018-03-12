package com.wiitrans.base.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wiitrans.base.db.model.TMBean;
import com.wiitrans.base.db.model.TMBeanMapper;
import com.wiitrans.base.db.model.TMServiceTextBean;
import com.wiitrans.base.db.model.TMVariantBean;
import com.wiitrans.base.db.model.TMVariantBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TMDAO extends CommonDAO {

	private TMBeanMapper _mapper = null;
	private TMVariantBeanMapper _mappervariant = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(TMBeanMapper.class);
				_mappervariant = _session.getMapper(TMVariantBeanMapper.class);

				if (_mapper != null && _mappervariant != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<TMBean> SelectAll() {
		return _mapper.SelectAll();
	}

	public TMBean SelectForTMID(int tmid) {
		return _mapper.SelectForTMID(tmid);
	}

	public TMBean SelectForNodeTMID(int tmid) {
		return _mapper.SelectForNodeTMID(tmid);
	}

	public void UpdateAnalyse(TMBean tm) {
		_mapper.UpdateAnalyse(tm);
	}

	public void UpdateAnalyseForNode(TMBean tm) {
		_mapper.UpdateAnalyseForNode(tm);
	}

	public void Delete(int tmid) {
		_mapper.Delete(tmid);
	}

	public void DeleteForNode(int tmid) {
		_mapper.DeleteForNode(tmid);
	}

	public List<TMVariantBean> SelectVariant(int lang_id) {
		return _mappervariant.Select(lang_id);
	}

	public List<Integer> Deletable(int[] tmIDs) {
		if (tmIDs == null || tmIDs.length == 0) {
			return new ArrayList<Integer>();
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tmIDs", tmIDs);
			return _mapper.Deletable(map);
		}
	}
}