package com.wiitrans.frag.bundle;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.file.notag.BiliFileDetails;
import com.wiitrans.base.interproc.*;

public class Bundle extends Thread implements IBundle, IServer {
	// Bolt返回的JSONObject :
	// { 文件编号，文件总页数，类型T/E（尚未配置），每页句子数
	// 文本内容 { 第一页 { 第一句 { 句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文}
	// }}，
	// 第二句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，}，...
	// 第N句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，}} } }
	class PageMeta {
		public JSONObject _obj = null;
		public byte[] _buf = null;
		public boolean _isDirty = true;
	}

	class Pages {
		public ArrayList<PageMeta> _page = null;
		public int _pageCount = 0;

		public Pages() {
			if (_page == null) {
				_page = new ArrayList<PageMeta>();
			}
		}

		public void Uninit() {
			if (_page != null) {
				for (PageMeta pageMeta : _page) {
					pageMeta._obj = null;
					pageMeta._buf = null;
				}
				_page.clear();
			}
		}
	}

	private IResponse _bundleManager = null;
	private String _bid = null;
	private BundleRequest _spout = null;
	private ConcurrentHashMap<String, Pages> _files = null;
	// 双语文件持久化
	private FragPersistence _persistence = null;
	private Client _client = null;

	private ConcurrentHashMap<String, BiliFileDetails> _filedetails = null;

	@Override
	public String GetBundleId() {
		return BundleConf.FRAGMENTATION_BUNDLE_ID;
	}

	@Override
	public int SetResponse(IResponse res) {

		int ret = Const.FAIL;

		_bundleManager = res;
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

		return _spout.Start();
	}

	@Override
	public int Stop() {

		int ret = Const.FAIL;

		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		_spout = new BundleRequest();

		if (_files == null) {
			_files = new ConcurrentHashMap<String, Pages>();
		}

		if (_persistence == null) {
			_persistence = new FragPersistence(_spout);
		}

		if (_filedetails == null) {
			_filedetails = new ConcurrentHashMap<String, BiliFileDetails>();
		}

		ret = Const.SUCCESS;

		return ret;
	}

	// 从当前Bundle缓存中过去请求页返回给PHP
	private int SendToPHP(PageMeta page, String id) {
		int ret = Const.FAIL;

		if (page._isDirty) {
			page._buf = page._obj.toString().getBytes();
			page._isDirty = false;
		}
		// Report msg.
		if (_bundleManager != null) {
			_bundleManager.Response(id, page._buf);
			ret = Const.SUCCESS;
		} else {
			Log4j.error("Frag service bundle callback is null");
		}

		return ret;
	}

	private int PageNoHandler(String fid, int pageNo, String id) {
		int ret = Const.FAIL;

		if (_files.containsKey(fid)) {
			int retPageNo = 0;
			Pages pages = _files.get(fid);
			// 页面从1开始，缓存数据从0开始
			if (pageNo < 0) {
				// 返回第一页
				retPageNo = 0;
			} else if (pageNo >= pages._pageCount) {
				// 返回最后一页
				retPageNo = pages._pageCount - 1;
			} else {
				retPageNo = pageNo;
			}

			ret = SendToPHP(pages._page.get(retPageNo), id);
		} else {
			Log4j.error("Bundle buffer is not exist " + fid);
		}

		return ret;
	}

	// private int SynHandler(JSONObject obj) {
	private int SynHandler(String fid, int pageNo, int sentNo, String type,
			String newSent, String newSent_r, int score) {
		int ret = Const.FAIL;

		// String fid = Util.GetStringFromJSon("fid", obj);
		// int pageNo = Util.GetIntFromJSon("pageno", obj);
		if (_files.containsKey(fid)) {
			Pages pages = _files.get(fid);
			// 页面从1开始，缓存数据从1开始
			if ((pageNo >= 0) && (pageNo < pages._pageCount)) {
				// int sentNo = Util.GetIntFromJSon("sentno", obj);

				// String type = Util.GetStringFromJSon("type", obj);

				if (type.equalsIgnoreCase("T") || type.equalsIgnoreCase("E")) {
					// _page中句子编号从0开始
					PageMeta page = pages._page.get(pageNo);
					JSONObject sentJSONs = Util.GetJSonFromJSon("content",
							page._obj);
					if (sentJSONs != null) {
						JSONObject sentObj = Util.GetJSonFromJSon(
								Integer.toString(sentNo), sentJSONs);
						// 从Bundle缓存中获取译文（T或E）
						String oldSent = Util.GetStringFromJSon(
								type.toUpperCase(), sentObj);
						if (oldSent == null) {
							oldSent = "";
						}

						if (newSent != null && 0 != oldSent.compareTo(newSent)
								|| score >= 0 || newSent_r != null) {
							if (sentObj == null) {
								sentObj = new JSONObject();
								// page._obj
								// .put(Integer.toString(sentNo), sentObj);
							}
							if (newSent != null
									&& 0 != oldSent.compareTo(newSent)) {
								sentObj.put(type.toUpperCase(), newSent);
								if (type.equalsIgnoreCase("T")) {
									sentObj.put("hashstatus", "true");
								}
							}
							if (newSent_r != null) {
								sentObj.put(type.toUpperCase() + "_r",
										newSent_r);
							}
							if (score >= 0) {
								sentObj.put("score", score);
							}

							// sentJSONs.put(Integer.toString(sentNo), sentObj);
							// page._obj.put("content", sentJSONs);

							page._isDirty = true;
							// file is dirty, may be save to fdfs
							_persistence.Push(fid);

						}
					}
				} else {
					Log4j.error("Sentence type is invalid " + type);
				}
			} else {
				Log4j.error("Bundle buffer is not exist pageno " + pageNo);
			}

		} else {
			Log4j.error("Bundle buffer is not exist fid " + fid);
		}

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

		_bundleManager.Response(id, resObj.toString().getBytes());

		return ret;
	}

	@Override
	public int Request(String msg) {

		int ret = Const.FAIL;
		if (_client != null) {
			ret = _spout.Push(msg);
		} else {
			ret = Invalid(msg);
		}
		return ret;
	}

	@Override
	public int Request(JSONObject msg) {

		Log4j.log("frag bundle " + msg.toString());

		int ret = Const.FAIL;
		String fid = Util.GetStringFromJSon("fid", msg);
		String aid = Util.GetStringFromJSon("aid", msg);
		switch (aid) {
		case "pageno": {
			// PHP在线编辑器翻页
			int pageNo = Util.GetIntFromJSon("pageno", msg);
			String id = Util.GetStringFromJSon("id", msg);

			ret = PageNoHandler(fid, pageNo, id);
			if (Const.FAIL == ret) {
				msg.put("result", "FAILED");
				_bundleManager.Response(id, msg.toString().getBytes());
			}
			break;
		}
		case "syn": {
			// 同步保存当前句子，Bundle缓存保存完毕后发送Bolt进行持久化

			// String fid = Util.GetStringFromJSon("fid", obj);
			int pageNo = Util.GetIntFromJSon("pageno", msg);
			int sentNo = Util.GetIntFromJSon("sentno", msg);
			String type = Util.GetStringFromJSon("type", msg);
			String newSent = Util.GetStringFromJSon("text", msg);
			// String newSent_r = Util.GetStringFromJSon("content_r", obj);
			int score = Util.GetIntFromJSon("score", msg);

			ret = SynHandler(fid, pageNo, sentNo, type, newSent, null, score);
			// ret = SynHandler(msg);

			ret = Request(msg.toString());
			break;
		}
		case "details": {

			JSONObject resObj = new JSONObject();
			BiliFileDetails details = null;
			if (_filedetails.containsKey(fid)) {
				details = _filedetails.get(fid);
			}
			if (details == null) {
				resObj.put("result", "FAILED");
			} else {
				resObj.put("result", "OK");
				resObj.put("sentcount", String.valueOf(details.sentenceCount));
				resObj.put("tsentcount",
						String.valueOf(details.tranlateSentenceCount));
				resObj.put("esentcount",
						String.valueOf(details.editSentenceCount));
				resObj.put("wordcount", String.valueOf(details.wordCount));
				resObj.put("twordcount",
						String.valueOf(details.tranlateWordCount));
				resObj.put("ewordcount", String.valueOf(details.editWordCount));
			}

			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, msg);
			resObj.put(Const.BUNDLE_INFO_ID, id);
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, msg));

			return _bundleManager.Response(id, resObj.toString().getBytes());
		}
		default: {

			ret = Request(msg.toString());
			break;
		}
		}

		return ret;
	}

	private int InsertToBundleBuf(JSONObject obj, boolean compel) {
		int ret = Const.FAIL;

		// Bolt返回的JSONObject :
		// { PHP连接ID，文件编号，文件总页数，类型T/E（尚未配置），每页句子数
		// 文本内容 { 第一页 { 第一句 { 句子编号，原文，T，E，推荐译文，推荐术语
		// {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，
		// 第二句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，}，...
		// 第N句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，}}
		// 第二页 ... } }
		try {
			String fid = Util.GetStringFromJSon("fid", obj);
			if (fid != null) {
				if (compel) {
					// 遇到跨节点Q时，强制刷新
					if (_files.containsKey(fid)) {
						Pages p = _files.remove(fid);
						p.Uninit();
					}
				}
				if (!_files.containsKey(fid)) {
					int pageCount = Util.GetIntFromJSon("pagecount", obj);
					String countPerPage = Util.GetStringFromJSon(
							"countperpage", obj);
					JSONObject contentObj = Util
							.GetJSonFromJSon("content", obj);
					String tag = Util.GetStringFromJSon("tag", obj);
					int index = 0;
					Pages pages = new Pages();

					pages._pageCount = pageCount;
					for (; index < pageCount; index++) {
						// 获取整页内容
						JSONObject pageObj = new JSONObject();
						pageObj.put("fid", fid);
						pageObj.put("pagecount", pageCount);
						pageObj.put("countperpage", countPerPage);
						JSONObject pageContentObj = Util.GetJSonFromJSon(
								Integer.toString(index), contentObj);
						pageObj.put("content", pageContentObj);
						pageObj.put("pageindex", index);
						pageObj.put("tag", tag);
						// 赋值给PageMeta
						PageMeta page = new PageMeta();
						page._obj = pageObj;

						// 赋值给Pages
						pages._page.add(page);
					}

					_files.put(fid, pages);
				}
			}
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	public int Response(Client client) {

		int ret = Const.FAIL;

		JSONObject obj = client.GetBundleInfoJSON();
		String state = Util.GetStringFromJSon(Const.BUNDLE_INFO_STATE, obj);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);

		String aid = Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			// Registe bundle.
			String bid = Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID,
					obj);
			if (0 == (bid.compareTo(BundleConf.FRAGMENTATION_BUNDLE_ID))) {
				_bid = bid;
				_spout.SetClient(client);
				_client = client;

				Log4j.log("Bundle[" + _bid + "] is actived.");

			} else {
				Log4j.error("Registe bundle[" + _bid + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {
			// 将Bolt返回的数据保存到Bundle缓存
			switch (aid) {
			case "start": {
				String result = Util.GetStringFromJSon("result", obj);
				String fid = Util.GetStringFromJSon("fid", obj);
				if (result.equals("OK")) {

					BiliFileDetails details;
					if (_filedetails.containsKey(fid)) {
						details = _filedetails.get(fid);
					} else {
						details = new BiliFileDetails();
						_filedetails.put(fid, details);
					}
					details.sentenceCount = Util.GetIntFromJSon("sentcount",
							obj);
					details.tranlateSentenceCount = Util.GetIntFromJSon(
							"tsentcount", obj);
					details.editSentenceCount = Util.GetIntFromJSon(
							"esentcount", obj);
					details.wordCount = Util.GetIntFromJSon("wordcount", obj);
					details.tranlateWordCount = Util.GetIntFromJSon(
							"twordcount", obj);
					details.editWordCount = Util.GetIntFromJSon("ewordcount",
							obj);
					// 获取参数，判断是否所质量报告Q并且所需要强制刷新
					String openstate = Util.GetStringFromJSon("openstate", obj);
					String remove = Util.GetStringFromJSon("remove", obj);
					boolean compel = false;
					if (openstate != null && remove != null
							&& openstate.equalsIgnoreCase("Q")
							&& remove.equalsIgnoreCase("remove")) {
						compel = true;
					}
					ret = InsertToBundleBuf(obj, compel);
					if (Const.SUCCESS == ret) {

						// 发送第一页到PHP
						ret = PageNoHandler(fid, 0, id);
					} else {
						Log4j.error("Insert bolt json into bundle buffer is failed.");
						if (_bundleManager != null) {
							_bundleManager.Response(id, obj.toString()
									.getBytes());
						} else {
							Log4j.error("Frag service bundle callback is null");
						}
					}
				} else {
					Log4j.error("文件打开失败.");
					if (_bundleManager != null) {
						_bundleManager.Response(id, obj.toString().getBytes());
					} else {
						Log4j.error("Frag service bundle callback is null");
					}
				}
				break;
			}
			case "syn": {
				String fid = Util.GetStringFromJSon("fid", obj);
				BiliFileDetails details;
				if (_filedetails.containsKey(fid)) {
					details = _filedetails.get(fid);
				} else {
					details = new BiliFileDetails();
					_filedetails.put(fid, details);
				}
				details.sentenceCount = Util.GetIntFromJSon("sentcount", obj);
				details.tranlateSentenceCount = Util.GetIntFromJSon(
						"tsentcount", obj);
				details.editSentenceCount = Util.GetIntFromJSon("esentcount",
						obj);
				details.wordCount = Util.GetIntFromJSon("wordcount", obj);
				details.tranlateWordCount = Util.GetIntFromJSon("twordcount",
						obj);
				details.editWordCount = Util.GetIntFromJSon("ewordcount", obj);

				// content_r操作
				String result = Util.GetStringFromJSon("result", obj);
				if (result.equals("OK")) {
					int pageNo = Util.GetIntFromJSon("pageno", obj);
					int sentNo = Util.GetIntFromJSon("sentno", obj);
					String type = Util.GetStringFromJSon("type", obj);
					String newSent_r = Util.GetStringFromJSon("content_r", obj);

					ret = SynHandler(fid, pageNo, sentNo, type, null,
							newSent_r, -1);
				}
				// 重复句子操作
				JSONObject sameObject = Util.GetJSonFromJSon("same", obj);
				obj.remove("same");
				JSONObject sen;
				String pageSameObject = "";
				if (sameObject != null) {

					int pageNo = Util.GetIntFromJSon("pageno", obj);
					int sentNo = Util.GetIntFromJSon("sentno", obj);

					int newPageNo;
					int newSentNo;
					String type = Util.GetStringFromJSon("type", obj);
					String newSent = Util.GetStringFromJSon("text", obj);
					String newSent_r = Util.GetStringFromJSon("content_r", obj);
					int score = Util.GetIntFromJSon("score", obj);
					String[] names = JSONObject.getNames(sameObject);
					if (names != null && names.length > 0) {
						for (String name : names) {
							sen = Util.GetJSonFromJSon(name, sameObject);
							newPageNo = Util.GetIntFromJSon("pageno", sen);
							newSentNo = Util.GetIntFromJSon("sentno", sen);

							if (pageNo == newPageNo && sentNo != newSentNo) {
								pageSameObject = pageSameObject
										+ String.valueOf(newSentNo)
										+ String.valueOf(",");
							}

							SynHandler(fid, newPageNo, newSentNo, type,
									newSent, newSent_r, score);
						}
					}

					if (pageSameObject.length() > 0) {
						obj.put("pagesame", pageSameObject);
					}
				}

				if (_bundleManager != null) {
					_bundleManager.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("Frag service bundle callback is null");
				}
				break;
			}
			case "stop": {
				String result = Util.GetStringFromJSon("result", obj);
				if (result.equals("OK")) {
					String fid = Util.GetStringFromJSon("fid", obj);
					String remove = Util.GetStringFromJSon("remove", obj);
					if (_files.containsKey(fid) && remove != null
							&& remove.equalsIgnoreCase("remove")) {
						_files.remove(fid);
					}
				} else {
					Log4j.error("文件保存退出失败.");
				}

				if (_bundleManager != null) {
					_bundleManager.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("Frag service bundle callback is null");
				}
				break;
			}
			case "closefile": {
				String result = Util.GetStringFromJSon("result", obj);
				if (result.equals("OK")) {
					String fid = Util.GetStringFromJSon("fid", obj);
					if (_files.containsKey(fid)) {
						_files.remove(fid);
					}
				} else {
					Log4j.error("文件关闭失败.");
				}

				if (_bundleManager != null) {
					_bundleManager.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("Frag service bundle callback is null");
				}
				break;
			}
			default:
				ret = Const.SUCCESS;

				if (_bundleManager != null) {
					_bundleManager.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("Frag service bundle callback is null");
				}
				break;
			}

			break;
		}
		default:
			Log4j.error("Frag service bundle state[" + state + "] error");

			if (_bundleManager != null) {
				_bundleManager.Response(id, obj.toString().getBytes());
			}
			break;
		}

		return ret;
	}

	@Override
	public int NewClient(Client client) {

		return Response(client);
	}

	public void run() {
		BaseServer svr = new BaseServer();
		svr.SetPort(BundleConf.FRAGMENTATION_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}

	@Override
	public int SetContent(String clientId, ByteBuf content, boolean isSplit) {
		return Const.NOT_IMPLEMENTED;
	}
}
