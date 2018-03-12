package com.wiitrans.base.db.model;

import java.util.List;

public interface OrderFileBeanMapper {

	public OrderFileBean Select(int file_id);

	public OrderFileBean SelectForNode(int fnid, int file_id);

	public List<OrderFileBean> SelectByOrderID(int order_id);

	public List<OrderFileBean> SelectByOrderIDForNode(int nid, int order_id);

	public void UpdateWordCount(OrderFileBean orderFile);

	public void UpdateWordCountForNode(OrderFileBean orderFile);

	public void UpdatePreview(OrderFileBean orderFile);

	public void UpdatePreviewForNode(OrderFileBean orderFile);

	public void UpdateEditScore(OrderFileBean orderFile);

	public void UpdateEditScoreForNode(OrderFileBean orderFile);

	public void UpdateBiliFile(OrderFileBean orderFile);

	public void UpdateBiliFileForNode(OrderFileBean orderFile);

	public void UpdatePreprocessFile(OrderFileBean orderFile);

	public void UpdatePreprocessFileForNode(OrderFileBean orderFile);

	public void UpdateTransFile(OrderFileBean orderFile);

	public void UpdateTransFileForNode(OrderFileBean orderFile);

	public void UpdateEditFile(OrderFileBean orderFile);

	public void UpdateEditFileForNode(OrderFileBean orderFile);

	public void UpdateTmxFile(OrderFileBean orderFile);

	public void UpdateTmxFileForNode(OrderFileBean orderFile);
}
