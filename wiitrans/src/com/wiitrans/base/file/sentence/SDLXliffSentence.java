package com.wiitrans.base.file.sentence;

import java.util.ArrayList;

import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.term.Term;
import com.wiitrans.base.xml.XNode;

public class SDLXliffSentence extends Sentence {
	public String _recomTrans = null;
	public boolean _lock = false;
	public int _percent = 0;
	public int _totalSourceWordCount;
	public String _mid;

	@Override
	public void Save(FileUtil fileutil) {

		_node.ClearChildren();
		// BiliXmlUtil bilixmlutil = new BiliXmlUtil();
		// <Sentence index="" state="" wordcount="">
		_node.SetAttr("index", _entitySentenceIndex);
		_node.SetAttr("mid", _mid);
		// _node.SetAttr("state", _state.toString());
		_node.SetAttr("wordcount", _sourceWordCount);
		_node.SetAttr("totalwordcount", "" + _totalSourceWordCount);
		// _node.SetAttr("tagcount", _sourceTagCount);
		_node.SetAttr("hashcode", String.valueOf(_hashcode));
		_node.SetAttr("valid", String.valueOf(_valid));
		_node.SetAttr("lock", String.valueOf(_lock));
		_node.SetAttr("percent", String.valueOf(_percent));

		// <Source>
		_node.AddChild("Source", fileutil.UnTagPair(_source));

		// if (_source.contains(String.valueOf('\u0008'))) {
		// String a = _source;
		// }

		// <Translate>
		XNode translateNode = new XNode("Translate");
		translateNode.AddChild("Content", fileutil.UnTagPair(_translate));
		translateNode.AddChild("Content_r", fileutil.UnTagPair(_translate_r));
		translateNode.AddChild("Comment", _tcomment);
		_node.AddChild(translateNode);

		// <RecomTrans>
		XNode recomTransNode = new XNode("RecomTrans");
		recomTransNode.AddChild("Content", fileutil.UnTagPair(_recomTrans));
		_node.AddChild(recomTransNode);

		// <Edit>
		XNode editNode = new XNode("Edit");
		editNode.AddChild("Content", fileutil.UnTagPair(_edit));
		editNode.AddChild("Content_r", fileutil.UnTagPair(_edit_r));
		editNode.AddChild("Comment", _ecomment);
		editNode.AddChild("Score", String.valueOf(_eScore));
		_node.AddChild(editNode);

		// <QA>
		XNode qaNode = new XNode("QA");
		qaNode.AddChild("Content", _qa);
		qaNode.AddChild("Score", String.valueOf(_qScore));
		_node.AddChild(qaNode);

		// <Recommend>
		XNode recommendNode = new XNode("Recommend");
		recommendNode.AddChild("Content", _recommend);
		recommendNode.AddChild("Type", _recommendType.toString());
		_node.AddChild(recommendNode);

		// <CheckDigit>
		XNode checkDigitNode = new XNode("CheckDigit");
		checkDigitNode.SetAttr("source", _sourceDigit);
		checkDigitNode.SetAttr("target", _targetDigit);
		_node.AddChild(checkDigitNode);

		XNode hashCheckNode = new XNode("HashCheck");
		hashCheckNode.SetAttr("value", _hashCheck);
		_node.AddChild(hashCheckNode);

		// <Term>
		XNode checkTermsNode = new XNode("CheckTerms");
		if (_termList != null && _termList.size() > 0) {
			for (Term term : _termList) {
				XNode termNode = new XNode("term");
				termNode.SetAttr("term_id", term.term_id);
				termNode.SetAttr("term", term._term);
				termNode.SetAttr("meaning", term.check_meaning);
				termNode.SetAttr("remark", term.check_remark);
				termNode.SetAttr("usage", term.check_usage);
				termNode.SetAttr("begin", term.check_begin);
				termNode.SetAttr("end", term.check_end);
				termNode.SetAttr("count", term.check_count);
				termNode.SetAttr("checknumber", term.check_number);
				checkTermsNode.AddChild(termNode);
			}
		}
		_node.AddChild(checkTermsNode);

	}

	public int Parse(FileUtil fileutil) {
		int ret = Const.FAIL;

		// <Sentence index="" state="" wordcount="">
		_entitySentenceIndex = Util.String2Int(_node.GetAttr("index"));
		_percent = Util.String2Int(_node.GetAttr("percent"));
		_lock = Util.String2Bool(_node.GetAttr("lock"));
		_mid = _node.GetAttr("mid");
		_sourceWordCount = Util.String2Short(_node.GetAttr("wordcount"));
		_totalSourceWordCount = Util
				.String2Int(_node.GetAttr("totalwordcount"));
		// _sourceTagCount = Util.String2Short(_node.GetAttr("tagcount"));
		// _state = SENTENCE_STATE.String2Enum(_node.GetAttr("state"));
		_hashcode = Util.String2Long(_node.GetAttr("hashcode"));
		_valid = Util.String2Bool(_node.GetAttr("valid"));

		for (XNode node : _node.GetChildren()) {
			if (0 == node.GetTagName().compareTo("Source")) {
				ArrayList<XNode> children = node.GetChildren();
				if (children.size() > 0) {
					for (XNode text : children) {
						_source = fileutil.TagPair(text.GetValue());
					}
				} else {
					String content = node.getContent();
					if (content != null && content.trim().length() == 0) {
						_source = content;
					}
				}

				ret = Const.SUCCESS;

			} else if (0 == node.GetTagName().compareTo("Translate")) {
				for (XNode tran : node.GetChildren()) {
					if (0 == tran.GetTagName().compareTo("Content")) {
						for (XNode text : tran.GetChildren()) {
							_translate = fileutil.TagPair(text.GetValue());
						}

						ret = Const.SUCCESS;
					} else if (0 == tran.GetTagName().compareTo("Content_r")) {
						for (XNode text : tran.GetChildren()) {
							_translate_r = fileutil.TagPair(text.GetValue());
						}

						ret = Const.SUCCESS;

					} else if (0 == tran.GetTagName().compareTo("Comment")) {
						for (XNode text : tran.GetChildren()) {
							_tcomment = text.GetValue();
						}

						ret = Const.SUCCESS;

					} else {
						// Error, not parse.
					}
				}
			} else if (0 == node.GetTagName().compareTo("RecomTrans")) {
				for (XNode edit : node.GetChildren()) {
					if (0 == edit.GetTagName().compareTo("Content")) {
						for (XNode text : edit.GetChildren()) {
							_recomTrans = fileutil.TagPair(text.GetValue());
						}

						ret = Const.SUCCESS;

					} else {
						// Error, not parse.
					}
				}
			} else if (0 == node.GetTagName().compareTo("Edit")) {
				for (XNode edit : node.GetChildren()) {
					if (0 == edit.GetTagName().compareTo("Content")) {
						for (XNode text : edit.GetChildren()) {
							_edit = fileutil.TagPair(text.GetValue());
						}

						ret = Const.SUCCESS;
					} else if (0 == edit.GetTagName().compareTo("Content_r")) {
						for (XNode text : edit.GetChildren()) {
							_edit_r = fileutil.TagPair(text.GetValue());
						}

						ret = Const.SUCCESS;

					} else if (0 == edit.GetTagName().compareTo("Comment")) {
						for (XNode text : edit.GetChildren()) {
							_ecomment = text.GetValue();
						}

						ret = Const.SUCCESS;

					} else if (0 == edit.GetTagName().compareTo("Score")) {
						for (XNode text : edit.GetChildren()) {
							_eScore = Util.String2Int(text.GetValue());
						}

						ret = Const.SUCCESS;

					} else {
						// Error, not parse.
					}
				}
			} else if (0 == node.GetTagName().compareTo("QA")) {
				for (XNode qa : node.GetChildren()) {
					if (0 == qa.GetTagName().compareTo("Content")) {
						for (XNode text : qa.GetChildren()) {
							_qa = text.GetValue();
						}

						ret = Const.SUCCESS;

					} else if (0 == qa.GetTagName().compareTo("Score")) {
						for (XNode text : qa.GetChildren()) {
							_qScore = Util.String2Int(text.GetValue());
						}

						ret = Const.SUCCESS;

					} else {
						// Error, not parse.
					}
				}
			} else if (0 == node.GetTagName().compareTo("Recommend")) {
				for (XNode recommend : node.GetChildren()) {
					if (0 == recommend.GetTagName().compareTo("Content")) {
						for (XNode text : recommend.GetChildren()) {
							_recommend = text.GetValue();
						}

						ret = Const.SUCCESS;

					} else if (0 == recommend.GetTagName().compareTo("Type")) {
						for (XNode text : recommend.GetChildren()) {
							switch (text.GetValue()) {
							case "mt": {
								_recommendType = RECOMMEND_TYPE.MT;
								break;
							}
							case "tm": {
								_recommendType = RECOMMEND_TYPE.TM;
								break;
							}
							default:
								_recommendType = RECOMMEND_TYPE.NONE;
								break;
							}

						}

						ret = Const.SUCCESS;

					} else {
						// Error, not parse.
					}
				}
			} else if (0 == node.GetTagName().compareTo("CheckDigit")) {
				_sourceDigit = node.GetAttr("source");
				_targetDigit = node.GetAttr("target");
				ret = Const.SUCCESS;
			} else if (0 == node.GetTagName().compareTo("HashCheck")) {
				_hashCheck = node.GetAttr("value");
				ret = Const.SUCCESS;
			} else if (0 == node.GetTagName().compareTo("CheckTerms")) {
				_termList = new ArrayList<Term>();
				for (XNode xterm : node.GetChildren()) {
					if (0 == xterm.GetTagName().compareTo("term")) {
						Term term = new Term();
						term.term_id = Util
								.String2Int(xterm.GetAttr("term_id"));
						term._term = xterm.GetAttr("term");
						term.check_meaning = xterm.GetAttr("meaning");
						term.check_remark = xterm.GetAttr("remark");
						term.check_usage = xterm.GetAttr("usage");
						term.check_begin = Util.String2Int(xterm
								.GetAttr("begin"));
						term.check_end = Util.String2Int(xterm.GetAttr("end"));
						term.check_count = Util.String2Int(xterm
								.GetAttr("count"));
						term.check_number = Util.String2Int(xterm
								.GetAttr("checknumber"));
						_termList.add(term);
						ret = Const.SUCCESS;

					} else {
						// Error, not parse.
					}
				}

				ret = Const.SUCCESS;

			} else {
				// Error, not parse.
			}
		}
		if (_translate == null || _translate.trim().length() == 0) {
			_translatestatus = false;
		} else {
			_translatestatus = true;
		}

		if (_eScore < 0) {
			_editstatus = false;
		} else {
			_editstatus = true;
		}
		return ret;
	}
}
