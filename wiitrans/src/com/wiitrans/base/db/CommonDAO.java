package com.wiitrans.base.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class CommonDAO {

	protected SqlSession _session = null;

	public int Init(Boolean loadConf, JSONObject obj) {
		int ret = Const.FAIL;

		try {

			if (loadConf) {
				// AppConfig app = new AppConfig();
				// ret = app.Parse();
				WiitransConfig.getInstance(0);
			}

			int nid = Util.GetIntFromJSon("nid", obj);

			if (!BundleConf.BUNDLE_Node.isEmpty()) {
				File file = new File(BundleConf.BUNDLE_Node.get(nid).mybatis);
				InputStream inputStream = new FileInputStream(file);
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
						.build(inputStream);

				_session = sqlSessionFactory.openSession();
				ret = Const.SUCCESS;
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			if (loadConf) {
				// AppConfig app = new AppConfig();
				// ret = app.Parse();
				WiitransConfig.getInstance(0);
			}

			if (!BundleConf.BUNDLE_Node.isEmpty()) {
				File file = new File(
						BundleConf.BUNDLE_Node.get(BundleConf.DEFAULT_NID).mybatis);
				InputStream inputStream = new FileInputStream(file);
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
						.build(inputStream);

				_session = sqlSessionFactory.openSession();
				ret = Const.SUCCESS;
			}
		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public int Commit() {
		int ret = Const.FAIL;

		_session.commit();
		ret = Const.SUCCESS;

		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		try {

			if (_session != null) {
				_session.close();
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}
}
