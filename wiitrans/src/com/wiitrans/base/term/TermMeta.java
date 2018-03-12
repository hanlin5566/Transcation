package com.wiitrans.base.term;

import java.util.ArrayList;

public class TermMeta {

	public int _contributorUid = -1; // 贡献译员ID
	// public HashSet<Integer> _agreeList = null; // 赞同译员ID
	// public HashSet<Integer> _disagreeList = null; // 反对译员ID
	public ArrayList<Integer> _agreeList = null; // 赞同译员ID
	public ArrayList<Integer> _disagreeList = null; // 反对译员ID

	public String _meaning = null; // 译文
	public String _usage = null; // 用法
	public String _remark = null; // 备注
}
