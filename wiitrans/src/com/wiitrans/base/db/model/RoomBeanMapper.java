package com.wiitrans.base.db.model;

import java.util.Map;

public interface RoomBeanMapper {
	public void insert(RoomBean room);
	public RoomBean selectRoom(Map<String,Object> param);
	public RoomBean selectRoomByRoom(RoomBean room);
}
