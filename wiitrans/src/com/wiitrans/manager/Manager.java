/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.manager;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.service.Service;

public class Manager {

	private Service _service = null;
	private ClientQueue _clientQueue = null;
	private BundleQueue _bundleQueue = null;
	private ClientManager _clientManager = null;
	private BundleManager _bundleManager = null;

	public int ManageQuque() {
		int ret = Const.FAIL;

		if (null == _clientQueue) {
			_clientQueue = new ClientQueue();
		}

		if (null == _bundleQueue) {
			_bundleQueue = new BundleQueue();
		}

		ret = Const.SUCCESS;

		return ret;
	}

	public int ManageClients() {
		if (null == _clientManager) {
			_clientManager = new ClientManager(_bundleQueue, _clientQueue);
		}

		return _clientManager.Start();
	}

	public int ManageBundles() {
		if (null == _bundleManager) {
			_bundleManager = new BundleManager(_bundleQueue, _clientQueue);
		}

		return _bundleManager.Start();
	}

	public int ManageService() {
		if (null == _service) {
			_service = new Service(_clientManager);
		}

		return _service.Start();
	}

	public int Debug() {
		int ret = Const.FAIL;

		ret = Const.SUCCESS;

		return ret;
	}

	public static void main(String[] args) {

		int ret = Const.FAIL;

//		AppConfig app = new AppConfig();
//		app.Parse(1);
		WiitransConfig.getInstance(1);

		Manager man = new Manager();

		ret = man.ManageQuque();

		if (Const.SUCCESS == ret) {
			ret = man.ManageBundles();
		}

		if (Const.SUCCESS == ret) {
			ret = man.ManageClients();
		}

		if (Const.SUCCESS == ret) {
			ret = man.Debug();
		}

		if (Const.SUCCESS == ret) {
			ret = man.ManageService();
		}

		if (Const.SUCCESS == ret) {
			Log4j.log("Manager normal exit.");
		} else {
			Log4j.error("Manager abnormal exit!");
		}
	}
}
