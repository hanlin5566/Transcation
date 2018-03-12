package com.wiitrans.automation.bundle;

import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.automation.quartz.TaskScheduler;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.db.AutomationTaskDAO;
import com.wiitrans.base.db.model.AutomationTaskBean;
import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;

public class Bundle extends Thread implements IBundle, IServer {
	private IResponse _res = null;
	private Client _client = null;
	private String _id = null;
	private BundleRequest _spout = null;
	private TaskScheduler schedue;
	@Override
	public int NewClient(Client client) {
		return Response(client);
	}

	@Override
	public int SetResponse(IResponse res) {
		int ret = Const.FAIL;

		_res = res;
		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		_spout = new BundleRequest();
		schedue = TaskScheduler.getInstance();
		//TODO:读取全部未执行任务，执行。
		ret = Const.SUCCESS;
		return ret;
	}

	@Override
	public int Start() {
		int ret = Const.FAIL;

		ret = Init();
		if (Const.SUCCESS == ret) {
			this.start();
		}

		if (Const.SUCCESS == ret) {
			//加载服务自身配置任务
			Set<String> reqs = BundleConf.REQ_TEMPLATE.keySet();
			for (String req_name : reqs) {
				Map<String, String> taskMap = BundleConf.REQ_TEMPLATE.get(req_name);
				AutomationTaskBean taskBean = new AutomationTaskBean();
				taskBean.setTask_id((int)System.currentTimeMillis());
				taskBean.setCorn(taskMap.get("cron"));
				taskBean.setJob_class(taskMap.get("className"));
				JSONObject param = new JSONObject();
				param.put("url", taskMap.get("url"));
				param.put("req_type", taskMap.get("req_type"));
				param.put("param", taskMap.get("param"));
				taskBean.setParam(param.toString());
				schedue.getQueue().add(taskBean);
			}
			ret = _spout.Start();
		}

		return ret;
	}

	@Override
	public int Stop() {
		int ret = Const.FAIL;

		return ret;
	}

	private int Invalid(String msg) {
		int ret = Const.FAIL;

		JSONObject obj = new JSONObject(msg);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		JSONObject resObj = new JSONObject();
		resObj.put("result", "FAILED");
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID, id);
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		_res.Response(id, resObj.toString().getBytes());

		return ret;
	}
	
	@Override
	public int Request(String msg) {

		int ret = Const.FAIL;
		if (_client != null) {
			JSONObject obj = new JSONObject(msg);
			String aid = Util.GetStringFromJSon("aid", obj);
			String method = Util.GetStringFromJSon("method", obj);
			switch (method) {
			case "POST": {
				//缓存用户在线状态，为推送消息做缓存
				switch (aid) {
				//接收新任务
				case "newtask":
					/**
					 * @param jsonObject
					 * {
					 * "sid":"m8ncbjqmfre4ks096nlogqe0l6",
					 * "uid":"65",
					 * "taskType":"1",任务类型 1-实时任务，直接发送至strom，2-定时任务
					 * "corn":"", quartz的corn表达式
					 * "job_class":"com.wiitrans.automation.quartz.job.PushStromJob", 定时任务的实现类，目前有pushstrom与request
					 * param：传入执行类的参数：
					 * "param":{
					 * "className":"com.wiitrans.automation.logic.SendMailLogicImpl",//具体实现类 目前有SendMailLogicImpl与syncDataLogicImpl
					 * 实现类内所需参数：
					 * "order_id":"9Z2ETJNZ",
					 * "mailTemplate":"personnalQuotationOrder"
					 * }
					 */
					String taskType = Util.GetStringFromJSon("taskType", obj);//任务类型
					String corn = Util.GetStringFromJSon("corn", obj);
					String job_class = Util.GetStringFromJSon("job_class", obj);
					String param = obj.getJSONObject("param").toString();
					//将新任务添加至DB
					AutomationTaskBean taskBean = new AutomationTaskBean();
					taskBean.setJob_class(job_class);
					taskBean.setCorn(corn);
					taskBean.setParam(param);
					taskBean.setType(Integer.parseInt(taskType));
					taskBean.setRemaker("new task");
					AutomationTaskDAO taskDAO = new AutomationTaskDAO();
					taskDAO.Init(true);
					taskDAO.insert(taskBean);
					obj.put("task_id", ""+taskBean.getTask_id());
					taskDAO.Commit();
					taskDAO.UnInit();
					SendToPHP(obj, "OK");
					switch (taskType) {
						case "1"://realtime task
							//实时任务直接发送至strom
							ret = _spout.Push(obj.toString());
							break;
						case "2"://timed task
							//定时任务发送至quartz，等待quartz发送至strom
							schedue.getQueue().add(taskBean);
							break;
						default:
							break;
					}
					break;
				//接收任务回执
				case "receive":
					//根据回执更新DB
					break;
				//同步任务
				case "snychrotask":
					//读取DB同步任务
					break;
				default:
					break;
				}
			}
			default:
				break;
			}
			
		} else {
			ret = Invalid(msg);
		}
		return ret;
	}

	@Override
	public String GetBundleId() {
		return BundleConf.AUTOMATION_BUNDLE_ID;
	}

	@Override
	public int Request(JSONObject msg) {
		Log4j.log("auto bundle " + msg.toString());
		return Request(msg.toString());
	}

	public int Response(Client client) {
		int ret = Const.FAIL;

		JSONObject obj = client.GetBundleInfoJSON();
		String state = obj.getString(Const.BUNDLE_INFO_STATE);
		String id = obj.getString(Const.BUNDLE_INFO_ID);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			String bid = obj.getString(Const.BUNDLE_INFO_BUNDLE_ID);
			// Registe bundle.
			if (0 == (bid.compareTo(BundleConf.AUTOMATION_BUNDLE_ID))) {
				if (_client != null) {
					_client.GetContext().close();
				}

				_client = client;
				_id = bid;
				_spout.SetClient(client);

				Log4j.log("Bundle[" + _id + "] is actived.");
			} else {
				Log4j.error("Registe bundle[" + _id + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {

			String result = Util.GetStringFromJSon("result", obj);
			if (result != null) {

				switch (result) {
				case "1111111111": {
					break;
				}
				default: {
					// 回复消息到PHP
					if (_res != null) {
						_res.Response(id, obj.toString().getBytes());
					} else {
						Log4j.error("msg service bundle callback is null");
					}
					break;
				}
				}
			} else {
				Log4j.error("The report msg result is null.");
			}
			break;
		}
		default:
			Log4j.error("msg service bundle state[" + state + "] error");
			break;
		}

		return ret;
	}

	public void run() {
		BaseServer svr = new BaseServer();
		svr.SetPort(BundleConf.AUTOMATION_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}
	
	private int SendToPHP(JSONObject obj, String result) {
		JSONObject resObj = new JSONObject();
		resObj.put("result", result);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		resObj.put(Const.BUNDLE_INFO_ID, id);
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		return _res.Response(id, resObj.toString().getBytes());
	}
	
	@Override
	public int SetContent(String clientId, ByteBuf content, boolean isSplit) {
		return Const.NOT_IMPLEMENTED;
	}
}
