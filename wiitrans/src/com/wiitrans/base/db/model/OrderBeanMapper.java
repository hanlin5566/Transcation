package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface OrderBeanMapper {

	public List<OrderBean> SelectOrdersByStatus();
	public List<OrderBean> SelectReservedOrder();
	
	public List<Map<String, String>> SelectStayOrdersByTransLator(Map<String, Object> translator);
	public List<Map<String, String>> SelectStayOrdersByEditor(Map<String, Object> translator);

	public OrderBean Select(String ordercode);

	public OrderBean SelectForNode(String ordercode);

	public OrderBean SelectByID(int order_id);

	public Map<String, Object> SelectUserPayment(int order_id);

	public Map<String, Object> SelectPersonnalQuotationOrder(String order_code);

	public void UpdateWordCount(int order_id);

	public void UpdateWordCountForNode(Integer nid, int order_id);

	public void UpdateGetTimeT(int order_id);

	public void UpdateGetTimeE(int order_id);

	public void UpdateExpectedDeliveryTime(OrderBean order);

	public void UpdatePreprocess(int order_id);

	public void UpdateTrans(OrderBean order);

	public void UpdateEditor(OrderBean order);

	public void UpdateStatus(OrderBean order);

	public void UpdateStatusForNode(OrderBean order);

	public void UpdateFileStatus(int order_id);

	public void UpdateAddRecomScore(OrderBean order);
}
