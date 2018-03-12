package com.wiitrans.frag.topology;

import com.wiitrans.base.bundle.BundleBolt;
//import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.frag.bolt.FragHandlerBolt;
import com.wiitrans.frag.bolt.LearnBolt;
import com.wiitrans.frag.spout.ServiceSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			// TODO : Param is may be config.

			// AppConfig app = new AppConfig();
			// app.Parse();

			Config conf = new Config();

			boolean isExist = false;
			// BundleParam param = app._bundles.get("fragTopo");
			BundleParam param = WiitransConfig.getInstance(0).FRAG;
			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int fragSentenceCount = 2;
			int learnCount = 2;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
				case "fragSentence": {
					fragSentenceCount = bolt.count;
					break;
				}
				case "learn": {
					learnCount = bolt.count;
					break;
				}
				default:
					break;
				}
			}

			TopologyBuilder builder = new TopologyBuilder();

			builder.setSpout("spout", new ServiceSpout(),
					param.BUNDLE_SPOUT_COUNT);

			builder.setBolt("fragSentence", new FragHandlerBolt(),
					fragSentenceCount).fieldsGrouping("spout",
					new Fields("fid"));
			builder.setBolt("learn", new LearnBolt(), learnCount)
					.shuffleGrouping("fragSentence");

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
				Log4j.error("frag.xml is not exist.");
			}

		} catch (Exception e) {

			Log4j.error(e);
		}
	}

}
