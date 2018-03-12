package com.wiitrans.base.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wiitrans.base.db.VariationCheckWord;

public interface SentCheckWordServiceMapper {
	public void ImportSentCheckWord(Map<String, Object> map);

	public void DeleteSentCheckWord(int fid, int sent_id);

	public void ImportUnknown(Map<String, Object> map);

	public ArrayList<SentCheckWordBean> Check(int fid, int sent_id);

	public ArrayList<SentCheckWordBean> CheckVariation(int fid, int sent_id);

	public ArrayList<SentCheckWordBean> CheckSymbol(int fid, int sent_id);

	public void InsertWord(String word);

	public void InsertVariation(List<VariationCheckWord> list);
}
