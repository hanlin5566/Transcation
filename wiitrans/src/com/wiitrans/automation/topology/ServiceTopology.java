package com.wiitrans.automation.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.wiitrans.automation.bolt.CheckBolt;
import com.wiitrans.automation.bolt.ExecuteBolt;
import com.wiitrans.automation.bolt.ReportBolt;
import com.wiitrans.automation.spout.ServiceSpout;
import com.wiitrans.base.bundle.BundleBolt;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			// AppConfig app = new AppConfig();
			// app.Parse();

			Config conf = new Config();

			boolean isExist = false;
			// BundleParam param = app._bundles.get("autoTopo");
			BundleParam param = WiitransConfig.getInstance(0).AUTO;
			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int checkCount = 2;
			int executeCount = 2;
			int routerCount = 2;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
				case "check": {
					checkCount = bolt.count;
					break;
				}
				case "execute": {
					executeCount = bolt.count;
					break;
				}
				case "report": {
					routerCount = bolt.count;
					break;
				}
				default:
					break;
				}
			}

			TopologyBuilder builder = new TopologyBuilder();

			builder.setSpout("spout", new ServiceSpout(),
					param.BUNDLE_SPOUT_COUNT);

			builder.setBolt("check", new CheckBolt(), checkCount)
					.localOrShuffleGrouping("spout");

			builder.setBolt("execute", new ExecuteBolt(), executeCount)
					.localOrShuffleGrouping("check");

			builder.setBolt("report", new ReportBolt(), routerCount)
					.localOrShuffleGrouping("execute");

			if (param.BUNDLE_IS_LOCALCLUSTER) {
				conf.setMaxTaskParallelism(2);

				LocalCluster cluster = new LocalCluster();
				cluster.submitTopology(param.BUNDLE_NAME, conf,
						builder.createTopology());

				Thread.sleep(600000000);

				cluster.shutdown();
			} else {
				conf.setNumWorkers(param.BUNDLE_WORKER_NUM);

				StormSubmitter.submitTopologyWithProgressBar(param.BUNDLE_NAME,
						conf, builder.createTopology());
			}
			isExist = true;

			if (!isExist) {
				Log4j.error("automation.xml is not exist.");
			}

		} catch (Exception e) {

			Log4j.error(e);
		}
	}

}
