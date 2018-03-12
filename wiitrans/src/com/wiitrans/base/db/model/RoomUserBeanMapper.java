package com.wiitrans.base.db.model;

import java.util.List;

public interface RoomUserBeanMapper {
	public void insert(RoomUserBean roomUser);
	public void updateConnectTime(RoomUserBean roomUser);
	public void updateNodeConnectTime(RoomUserBean roomUser);
	public RoomUserBean selectRoomUser(RoomUserBean roomUser);
	public List<RoomUserBean> selectRoomUserList(RoomUserBean roomUser);
}
