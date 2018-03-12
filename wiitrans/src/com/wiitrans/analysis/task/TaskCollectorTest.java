package com.wiitrans.analysis.task;

import java.util.List;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.db.DictRateDAO;
import com.wiitrans.base.db.model.DictRateBean;
import com.wiitrans.base.interproc.ThreadUtility;
import com.wiitrans.base.task.TaskCollector;

public class TaskCollectorTest {

	public static void main(String[] args) {

		DictRateDAO dao = new DictRateDAO();
		dao.Init(true);
		List<DictRateBean> list = dao.SelectAll();
		for (DictRateBean dictRateBean : list) {

		}
	}

	public static void main_(String[] args) {

		TaskCollector coll = new TaskCollector(BundleConf.BUNDLE_REPORT_IP, 10001,
				"analysisfile");
		coll.Start();
		coll.Register();

		ThreadUtility.Sleep(100000000);
	}
}
