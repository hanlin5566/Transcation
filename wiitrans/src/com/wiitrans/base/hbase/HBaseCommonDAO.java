package com.wiitrans.base.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class HBaseCommonDAO {

	private Configuration conf;
	private Connection conn;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			if (loadConf) {
				// AppConfig app = new AppConfig();
				// ret = app.Parse();
				WiitransConfig.getInstance(0);
			}

			if (!BundleConf.HBASE_CONF_URL.isEmpty()) {
				conf = HBaseConfiguration.create();
				conf.set("hbase.zookeeper.property.clientPort",
						HbaseConfig.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT);
				conf.set("hbase.zookeeper.quorum",
						HbaseConfig.HBASE_ZOOKEEPER_QUORUM);
				// conf.set("hbase.master", HbaseConfig.HBASE_MASTER);
				ret = Const.SUCCESS;
				try {
					conn = ConnectionFactory.createConnection(conf);
				} catch (Exception e) {
					Log4j.error(e);
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public boolean tableExists(String tableName) {
		try {
			Admin admin = conn.getAdmin();
			TableName table = TableName.valueOf(tableName);
			return admin.tableExists(table);
		} catch (Exception e) {
			Log4j.error(e);
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ie) {
				Log4j.error(ie);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param tableName
	 *            表名
	 * @param afresh
	 *            是否重新创建
	 * @param familyNames
	 *            列簇名
	 */
	public void createTable(String tableName, boolean afresh,
			String... familyNames) {
		try {

			Admin admin = conn.getAdmin();
			TableName table = TableName.valueOf(tableName);
			boolean isExists = admin.tableExists(table);// true false
			if (isExists && afresh) {
				admin.disableTable(table);
				admin.deleteTable(table);
			}
			if (!(isExists && afresh == false)) {
				HTableDescriptor tableDescriptor = new HTableDescriptor(table);
				for (String familyName : familyNames) {
					HColumnDescriptor columnDescriptor = new HColumnDescriptor(
							familyName);
					// columnDescriptor设置信息并未暴露
					// columnDescriptor.setMaxVersions(Integer.MAX_VALUE);
					// columnDescriptor.setTimeToLive(Integer.MAX_VALUE);
					tableDescriptor.addFamily(columnDescriptor);
				}
				admin.createTable(tableDescriptor);
			}
		} catch (Exception e) {
			Log4j.error(e);
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ie) {
				Log4j.error(ie);
			}
		}
	}

	public void insert(String tableName, HbaseRow row) {
		try {
			Put put = new Put(row.getRowKey().getBytes());
			Map<String, String> cols = row.getCols();
			Set<String> colKeys = row.getCols().keySet();
			String family = row.getColFamily();
			for (String colkey : colKeys) {
				put.addColumn(family.getBytes(), colkey.getBytes(),
						cols.get(colkey).getBytes());
			}
			conn.getTable(TableName.valueOf(tableName)).put(put);
		} catch (IOException e) {
			Log4j.error(e);
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				Log4j.error(ie);
			}
		}
	}

	public void insert(String tableName, List<HbaseRow> rows) {
		try {
			List<Put> puts = new ArrayList<Put>();
			for (HbaseRow row : rows) {
				Put put = new Put(row.getRowKey().getBytes());
				Map<String, String> cols = row.getCols();
				Set<String> colKeys = row.getCols().keySet();
				String family = row.getColFamily();
				for (String colkey : colKeys) {
					put.addColumn(family.getBytes(), colkey.getBytes(), cols
							.get(colkey).getBytes());
				}
				puts.add(put);
			}
			conn.getTable(TableName.valueOf(tableName)).put(puts);
		} catch (IOException e) {
			Log4j.error(e);
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				Log4j.error(ie);
			}
		}
	}

	public void find(String tableName, HbaseRow row) {
		try {
			TableName table = TableName.valueOf(tableName);
			Table t = conn.getTable(table);
			Result rs = t.get(new Get(row.getRowKey().getBytes()));
			Map<String, String> cols = row.getCols();
			Set<String> colKeys = row.getCols().keySet();
			String family = row.getColFamily();
			for (String colkey : colKeys) {
				String v = rs.getValue(family.getBytes(), colkey.getBytes()) == null ? ""
						: new String(rs.getValue(family.getBytes(),
								colkey.getBytes()));
				cols.put(colkey, v);
			}
			row.setCols(cols);
		} catch (Exception e) {
			Log4j.error(e);
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				Log4j.error(ie);
			}
		}
	}

	public List<HbaseRow> findRange(String tableName, String startRowKey,
			String endRowKey, HbaseRow row, boolean asc) {
		List<HbaseRow> ret = new ArrayList<HbaseRow>();
		try {
			Scan scan = new Scan();
			Set<String> colKeys = row.getCols().keySet();
			String family = row.getColFamily();
			for (String colkey : colKeys) {
				scan.addColumn(family.getBytes(), colkey.getBytes());
			}
			if (!asc) {
				scan.setReversed(true);
			}
			if (startRowKey != null) {
				scan.setStartRow(startRowKey.getBytes());
			}
			if (endRowKey != null) {
				scan.setStopRow(endRowKey.getBytes());
			}
			TableName table = TableName.valueOf(tableName);
			Table t = conn.getTable(table);
			ResultScanner rs = t.getScanner(scan);
			for (Result r : rs) {
				Map<String, String> _cols = new HashMap<String, String>();
				for (String colkey : colKeys) {
					String v = r.getValue(family.getBytes(), colkey.getBytes()) == null ? ""
							: new String(r.getValue(family.getBytes(),
									colkey.getBytes()));
					_cols.put(colkey, v);
				}
				HbaseRow _row = new HbaseRow(family, new String(r.getRow()),
						_cols);
				ret.add(_row);
			}
		} catch (Exception e) {
			Log4j.error(e);
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				Log4j.error(ie);
			}
		}
		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		try {

			if (conn != null) {
				conn.close();
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}
}
