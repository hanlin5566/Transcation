package com.wiitrans.base.db;

import java.util.List;
import com.wiitrans.base.db.model.TranslatorBean;
import com.wiitrans.base.db.model.TranslatorBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TranslatorDAO extends CommonDAO {

	private TranslatorBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(TranslatorBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public TranslatorBean Select(int uid) {
		return _mapper.Select(uid);
	}

	public List<TranslatorBean> SelectAll() {
		return _mapper.SelectAll();
	}

	public List<TranslatorBean> SelectInternal() {
		return _mapper.SelectInternal();
	}

	// TODO:此处需要增加调用成功失败返回值
	public void Delete(int uid) {
		_mapper.Delete(uid);
	}

	// TODO:此处需要增加调用成功失败返回值
	public void Insert(TranslatorBean tran) {
		_mapper.Insert(tran);
	}

	// TODO:此处需要增加调用成功失败返回值
	public void Update(TranslatorBean tran) {
		_mapper.Update(tran);
	}
	
	public TranslatorBean SelectUserId(TranslatorBean tran) {
		return _mapper.SelectUserId(tran);
	}
	
	public List<TranslatorBean> SelectUserIds(List<String> userIds) {
		return _mapper.SelectUserIds(userIds);
	}
}
