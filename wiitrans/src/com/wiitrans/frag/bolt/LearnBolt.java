package com.wiitrans.frag.bolt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleBolt;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.LearnApi;
import com.wiitrans.base.db.SentCheckWordDAO;
import com.wiitrans.base.db.VariationCheckWord;
import com.wiitrans.base.http.ICIBAWord;
import com.wiitrans.base.http.TransUtil_ICIBA;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class LearnBolt extends BaseBasicBolt {
	private String iciba_url = null;
	private String auth_key = null;

	public LearnBolt() {
		BundleParam param = WiitransConfig.getInstance(0).FRAG;
		for (LearnApi api : param.BUNDLE_FRAG_LEARN_API) {
			switch (api.name) {
			case "iciba": {
				iciba_url = api.url;
				auth_key = api.key;
				Log4j.info(String
						.format("learnbolt  learnapi  name:%s    url:%s    key:%s",
								api.name, api.url, api.key));
				break;
			}
			default:
				break;
			}
		}
	}

	private void LearnWord(JSONObject obj) {
		JSONObject learn = Util.GetJSonFromJSon("learn", obj);
		if (learn != null) {
			String[] names = JSONObject.getNames(learn);
			if (names != null && names.length > 0) {
				SentCheckWordDAO dao = null;
				VariationCheckWord variation = null;
				try {
					dao = new SentCheckWordDAO();
					dao.Init();
					for (String word : names) {
						if (word == null || word.length() <= 1) {
							continue;
						}
						ICIBAWord icibaword = new ICIBAWord(word, iciba_url,
								auth_key);
						try {
							TransUtil_ICIBA.translate(icibaword);
						} catch (Exception e) {
							Log4j.error("handle word:" + word + " error retry",
									e);
							try {
								TransUtil_ICIBA.translate(icibaword);
							} catch (Exception e1) {
								Log4j.error("handle word:" + word
										+ " error retry", e);
							}
						}
						if (icibaword != null && icibaword.isWord) {
							Log4j.info(String
									.format("word:%s    pl:%s    past:%s    done:%s    ing:%s    third:%s    er:%s    est:%s",
											icibaword.source, icibaword.pl,
											icibaword.past, icibaword.done,
											icibaword.ing, icibaword.third,
											icibaword.er, icibaword.est));
							List<VariationCheckWord> varlist = new ArrayList<VariationCheckWord>();
							if (StringUtils.isNotEmpty(icibaword.pl)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.pl;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_PL;
								varlist.add(variation);
							}
							if (StringUtils.isNotEmpty(icibaword.past)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.past;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_PAST;
								varlist.add(variation);
							}
							if (StringUtils.isNotEmpty(icibaword.done)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.done;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_DONE;
								varlist.add(variation);
							}
							if (StringUtils.isNotEmpty(icibaword.ing)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.ing;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_ING;
								varlist.add(variation);
							}
							if (StringUtils.isNotEmpty(icibaword.third)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.third;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_THIRD;
								varlist.add(variation);
							}
							if (StringUtils.isNotEmpty(icibaword.er)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.er;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_ER;
								varlist.add(variation);
							}
							if (StringUtils.isNotEmpty(icibaword.est)) {
								variation = new VariationCheckWord();
								variation.word = icibaword.source;
								variation.variation = icibaword.est;
								variation.type = TransUtil_ICIBA.EXCHANGE_TYPE_EST;
								varlist.add(variation);
							}
							if (varlist.size() > 0) {
								dao.InsertVariation(varlist);
							}
							dao.InsertWord(icibaword.source);
							dao.Commit();
						}
					}
				} catch (Exception e) {
					Log4j.error(e);
				} finally {
					if (dao != null) {
						dao.UnInit();
					}
				}

			}
		}
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		JSONObject obj = new JSONObject(tuple.getStringByField("content"));
		String aid = Util.GetStringFromJSon("aid", obj);

		Log4j.debug("learn " + obj.toString());
		try {

			switch (aid) {
			case "syn": {
				LearnWord(obj);
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			Log4j.error(e);
		}

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

}
