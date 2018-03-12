package com.wiitrans.recom.topology;

import com.wiitrans.base.bundle.BundleBolt;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.recom.bolt.MatchingBolt;
import com.wiitrans.recom.bolt.OrderBolt;
import com.wiitrans.recom.bolt.TranslatorBolt;
import com.wiitrans.recom.spout.ServiceSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			// Param is may be config.

			// AppConfig app = new AppConfig();
			// app.Parse();

			Config conf = new Config();

			boolean isExist = false;
			// BundleParam param = app._bundles.get("recomTopo");
			BundleParam param = WiitransConfig.getInstance(0).RECOM;
			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int languageCount = 2;
			int translatorCount = 2;
			int orderCount = 2;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
				case "language": {
					languageCount = bolt.count;
					break;
				}
				case "translator": {
					translatorCount = bolt.count;
					break;
				}
				case "order": {
					orderCount = bolt.count;
					break;
				}
				default:
					break;
				}
			}

			TopologyBuilder builder = new TopologyBuilder();

			builder.setSpout("spout", new ServiceSpout(),
					param.BUNDLE_SPOUT_COUNT);

			builder.setBolt("language", new MatchingBolt(), languageCount)
					.fieldsGrouping("spout", new Fields("langpair"));

			builder.setBolt("translator", new TranslatorBolt(), translatorCount)
					.fieldsGrouping("language", new Fields("uid"));

			builder.setBolt("order", new OrderBolt(), orderCount)
					.fieldsGrouping("translator", new Fields("ordercode"));

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
				Log4j.error("recom.xml is not exist.");
			}

		} catch (Exception e) {

			Log4j.error(e);
		}
	}

}
