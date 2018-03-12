package com.wiitrans.base.db;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.OrderBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class OrderDAO extends CommonDAO {

	private OrderBeanMapper _mapper = null;

	public int Init(Boolean loadConf, JSONObject obj) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf, obj);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(OrderBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(OrderBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<OrderBean> SelectOrdersByStatus() {
		return _mapper.SelectOrdersByStatus();
	}
	public List<OrderBean> SelectReservedOrder() {
	    return _mapper.SelectReservedOrder();
	}
	
	public List<Map<String, String>> SelectStayOrdersByTransLator(Map<String, Object> translator) {
	    return _mapper.SelectStayOrdersByTransLator(translator);
	}
	public List<Map<String, String>> SelectStayOrdersByEditor(Map<String, Object> translator) {
	    return _mapper.SelectStayOrdersByEditor(translator);
	}

	public OrderBean Select(String code) {
		return _mapper.Select(code);
	}

	public OrderBean SelectForNode(String code) {
		return _mapper.SelectForNode(code);
	}

	public OrderBean Select(int order_id) {
		return _mapper.SelectByID(order_id);
	}

	public void UpdateWordCount(int order_id) {
		_mapper.UpdateWordCount(order_id);
	}

	public void UpdateWordCountForNode(int nid, int order_id) {
		_mapper.UpdateWordCountForNode(nid, order_id);
	}

	public void UpdateGetTimeT(int order_id) {
		_mapper.UpdateGetTimeT(order_id);
	}

	public void UpdateGetTimeE(int order_id) {
		_mapper.UpdateGetTimeE(order_id);
	}

	public void UpdateExpectedDeliveryTime(OrderBean order) {
		_mapper.UpdateExpectedDeliveryTime(order);
	}

	public void UpdatePreprocess(int order_id) {
		_mapper.UpdatePreprocess(order_id);
	}

	public void UpdateTrans(OrderBean order) {
		_mapper.UpdateTrans(order);
	}

	public void UpdateEditor(OrderBean order) {
		_mapper.UpdateEditor(order);
	}

	public void UpdateStatus(OrderBean order) {
		_mapper.UpdateStatus(order);
	}

	public void UpdateStatusForNode(OrderBean order) {
		_mapper.UpdateStatusForNode(order);
	}

	public void UpdateFileStatus(int order_id) {
		_mapper.UpdateFileStatus(order_id);
	}

	public void UpdateAddRecomScore(OrderBean order) {
		_mapper.UpdateAddRecomScore(order);
	}

	public Map<String, Object> SelectUserPayment(int order_id) {
		return _mapper.SelectUserPayment(order_id);
	}

	public Map<String, Object> SelectPersonnalQuotationOrder(String order_code) {
		return _mapper.SelectPersonnalQuotationOrder(order_code);
	}
}
