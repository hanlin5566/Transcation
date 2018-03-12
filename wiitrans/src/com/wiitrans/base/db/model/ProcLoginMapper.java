package com.wiitrans.base.db.model;

import java.util.List;

public interface ProcLoginMapper {
	public List<Integer> Login(int user_id);
	public int getSysMsgCount(int user_id);
}
