package com.wiitrans.base.db;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import com.wiitrans.base.hbase.HbaseOrderLogDAO;

public class HBaseLog {

	public static void main(String[] args) {
		String file = "/root/Desktop/file/aaa.xml"; // ÎÄŒþŽæ·ÅÎ»ÖÃ
		HBaseLog dj = new HBaseLog();
//		dj.createXml(file);
//		dj.parserXml(file);
	}

	/**
	 * 生成XML
	 * 
	 * @param filePath
	 *            文件路径
	 */
//	public void createXml(String fileName) {
//		Element root = new Element("persons");
//		Document document = new Document(root, new DocType("tmx", "bb",
//				"tmx14.dtd"));
//		Element person = new Element("person");
//		root.addContent(person);
//		Element name = new Element("name");
//		name.setText("java");
//		person.addContent(name);
//		Element sex = new Element("sex");
//		sex.setText("man");
//		person.addContent(sex);
//		Element age = new Element("age");
//		age.setText("23");
//		person.addContent(age);
//		XMLOutputter XMLOut = new XMLOutputter();
//		try {
//			Format f = Format.getPrettyFormat();
//			f.setEncoding("UTF-8");// default=UTF-8
//			XMLOut.setFormat(f);
//			XMLOut.output(document, new FileOutputStream(fileName));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * 解析XML
	 * 
	 * @param filePath
	 *            文件路径
	 */
//	public void parserXml(String fileName) {
//		try {
//			SAXBuilder builder = new SAXBuilder();
//			Document document = builder.build(fileName);
//			Element root = document.getRootElement();
//			List persons = root.getChildren("person");
//			for (int i = 0; i < persons.size(); i++) {
//				Element person = (Element) persons.get(i);
//				List pros = person.getChildren();
//				for (int j = 0; j < pros.size(); j++) {
//					Element element = (Element) pros.get(j);
//					System.out.println(element.getName() + ":"
//							+ element.getValue());
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public static void main2(String[] args) {
		try {
			Date now = new Date();
			long timestamp = now.getTime();
			NumberFormat _numberFormat8 = NumberFormat.getInstance();
			_numberFormat8.setGroupingUsed(false);
			_numberFormat8.setMaximumIntegerDigits(8);
			_numberFormat8.setMinimumIntegerDigits(8);
			String time1 = _numberFormat8.format(timestamp % 86400000);
			SimpleDateFormat simpledateformat1 = new SimpleDateFormat(
					"yyyyMMdd");
			SimpleDateFormat simpledateformat2 = new SimpleDateFormat("HHmmss");
			String date = simpledateformat1.format(now);
			String time2 = simpledateformat2.format(now);
			System.out.println(date);
			System.out.println(time1);
			System.out.println(Integer.parseInt(time2.substring(0, 2)) * 3600
					+ Integer.parseInt(time2.substring(2, 4)) * 60
					+ Integer.parseInt(time2.substring(4)));
			System.out.println(simpledateformat2.parse(time2).getTime());
		} catch (Exception e) {

		}

	}

	public static void main_(String[] args) {
		HbaseOrderLogDAO dao = new HbaseOrderLogDAO();
		dao.Init(true);

		// dao.SearchOrderLog(1, 15, 0, 0);

		dao.UnInit();

	}

	public static Configuration conf;
	public static Connection conn;
	public final static String FAMILY = "msg";
	public final static String ROOMID = "room";
	// public static Logger log = Logger.getLogger(HbaseTest.class);
	static {
		conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		conf.set("hbase.zookeeper.quorum", "master");
		// conf.set("hbase.master", "192.168.1.192:600000");
		try {
			conn = ConnectionFactory.createConnection(conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int count = 0;

	public synchronized static int getCount() {
		return count;
	}

	public synchronized static void addtCount() {
		HBaseLog.count++;
	}

	public static void main___(String[] args) throws InterruptedException {

		// createTable("test");
		// while(count <= 1000000){
		// Thread thread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		// HbaseTest.addtCount();
		// long sTime = System.currentTimeMillis();
		// // TODO Auto-generated method stub
		// System.out.print("start ["+HbaseTest.getCount()+"] table --------->");
		// // log.info("start ["+HbaseTest.getCount()+"] table --------->");
		// String roomId = ROOMID+HbaseTest.getCount();
		// createTable(roomId);
		// for (int j = 0; j < 2500; j++) {
		// insert(roomId, ""+System.currentTimeMillis(),"hlhu",
		// "hello,anybody here?");
		// insert(roomId, ""+System.currentTimeMillis(),"ly", "hello,i'm here");
		// insert(roomId, ""+System.currentTimeMillis(),"hlhu",
		// "hello,what are u doing?");
		// insert(roomId, ""+System.currentTimeMillis(),"ly",
		// "hello,i'm eating");
		// }
		// System.out.println("end ["+HbaseTest.getCount()+"] table consume:"+(System.currentTimeMillis()
		// - sTime));
		// log.info("end ["+HbaseTest.getCount()+"] table consume:"+(System.currentTimeMillis()
		// - sTime));
		// sTime = System.currentTimeMillis();
		// }
		// });
		// thread.start();
		// Thread.sleep(3000);
		// }
		// long sTime = System.currentTimeMillis();
		// query("room_0010");
		// System.out.println("query end:"+(System.currentTimeMillis() -
		// sTime));
		deleteTable();
	}

	public static void deleteTable() {
		try {
			// TableName tableName = TableName.valueOf("room_1");
			Admin admin = conn.getAdmin();
			// Table ttable = conn.getTable(tableName);
			// System.out.println(ttable.getName());
			// System.out.println(admin.tableExists(tableName));
			// if(admin.tableExists(tableName)){
			// admin.disableTable(tableName);
			// admin.deleteTable(tableName);
			// }
			System.out.println("delete");
			TableName[] tablesNames = admin.listTableNames();
			int count = tablesNames.length;
			System.out.println("ROOM COUNT:" + tablesNames.length);
			for (TableName table : tablesNames) {
				if (admin.tableExists(table)) {
					admin.disableTable(table);
					admin.deleteTable(table);
					System.out.println("DELETE:" + table.getNameAsString());
				}
				System.out.println("还剩:" + (--count));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static void createTable(String tableName) {
		// System.out.println("start create table");
		try {

			Admin admin = conn.getAdmin();
			TableName table = TableName.valueOf(tableName);
			if (admin.tableExists(table)) {
				// admin.disableTable(table);
				// admin.deleteTable(table);
				// System.out.println(tableName +"is exist , don't create");
			} else {
				HTableDescriptor tableDescriptor = new HTableDescriptor(table);
				HColumnDescriptor columnDescriptor = new HColumnDescriptor(
						FAMILY);
				// columnDescriptor.setMaxVersions(Integer.MAX_VALUE);
				// columnDescriptor.setTimeToLive(Integer.MAX_VALUE);
				tableDescriptor.addFamily(columnDescriptor);
				admin.createTable(tableDescriptor);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
		}
		// System.out.println("end create table");
	}

	public static void insert(String tableName, String rowId, String userId,
			String content) {
		// System.out.println("start insert");
		try {
			Put put = new Put(rowId.getBytes());
			put.addColumn(FAMILY.getBytes(), "content".getBytes(),
					content.getBytes());
			put.addColumn(FAMILY.getBytes(), "userId".getBytes(),
					userId.getBytes());
			conn.getTable(TableName.valueOf(tableName)).put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
		}
		// System.out.println("end insert");
	}

	public static void find(String tableName, String rowId) {
		try {
			TableName table = TableName.valueOf(tableName);
			Table t = conn.getTable(table);
			Result rs = t.get(new Get(rowId.getBytes()));
			String content = new String(rs.getValue(FAMILY.getBytes(),
					"content".getBytes()));
			String userId = new String(rs.getValue(FAMILY.getBytes(),
					"userId".getBytes()));
			System.out.println("user:" + userId + "    speak:" + content);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
		}
	}

	public static void query(String tableName) {
		try {
			Scan scan = new Scan();
			scan.addColumn(FAMILY.getBytes(), "userId".getBytes());
			scan.addColumn(FAMILY.getBytes(), "content".getBytes());
			// scan.setTimeRange(System.currentTimeMillis()-100000,
			// System.currentTimeMillis());
			// scan.setMaxVersions(Integer.MAX_VALUE);
			scan.setStartRow("1".getBytes());
			scan.setStopRow("2".getBytes());
			TableName table = TableName.valueOf(tableName);
			Table t = conn.getTable(table);
			ResultScanner rs = t.getScanner(scan);
			for (Result r : rs) {
				// System.out.println(r);
				String content = new String(r.getValue(FAMILY.getBytes(),
						"content".getBytes()));
				String userId = new String(r.getValue(FAMILY.getBytes(),
						"userId".getBytes()));
				System.out.println("user:" + userId + "   speak:" + content);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (IOException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
		}
	}

}
