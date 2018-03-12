package com.wiitrans.msg.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import com.wiitrans.base.bundle.BundleBolt;
//import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.msg.bolt.AuthorityBolt;
import com.wiitrans.msg.bolt.FilterBolt;
import com.wiitrans.msg.bolt.PersistenceBolt;
import com.wiitrans.msg.bolt.RouterBolt;
import com.wiitrans.msg.spout.ServiceSpout;

public class ServiceTopology {

	public static void main(String[] args) {

		try {

			// AppConfig app = new AppConfig();
			// app.Parse();

			Config conf = new Config();

			boolean isExist = false;
			// BundleParam param = app._bundles.get("msgTopo");
			BundleParam param = WiitransConfig.getInstance(0).MSG;
			if (param.BUNDLE_IS_DEBUG) {
				conf.setDebug(true);
			} else {
				conf.setDebug(false);
			}

			int authorityCount = 2;
			int filterCount = 2;
			int routerCount = 2;
			int persistenceCount = 2;
			for (BundleBolt bolt : param.BUNDLE_BOLT_COUNT) {
				switch (bolt.name) {
				case "authority": {
					authorityCount = bolt.count;
					break;
				}
				case "filter": {
					filterCount = bolt.count;
					break;
				}
				case "router": {
					routerCount = bolt.count;
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

			builder.setBolt("authority", new AuthorityBolt(), authorityCount)
					.localOrShuffleGrouping("spout");

			builder.setBolt("filter", new FilterBolt(), filterCount)
					.localOrShuffleGrouping("authority");

			builder.setBolt("router", new RouterBolt(), routerCount)
					.localOrShuffleGrouping("filter");

			builder.setBolt("persistence", new PersistenceBolt(),
					persistenceCount).fieldsGrouping("router",
					new Fields("room_id"));

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
