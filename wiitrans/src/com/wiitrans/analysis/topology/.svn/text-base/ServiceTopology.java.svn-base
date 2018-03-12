package com.wiitrans.analysis.topology;

import com.wiitrans.analysis.bolt.CalculateMoneyBolt;
import com.wiitrans.analysis.bolt.SplitHandlerBolt;
import com.wiitrans.analysis.spout.ServiceSpout;
import com.wiitrans.base.bundle.BundleBolt;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.xml.WiitransConfig;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			Config conf = new Config();

			boolean isExist = false;
			// BundleParam param = app._bundles.get("anlyTopo");
			BundleParam param = WiitransConfig.getInstance(0).ANALY;
			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int calculateCount = 2;
			int splitCount = 2;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
				case "calculate": {
					calculateCount = bolt.count;
					break;
				}
				case "split": {
					splitCount = bolt.count;
					break;
				}
				default:
					break;
				}
			}

			TopologyBuilder builder = new TopologyBuilder();

			builder.setSpout("spout", new ServiceSpout(),
					param.BUNDLE_SPOUT_COUNT);

			builder.setBolt("calculate", new CalculateMoneyBolt(),
					calculateCount).localOrShuffleGrouping("spout");

			builder.setBolt("split", new SplitHandlerBolt(), splitCount)
					.localOrShuffleGrouping("calculate");

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
				Log4j.error("anly.xml is not exist.");
			}

		} catch (Exception e) {

			Log4j.error(e);
		}
	}

}
