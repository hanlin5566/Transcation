package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.OrderFileBean;
import com.wiitrans.base.db.model.OrderFileBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class OrderFileDAO extends CommonDAO {

	private OrderFileBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(OrderFileBeanMapper.class);
				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public OrderFileBean Select(int file_id) {
		return _mapper.Select(file_id);
	}

	public OrderFileBean SelectForNode(int fnid, int file_id) {
		return _mapper.SelectForNode(fnid, file_id);
	}

	public List<OrderFileBean> SelectByOrderID(int order_id) {
		return _mapper.SelectByOrderID(order_id);
	}

	public List<OrderFileBean> SelectByOrderIDForNode(int nid, int order_id) {
		return _mapper.SelectByOrderIDForNode(nid, order_id);
	}

	public void UpdateWordCount(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateWordCount(orderfile);
		}
	}

	public void UpdateWordCountForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateWordCountForNode(orderfile);
		}
	}

	public void UpdatePreview(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdatePreview(orderfile);
		}
	}

	public void UpdatePreviewForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdatePreviewForNode(orderfile);
		}
	}

	public void UpdateEditScore(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateEditScore(orderfile);
		}
	}

	public void UpdateEditScoreForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateEditScoreForNode(orderfile);
		}
	}

	public void UpdateBiliFile(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateBiliFile(orderfile);
		}
	}

	public void UpdateBiliFileForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateBiliFileForNode(orderfile);
		}
	}

	public void UpdatePreprocessFile(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdatePreprocessFile(orderfile);
		}
	}

	public void UpdatePreprocessFileForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdatePreprocessFileForNode(orderfile);
		}
	}

	public void UpdateTransFile(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateTransFile(orderfile);
		}
	}

	public void UpdateTransFileForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateTransFileForNode(orderfile);
		}
	}

	public void UpdateEditFile(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateEditFile(orderfile);
		}
	}

	public void UpdateEditFileForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateEditFileForNode(orderfile);
		}
	}

	public void UpdateTmxFile(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateTmxFile(orderfile);
		}
	}

	public void UpdateTmxFileForNode(OrderFileBean orderfile) {
		if (orderfile != null && orderfile.file_id > 0) {
			_mapper.UpdateTmxFileForNode(orderfile);
		}
	}
}
