package com.wiitrans.oc.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.wiitrans.base.bundle.BundleBolt;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.oc.bolt.OrderBolt;
import com.wiitrans.oc.bolt.TranslatorBolt;
import com.wiitrans.oc.spout.ServiceSpout;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			Config conf = new Config();

			boolean isExist = false;
			BundleParam param = WiitransConfig.getInstance(0).OC;
			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int translatorCount = 2;
			int orderCount = 2;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
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

			builder.setBolt("translator", new TranslatorBolt(), translatorCount)
					.fieldsGrouping("spout", new Fields("uid"));

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
				Log4j.error("oc.xml is not exist.");
			}

		} catch (Exception e) {

			Log4j.error(e);
		}
	}

}
