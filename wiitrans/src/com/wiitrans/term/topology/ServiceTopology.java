package com.wiitrans.term.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.wiitrans.base.bundle.BundleBolt;
//import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.term.bolt.PersistenceBolt;
import com.wiitrans.term.bolt.TermBolt;
import com.wiitrans.term.spout.ServiceSpout;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			// TODO : Param is may be config.

//			AppConfig app = new AppConfig();
//			app.Parse();

			Config conf = new Config();

			boolean isExist = false;
//			BundleParam param = app._bundles.get("termTopo");
			BundleParam param = WiitransConfig.getInstance(0).TERM;

			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int termHandlerCount = 2;
			int persistenceCount = 1;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
				case "termHandler": {
					termHandlerCount = bolt.count;
					break;
				}
				case "persistence": {
					persistenceCount = bolt.count;
					break;
				}
				default:
					break;
				}
			}

			TopologyBuilder builder = new TopologyBuilder();

			builder.setSpout("spout", new ServiceSpout(),
					param.BUNDLE_SPOUT_COUNT);

			builder.setBolt("termHandler", new TermBolt(), termHandlerCount)
					.localOrShuffleGrouping("spout");

			builder.setBolt("persistence", new PersistenceBolt(),
					persistenceCount).localOrShuffleGrouping("termHandler");

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
				Log4j.error("term.xml is not exist.");
			}

		} catch (Exception e) {

			Log4j.error(e);
		}
	}
}