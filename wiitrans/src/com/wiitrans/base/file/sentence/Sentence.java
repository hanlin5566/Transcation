/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file.sentence;

import java.util.ArrayList;

import com.wiitrans.base.file.FileConst;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.PreprocessTerm;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.term.Term;
import com.wiitrans.base.xml.XNode;

public class Sentence {

	public enum RECOMMEND_TYPE {
		NONE, TM, MT
	};

	public String _source = null;
	// public boolean _sourcestatus = false;//是否需要翻译
	public String _translate = null;
	public String _translate_r = null;
	public boolean _translatestatus = false;
	public String _edit = null;
	public String _edit_r = null;
	public boolean _editstatus = false;
	public String _qa = null;
	public String _tcomment = null;
	public String _ecomment = null;
	public String _recommend = null;
	public RECOMMEND_TYPE _recommendType = null;
	// public Term _term = null; // In domain.
	public String _sourceDigit = null;
	public String _targetDigit = null;
	public ArrayList<Term> _termList = null;
	public String _hashCheck = null;

	// public SENTENCE_STATE _state = SENTENCE_STATE.NONE;
	public int _eScore;
	public int _qScore;
	// Index in file.
	public int _entityFragIndex;
	public FRAG_TYPE _entityFragType;
	// Index in file.
	public int _virtualFragIndex;
	// Index in paragraph.
	public int _entitySentenceIndex;

	public int _virtualSentenceIndex;
	// Counts of the source word.
	public short _sourceWordCount;

	// public short _sourceTagCount;

	public long _hashcode;
	public boolean _hashstatus;
	public boolean _valid;

	protected XNode _node = null;

	public Sentence() {
		_eScore = -1;
		_qScore = -1;
		_recommendType = RECOMMEND_TYPE.NONE;
		_entityFragIndex = FileConst.INVALID_INDEX;
		_virtualFragIndex = FileConst.INVALID_INDEX;
		_entitySentenceIndex = FileConst.INVALID_INT32_COUNT;
		_sourceWordCount = FileConst.INVALID_INT16_COUNT;
		// _sourceTagCount = FileConst.INVALID_INT16_COUNT;
		_hashcode = 0;
		_hashstatus = false;
		_valid = true;
	}

	public void SetNode(XNode node) {
		_node = node;
	}

	public int Parse(FileUtil fileutil) {
		int ret = Const.FAIL;

		// <Sentence index="" state="" wordcount="">
		_entitySentenceIndex = Util.String2Int(_node.GetAttr("index"));
		_sourceWordCount = Util.String2Short(_node.GetAttr("wordcount"));
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
				_hashstatus = Util.String2Bool(node.GetAttr("hashstatus"));
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

	public void Save(FileUtil fileutil) {

		_node.ClearChildren();
		// BiliXmlUtil bilixmlutil = new BiliXmlUtil();
		// <Sentence index="" state="" wordcount="">
		_node.SetAttr("index", _entitySentenceIndex);
		// _node.SetAttr("state", _state.toString());
		_node.SetAttr("wordcount", _sourceWordCount);
		// _node.SetAttr("tagcount", _sourceTagCount);
		_node.SetAttr("hashcode", String.valueOf(_hashcode));

		_node.SetAttr("valid", String.valueOf(_valid));

		// <Source>
		_node.AddChild("Source", fileutil.UnTagPair(_source));

		// if (_source.contains(String.valueOf('\u0008'))) {
		// String a = _source;
		// }

		// <Translate>
		XNode translateNode = new XNode("Translate");
		translateNode.SetAttr("hashstatus", String.valueOf(_hashstatus));
		translateNode.AddChild("Content", fileutil.UnTagPair(_translate));
		translateNode.AddChild("Content_r", fileutil.UnTagPair(_translate_r));
		translateNode.AddChild("Comment", _tcomment);
		_node.AddChild(translateNode);

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

	public void UnInit() {

		_source = null;
		_translate = null;
		_translate_r = null;
		_edit = null;
		_edit_r = null;
		_qa = null;
		_tcomment = null;
		_ecomment = null;
		_recommend = null;
		_recommendType = null;
		// _term = null;
		_sourceDigit = null;
		_targetDigit = null;
		if (_termList != null) {
			if (_termList.size() > 0) {
				for (Term term : _termList) {
					term.Uninit();
					term = null;
				}
			}
			_termList = null;
		}
		_hashCheck = null;
		// _state = null;
		_node = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else {
			Sentence sent = (Sentence) obj;

			if (this._virtualFragIndex == sent._virtualFragIndex
					&& this._virtualSentenceIndex == sent._virtualSentenceIndex) {
				return true;
			} else {
				return false;
			}
		}
	}
}
