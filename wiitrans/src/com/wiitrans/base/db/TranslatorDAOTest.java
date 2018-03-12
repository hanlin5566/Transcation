package com.wiitrans.base.db;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.model.ProcRecomOrderBean;
import com.wiitrans.base.db.model.TMBean;
import com.wiitrans.base.file.lang.TmxFileChunk;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.tm.TMENUS;
import com.wiitrans.base.tm.TMLanguage;
import com.wiitrans.base.tm.TMResult;
import com.wiitrans.base.xml.WiitransConfig;

public class TranslatorDAOTest {
	public static void main(String[] args) {
		WiitransConfig.getInstance(0);
		ProcCreateOrderDAO dao = new ProcCreateOrderDAO();
		dao.Init(true);
		//dao.CreateOrderMlv( p_word_count, p_analyse_word_count, p_currency_id, p_total_money, expected_delivery_time, expected_delivery_time_t, expected_delivery_time_e, p_analyse, p_description, p_sql, p_tm_id, p_tg_id, p_cat_ids, p_name, p_file_id)
		dao.CreateOrderMlv(6, 1, 55555, 3, 0, 0, 12345, 12345, 1, 1234,
				1474599855, 1474598855, 1474599855, false, "订单备注啊", "", 0, 0,
				"1,3", "", "");
		dao.Commit();
		//"pair_id":"6","industry_id":"1","price_level_id":"1","word_count":"12345","analyse_word_count":"0","currency_id":"1","total_money":"1234","expected_delivery_time":"2016-09-25 10:10:10","expected_delivery_time_t":"2016-09-25 10:10:10","expected_delivery_time_e":"0"
		System.out.println();

	}

	public static void mainasdsdf(String[] args) {
		WiitransConfig.getInstance(0);

		// String cmd = "jps -mlvV | grep tmsvr.jar";
		String cmd = "jps";
		ArrayList<String> ary = Util.exeCmdForResult(cmd);
		if (ary != null && ary.size() > 0) {
			for (String string : ary) {
				System.out.println(string);
			}
		}

		int tmID = 1234;
		// AppConfig app = new AppConfig();
		// app.Parse();

		TMDAO dao = new TMDAO();
		dao.Init(false);
		List<Integer> listint = dao.Deletable(new int[] {});

		TMLanguage lang = new TMENUS();
		// lang.ParseChunk(tmID);
		// TmxFileChunk tmxFile = new TmxFileChunk();
		lang.ReadTMChunk(tmID);
		ArrayList<TMResult> list = lang.SearchTMChunk(tmID,
				"$22: VIN(Vehicle iddentification number) Original ($F190)");
		// dao.Commit();
		System.out.println("adfsasdf");
	}

	public static void mainanalysetm(String[] args) {
		int tmID = 1234;
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		TMServiceDAO dao = new TMServiceDAO();
		dao.Init();
		dao.DropTMIndexIfExists(tmID);
		dao.CreateTMIndex(tmID);
		dao.DropTMWordIfExists(tmID);
		dao.CreateTMWord(tmID);
		dao.DropTMTimeIfExists(tmID);
		dao.CreateTMTime(tmID);
		dao.DropTMTextIfExists(tmID);
		dao.CreateTMText(tmID);

		TMLanguage lang = new TMENUS();
		// lang.ParseChunk(tmID);
		TmxFileChunk tmxFile = new TmxFileChunk();
		lang.Init(tmxFile);
		tmxFile.Init("/root/Desktop/tm/51.tmx", "/tmp/", "", "", lang, 1000,
				tmID);
		tmxFile.AnalyseTMXFile(0);

		if (lang != null && lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
			dao.CleanUpTM(tmID, 0);
		} else {
			dao.CleanUpTM(tmID, 1);
		}

		// dao.Commit();

	}

	public static void main123123(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		// BundleParam param = app._bundles.get("recomTopo");
		BundleParam param = WiitransConfig.getInstance(0).RECOM;
		ProcRecomOrderEmailDAO dao = new ProcRecomOrderEmailDAO();
		dao.Init(true);
		HashMap map = new HashMap<String, Object>();
		// p_node_id INT,p_order_id INT,p_maxcount INT,p_match_industry BIT
		map.put("p_node_id", BundleConf.DEFAULT_NID);
		map.put("p_order_id", 1072);
		map.put("p_maxcount", param.BUNDLE_TRANSLATOR_ORDER_MAXCOUNT);
		map.put("p_match_industry", BundleConf.BUNDLE_MATCH_INDUSTRY);
		List<ProcRecomOrderBean> list = dao.RecomOrderEmail(map);
		System.out.println();
	}

	public static void main1(String[] args) {

		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.FLOOR);
		System.out.print(df.format(0.125));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		System.out.print(sdf.format(Calendar.getInstance().getTime()));

		TMDAO dao = new TMDAO();

		dao.Init(true);
		TMBean aa = dao.SelectForTMID(1);
		dao.UnInit();

		// TermDetailsBean termDetails = new TermDetailsBean();
		// termDetails.term_id = 4;
		// termDetails.translator_id = 4;
		// termDetails.meaning = "内部存储器";
		// termDetails.usage = "内部";
		// termDetails.remark = "存储器";
		// TermDAO dao = new TermDAO();
		//
		// dao.Init(true);
		// dao.InsertTermDetails(termDetails);
		// dao.Commit();
		// dao.UnInit();
	}

	public static void main__(String[] args) {
		// RoomDAO dao = new RoomDAO();
		// dao.Init(true);
		// Map<String, Object> param = new HashMap<String, Object>();
		// param.put("order_id", 123456);
		// List<Integer> list = new ArrayList<Integer>();
		// list.add(1233);
		// list.add(3221);
		// param.put("user_ids", list);
		// //List<Map<String, Object>> result = dao.selectRoom(param);
		// dao.UnInit();
	}

	public static void main22(String[] args) {
		int o_error = 100;
		ProcTransSettlementDAO dao = new ProcTransSettlementDAO();

		dao.Init(true);
		HashMap map = new HashMap<String, Object>();
		map.put("p_node_id", 1);
		map.put("p_code", "asdf");
		map.put("o_error", o_error = 100);

		// dao.Settlement(1, "", o_error);
		dao.Settlement(map);
		dao.UnInit();

		// ProcPreprocessDAO dao = new ProcPreprocessDAO();
		//
		// dao.Init(true);
		// Map map = new HashMap<String, Object>();
		// map.put("p_order_id", 454);
		// List<List<?>> list = dao.PreprocessSelect(map);
		// dao.UnInit();

		// ProcLoadFileDAO dao = new ProcLoadFileDAO();
		//
		// dao.Init(true);
		// Map map = new HashMap<String, Object>();
		// map.put("p_file_id", 1419);
		// List<List<?>> list = dao.LoadFileSelect(map);
		// dao.UnInit();

		// TranslatorDAO dao = new TranslatorDAO();
		// dao.Init(true);
		// TranslatorBean tran = dao.Select(1);
		// dao.UnInit();
	}

}
